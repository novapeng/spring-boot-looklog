package novayoung.log;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LookLog Configuration
 */
@Component
@ConfigurationProperties(prefix = "lookLog")
public class LookLogConfig {

    /**
     * Cache log isDisable
     */
    private boolean enable = false;



    /**
     * Log cached time, Unit is second.
     *
     * This item is used to store exception, u can lookup exception by http api.
     *
     */
    private Long cachedSecond = 60 * 60 * 24L;


    /**
     * The Key Prefix
     */
    private String keyPrefix = "cachelog_key_";


    /**
     * LookLog MxLimit
     */
    private int lookLogMaxLimit = 5000;


    /**
     * The Buffer Size
     */
    private Integer queueSize = 10000;


    /**
     * The Host Of CacheLog Redis
     */
    private String redisHost;


    /**
     * The Port Of CacheLog Redis
     */
    private Integer redisPort;


    /**
     * The Password Of CacheLog Redis
     */
    private String redisPassword;


    /**
     * The Connection Timeout Of CacheLog Redis
     */
    private Integer redisTimeout;


    /**
     * The DataBase Timeout Of CacheLog Redis
     */
    private Integer redisDatabase;

    /**
     * Max Number Of "active" Connections In The RedisPool
     */
    private Integer redisPoolMaxActive;


    /**
     * Max Number Of "idle" Connections In The RedisPool
     */
    private Integer redisPoolMaxIdle;


    /**
     * Min Number Of "idle" Connections In The RedisPool
     */
    private Integer redisPoolMinIdle;

    /**
     * Max Number Of "WaitTime" In The RedisPool
     */
    private Integer redisPoolMaxWait;


    /**
     * The Table Name Of CacheLog MongoDb
     */
    private String mongoDbCollectionName;


    /**
     * The Cache
     */
    private String mongoDbTmpCollectionName = "looklog";


    /**
     * Tmp value cached time, Unit is second.
     *
     */
    private Long tmpCachedSecond = 60 * 60 * 8L;



    /**
     * Security
     */
    private boolean security = true;


    /**
     * Password
     */
    private String password;


    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Long getCachedSecond() {
        return cachedSecond;
    }

    public void setCachedSecond(Long cachedSecond) {
        this.cachedSecond = cachedSecond;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public String getRedisHost() {
        return redisHost;
    }

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public Integer getRedisPort() {
        return redisPort;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public String getRedisPassword() {
        return redisPassword;
    }

    public void setRedisPassword(String redisPassword) {
        this.redisPassword = redisPassword;
    }

    public Integer getRedisTimeout() {
        return redisTimeout;
    }

    public void setRedisTimeout(Integer redisTimeout) {
        this.redisTimeout = redisTimeout;
    }

    public Integer getRedisDatabase() {
        return redisDatabase;
    }

    public void setRedisDatabase(Integer redisDatabase) {
        this.redisDatabase = redisDatabase;
    }

    public Integer getRedisPoolMaxActive() {
        return redisPoolMaxActive;
    }

    public void setRedisPoolMaxActive(Integer redisPoolMaxActive) {
        this.redisPoolMaxActive = redisPoolMaxActive;
    }

    public Integer getRedisPoolMaxIdle() {
        return redisPoolMaxIdle;
    }

    public void setRedisPoolMaxIdle(Integer redisPoolMaxIdle) {
        this.redisPoolMaxIdle = redisPoolMaxIdle;
    }

    public Integer getRedisPoolMinIdle() {
        return redisPoolMinIdle;
    }

    public void setRedisPoolMinIdle(Integer redisPoolMinIdle) {
        this.redisPoolMinIdle = redisPoolMinIdle;
    }

    public Integer getRedisPoolMaxWait() {
        return redisPoolMaxWait;
    }

    public void setRedisPoolMaxWait(Integer redisPoolMaxWait) {
        this.redisPoolMaxWait = redisPoolMaxWait;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(Integer queueSize) {
        this.queueSize = queueSize;
    }

    public String getMongoDbCollectionName() {
        return mongoDbCollectionName;
    }

    public void setMongoDbCollectionName(String mongoDbCollectionName) {
        this.mongoDbCollectionName = mongoDbCollectionName;
    }

    public String getMongoDbTmpCollectionName() {
        return mongoDbTmpCollectionName;
    }

    public void setMongoDbTmpCollectionName(String mongoDbTmpCollectionName) {
        this.mongoDbTmpCollectionName = mongoDbTmpCollectionName;
    }

    public Long getTmpCachedSecond() {
        return tmpCachedSecond;
    }

    public void setTmpCachedSecond(Long tmpCachedSecond) {
        this.tmpCachedSecond = tmpCachedSecond;
    }

    public int getLookLogMaxLimit() {
        return lookLogMaxLimit;
    }

    public void setLookLogMaxLimit(int lookLogMaxLimit) {
        this.lookLogMaxLimit = lookLogMaxLimit;
    }

    public boolean isSecurity() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
