package novayoung.log.handler;

import novayoung.log.LookLogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class RedisCacheOperator implements CacheOperator {

    @Autowired
    private LookLogConfig lookLogConfig;

    private JedisPool jedisPool;

    @Override
    public void init() {

        if (lookLogConfig.getRedisHost() == null || "".equals(lookLogConfig.getRedisHost())) {
            return;
        }

        /**
         * Build JedisPool !
         */
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(lookLogConfig.getRedisPoolMaxActive());
        jedisPoolConfig.setMaxIdle(lookLogConfig.getRedisPoolMaxIdle());
        jedisPoolConfig.setMinIdle(lookLogConfig.getRedisPoolMinIdle());
        jedisPoolConfig.setMaxWaitMillis(lookLogConfig.getRedisPoolMaxWait());

        if (lookLogConfig.getRedisPassword() == null || "".equals(lookLogConfig.getRedisPassword().trim())) {
            jedisPool = new JedisPool(
                    jedisPoolConfig,
                    lookLogConfig.getRedisHost(),
                    lookLogConfig.getRedisPort(),
                    lookLogConfig.getRedisTimeout());

        } else {
            jedisPool = new JedisPool(
                    jedisPoolConfig,
                    lookLogConfig.getRedisHost(),
                    lookLogConfig.getRedisPort(),
                    lookLogConfig.getRedisTimeout(),
                    lookLogConfig.getRedisPassword());
        }



        /**
         * Test Jedis Connection !
         */
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "lookLogApender#OnlyTestKey123";
            jedis.set(key, "1");
            jedis.del(key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

    }

    @Override
    public boolean isEnable() {
        return lookLogConfig.getRedisHost() == null || "".equals(lookLogConfig.getRedisHost().trim());
    }

    @Override
    public void putLog(LogDto logDto) {

        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();

            String key = lookLogConfig.getKeyPrefix() + logDto.getTraceId();

            Long len = jedis.rpush(key, logDto.getFormattedMessage());

            if (len == 1L && lookLogConfig.getCachedSecond() != null && lookLogConfig.getCachedSecond().intValue() > 0) {
                jedis.expire(key, lookLogConfig.getCachedSecond().intValue());
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public List<String> getLogs(String traceId) {
        Jedis jedis = null;

        try {

            jedis = jedisPool.getResource();

            String key = lookLogConfig.getKeyPrefix() + traceId;

            Long len = jedis.llen(key);

            if (len == null || len == 0) {
                return new ArrayList<>();
            }

            return jedis.lrange(key, 0, -1);

        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    @Override
    public List<String> getLogs(Map<String, Object> conditions, Integer order, Integer limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        if (jedisPool != null) {
            jedisPool.destroy();
        }
    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void set(String key, Object value) {

    }
}