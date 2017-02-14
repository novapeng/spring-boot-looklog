package novayoung.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import novayoung.log.handler.CacheLogHandler;
import novayoung.log.handler.LogDto;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 */
public class LookLogAppender extends OutputStreamAppender<ILoggingEvent> {

    private static final String SPLIT = " \\~\\|--_--\\|\\~ ";

    private static ApplicationContext applicationContext;

    private static volatile boolean started = false;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        LookLogAppender.applicationContext = applicationContext;
    }

    public static void setStarted(boolean started) {
        LookLogAppender.started = started;
    }

    @Override
    public void start() {

        setOutputStream(new BufferedOutputStream(new ByteArrayOutputStream()){

            @Override
            public void write(byte[] b) throws IOException {

                /**
                 * If Bytes Is Null Or Empty, Do Nothing !
                 */
                if (b == null || b.length == 0) {
                    return;
                }


                /**
                 * If Spring Is Not Initialized, Do Nothing !
                 *
                 */
                if (applicationContext == null || !started) {
                    return;
                }


                /**
                 * If Handler Is Not Found In Spring Beans, Do Nothing !
                 */
                CacheLogHandler cacheLogHandler = applicationContext.getBean(CacheLogHandler.class);
                if (cacheLogHandler == null || cacheLogHandler.isDisable()) {
                    return;
                }


                /**
                 * Invoke By Handler ...
                 */
                String formattedMessage = new String(b, "UTF-8");
                cacheLogHandler.handle(parse(formattedMessage));
            }
        });

        super.start();
    }


    /**
     * Parse The Log Message To LogDto
     *
     * @param formattedMessage
     *        Full logMessage, @see the pattern in logback.config.xml
     *
     * @return LogDto
     */
    private LogDto parse(String formattedMessage) {

        String[] arr = formattedMessage.split(SPLIT);

        if (arr.length != 8) {
            return null;
        }

        LogDto logDto = new LogDto();
        logDto.setFormattedMessage(StringUtils.arrayToDelimitedString(arr, " "));
        logDto.setTime(arr[0]);
        logDto.setThread(arr[1]);
        logDto.setTraceId(arr[2]);
        logDto.setLogLevel(arr[3] == null ? null : arr[3].trim());
        logDto.setLoggerName(arr[4]);
        logDto.setLine(arr[5]);
        logDto.setMessage(arr[7]);
        return logDto;
    }


}
