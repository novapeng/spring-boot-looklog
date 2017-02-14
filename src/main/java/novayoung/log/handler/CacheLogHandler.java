package novayoung.log.handler;

import novayoung.log.LookLogAppender;
import novayoung.log.LookLogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 */
@Component
public class CacheLogHandler {


    private BlockingQueue<LogDto> queue;

    @Autowired
    private LookLogConfig lookLogConfig;

    @Autowired
    private CacheOperator cacheOperator;


    public LookLogConfig getLookLogConfig() {
        return lookLogConfig;
    }

    @PostConstruct
    public void init() {

        /**
         * If UnEnable, Do Nothing !
         */
        if (isDisable()) {
            return;
        }


        /**
         * Initialize Buffer Queue
         */
        queue = new LinkedBlockingQueue<>(lookLogConfig.getQueueSize());


        /**
         * Initialize CacheOperator
         */
        cacheOperator.init();


        /**
         * Start A Thread To Listen Buffer Queue
         */
        listeningQueue();

        /**
         * Set LookLogAppender.started is true
         */
        LookLogAppender.setStarted(true);
    }

    @PreDestroy
    public void destroy() {
        if (cacheOperator != null) {
            cacheOperator.destroy();
        }
    }

    public boolean isDisable() {
        return lookLogConfig == null || cacheOperator == null || !lookLogConfig.isEnable() || !cacheOperator.isEnable();
    }

    private void listeningQueue() {

        if (cacheOperator == null ) {
            return;
        }

        Thread thread = new Thread(){

            @Override
            public void run() {

                //noinspection InfiniteLoopStatement
                while (true) {

                    try {

                        /**
                         * Receive Log Message, Put Into Cache !
                         */
                        writeLog(queue.take());

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();

                    } catch (Exception e) {

                        e.printStackTrace();

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e1) {
                            Thread.currentThread().interrupt();
                        }

                    }

                }

            }
        };

        thread.setName("LookLogAppender-Listen");

        thread.start();

    }

    /**
     * Returns the number of elements in buffer.
     *
     * @return the number of elements in buffer
     */
    public Integer getBufferSize() {
        return this.queue.size();
    }

    List<String> getLogs(String traceId) {

        if (cacheOperator == null || isDisable()) {
            return new ArrayList<>();
        }

        return cacheOperator.getLogs(traceId);

    }

    public List<String> getLogs(Map<String, Object> conditions, Integer order, Integer limit) {
        if (cacheOperator == null || isDisable()) {
            return new ArrayList<>();
        }

        return cacheOperator.getLogs(conditions, order, limit);
    }

    private void writeLog(LogDto logDto) {

        if (logDto.getFormattedMessage() == null || "".equals(logDto.getFormattedMessage().trim()) ||
                cacheOperator == null || isDisable()) {
            return;
        }

        cacheOperator.putLog(logDto);

    }

    public void handle(LogDto logDto) {


        /**
         * If UnEnable, Do Nothing !
         */
        if (isDisable()) {
            return;
        }

        if (logDto == null) {
            return;
        }


        /**
         * Put Log Message Into Queue, If Queue Is Full, Do Nothing !
         */
        queue.offer(logDto);

    }

    public String get(String key) {
        return (String) cacheOperator.get(key);
    }

    public void set(String key, String value) {
        cacheOperator.set(key, value);
    }

}
