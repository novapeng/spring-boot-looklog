package novayoung.log.handler;

import java.util.List;
import java.util.Map;

/**
 *
 */
public interface CacheOperator {

    void init();

    boolean isEnable();

    void putLog(LogDto logDto);

    List<String> getLogs(String traceId);

    List<String> getLogs(Map<String, Object> conditions, Integer order, Integer limit);

    void destroy();

    Object get(String key);

    void set(String key, Object value);

}
