package novayoung.log;

import novayoung.log.handler.CacheLogHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Controller
@RequestMapping("/looklog")
public class LookLogController {


    private static final Logger logger = LoggerFactory.getLogger(LookLogController.class);

    private static final String COOKIE_KEY = "LOOK_LOG";

    private static final String DEFAULT_PWD = "admin";

    private static final int CIRCUIT_BREAKER_POOL_SIZE = 10;

    private static final int CIRCUIT_BREAKER_PERMITS = 1;

    private static final long CIRCUIT_BREAKER_WAIT_TIME = 3000;

    private static final long DEFEN_TIME_INTERVAL = 3000;

    private static Map<String, Long> defenMap = new ConcurrentHashMap<>();

    private Semaphore circuitBreaker = new Semaphore(CIRCUIT_BREAKER_POOL_SIZE);

    @Autowired
    private CacheLogHandler cacheLogHandler;


    @Autowired
    private LookLogConfig lookLogConfig;


    @RequestMapping(value = "index")
    public String index(
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "startTime", required = false) String startTime,
                        @RequestParam(value = "endTime", required = false) String endTime,
                        @RequestParam(value = "traceId", required = false) String traceId,
                        @RequestParam(value = "limit", required = false) Integer limit,
                        @RequestParam(value = "order", required = false) Integer order,
                        @RequestParam(value = "level", required = false) String[] level,
                        HttpServletRequest request, HttpServletResponse response,
                        Map<String, Object> renderArgs
    ) throws ParseException {

        return circuitBreaker("index", renderArgs, () -> {

            if (!isAuth(request, response) &&  lookLogConfig.isSecurity()) {

                String sid = UUID.randomUUID().toString().replaceAll("-", "");

                renderArgs.put("sid", sid);

                return "looklog/login";
            }

            Date startDateTime = isBlank(startTime) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
            Date endDateTime   = isBlank(endTime) ? null : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);

            Integer limitParam = limit;
            if (limitParam == null) {
                limitParam = 200;
            }

            if (limitParam > lookLogConfig.getLookLogMaxLimit()) {
                limitParam = lookLogConfig.getLookLogMaxLimit();
            }

            Integer orderParam = order;
            if (order == null || (orderParam != -1 && orderParam != 1)) {
                orderParam = -1;
            }

            List<String> contentList = getLogs(traceId, keyword, startDateTime, endDateTime, level, orderParam, limitParam);

            renderArgs.put("keyword", keyword);
            renderArgs.put("startTime", startTime);
            renderArgs.put("endTime", endTime);
            renderArgs.put("level", level == null ? new ArrayList<>() : Arrays.asList(level));
            renderArgs.put("traceId", traceId);
            renderArgs.put("content", StringUtils.arrayToDelimitedString(contentList.toArray(), "\r\n").replaceAll("<", "&lt;").replaceAll(">", "&gt;"));
            renderArgs.put("size", contentList.size());
            renderArgs.put("order", orderParam);
            renderArgs.put("limit", limitParam);
            renderArgs.put("bufferSize", cacheLogHandler.getBufferSize());

            return "looklog/list";

        });



    }

    @RequestMapping(value = "logon", method = RequestMethod.POST)
    public String logon(
            @RequestParam(value = "pwd", required = false) String pwd,
            @RequestParam(value = "sid", required = false) String sid,
            @RequestParam(value = "vCode", required = false) String vCode,
            HttpServletRequest request, HttpServletResponse response, Map<String, Object> renderArgs
    ) throws IOException {


        return circuitBreaker("logon", renderArgs, () -> {

            if (isBlank(sid)) {
                return "looklog/login";
            }

            if (isBlank(vCode)) {
                renderArgs.put("sid", UUID.randomUUID().toString().replaceAll("-", ""));
                renderArgs.put("errorMsg", "验证码为空");
                return "looklog/login";
            }

            if (!vCode.equalsIgnoreCase(cacheLogHandler.get(sid))) {
                renderArgs.put("sid", UUID.randomUUID().toString().replaceAll("-", ""));
                renderArgs.put("errorMsg", "验证码不正确");
                return "looklog/login";
            }


            String password = lookLogConfig.getPassword();

            if (isBlank(password)) {
                password = DEFAULT_PWD;
            }

            if (!password.equals(pwd)) {
                renderArgs.put("sid", UUID.randomUUID().toString().replaceAll("-", ""));
                renderArgs.put("errorMsg", "密码不正确");
                return "looklog/login";
            }

            String token = UUID.randomUUID().toString().replaceAll("-", "");

            logon(token);

            Cookie cookie = new Cookie(COOKIE_KEY, token);

            cookie.setMaxAge(-1);

            response.addCookie(cookie);

            response.sendRedirect("index");

            return null;

        });


    }

    @RequestMapping(value = "vcode", method = RequestMethod.GET, produces = {MediaType.IMAGE_PNG_VALUE})
    public void vcode(String sid, HttpServletResponse response) throws IOException {

        circuitBreaker("vcode", new HashMap<>(), () -> {

            if (isBlank(sid)) {
                throw new IllegalArgumentException("sid is blank!");
            }

            ValidateCode vCode = new ValidateCode(120, 40, 5, 100);

            registryVcode(sid, vCode.getCode());

            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            vCode.write(response.getOutputStream());

            return null;

        });

    }

    private String circuitBreaker(String action, Map<String, Object> renderArgs, Closure closure) {

        try {

            boolean acquired = circuitBreaker.tryAcquire(CIRCUIT_BREAKER_PERMITS, CIRCUIT_BREAKER_WAIT_TIME, TimeUnit.MILLISECONDS);

            if (!acquired) {
                throw new TimeoutException("circuitBreaker tryAcquire failed!");
            }

            Long preTimestamp = defenMap.get(action);

            if (preTimestamp != null
                    && (System.currentTimeMillis() - preTimestamp) < DEFEN_TIME_INTERVAL) {

                renderArgs.put("intervalTime", DEFEN_TIME_INTERVAL / 1000);
                return "looklog/warn";
            }

            defenMap.put(action, System.currentTimeMillis());


            return closure.execute();

        } catch (Exception e) {

            return "";
            //throw new IllegalStateException(e.getMessage(), e);

        } finally {

            circuitBreaker.release(CIRCUIT_BREAKER_PERMITS);

        }

    }

    private void logon(String token) {

        cacheLogHandler.set(token, "true");

    }

    private void registryVcode(String sid, String code) {

        cacheLogHandler.set(sid, code);
    }


    private boolean isAuth(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if (!COOKIE_KEY.equals(cookie.getName())) {
                continue;
            }
            if (cacheLogHandler.get(cookie.getValue()) != null) {
                return true;
            }
        }

        return false;
    }


    private List<String> getLogs(String traceId, String keyword, Date startTime, Date endTime, String[] level, Integer order, Integer limit) {
        try {

            Map<String, Object> map = new HashMap<>();
            map.put("traceId", traceId);
            map.put("keyword", keyword);
            map.put("startTime", startTime);
            map.put("endTime", endTime);
            map.put("level", level);

            return cacheLogHandler.getLogs(map, order, limit);

        } catch (Exception e) {

            logger.warn("查询日志失败", e);

            throw e;

        }
    }

    private boolean isBlank(String s) {
        return s == null || "".equals(s.trim());
    }


    @FunctionalInterface
    private interface Closure {

        String execute() throws Exception;

    }

    private class ValidateCode {
        // 图片的宽度。
        private int width = 160;
        // 图片的高度。
        private int height = 40;
        // 验证码字符个数
        private int codeCount = 5;
        // 验证码干扰线数
        private int lineCount = 150;
        // 验证码
        private String code = null;
        // 验证码图片Buffer
        private BufferedImage buffImg = null;

        private char[] codeSequence = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
                'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
                'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

        /**
         * @param width     图片宽
         * @param height    图片高
         * @param codeCount 字符个数
         * @param lineCount 干扰线条数
         */
        ValidateCode(int width, int height, int codeCount, int lineCount) {
            this.width = width;
            this.height = height;
            this.codeCount = codeCount;
            this.lineCount = lineCount;
            this.createCode();
        }

        void createCode() {
            int red;
            int green;
            int blue;

            int x = width / (codeCount + 2);
            int codeY = height - 4;

            buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = buffImg.createGraphics();
            Random random = new Random();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, width, height);
            Font font = new Font(null, 0, 32);
            g.setFont(font);

            for (int i = 0; i < lineCount; i++) {
                int xs = random.nextInt(width);
                int ys = random.nextInt(height);
                int xe = xs + random.nextInt(width / 8);
                int ye = ys + random.nextInt(height / 8);
                red = random.nextInt(255);
                green = random.nextInt(255);
                blue = random.nextInt(255);
                g.setColor(new Color(red, green, blue));
                g.drawLine(xs, ys, xe, ye);
            }

            StringBuilder randomCode = new StringBuilder();
            for (int i = 0; i < codeCount; i++) {
                String strRand = String.valueOf(codeSequence[random.nextInt(codeSequence.length)]);
                red = random.nextInt(255);
                green = random.nextInt(255);
                blue = random.nextInt(255);
                g.setColor(new Color(red, green, blue));
                g.drawString(strRand, (i + 1) * x, codeY);
                randomCode.append(strRand);
            }
            code = randomCode.toString();
        }

        public void write(OutputStream sos) throws IOException {
            ImageIO.write(buffImg, "png", sos);
        }

        public String getCode() {
            return code;
        }

    }



}
