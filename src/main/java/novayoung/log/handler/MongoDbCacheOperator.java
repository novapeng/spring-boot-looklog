package novayoung.log.handler;

import com.mongodb.*;
import novayoung.log.LookLogConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 *
 */
@Component()
public class MongoDbCacheOperator implements CacheOperator {

    @Autowired
    private LookLogConfig lookLogConfig;

    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public void init() {

        createLogCollectionIndex();

        createCacheCollectionIndex();

    }


    /**
     * Cache Collection Index
     */
    private void createCacheCollectionIndex() {

        DBCollection tmpCollection = mongoTemplate.getCollection(lookLogConfig.getMongoDbTmpCollectionName());

        if (lookLogConfig.getTmpCachedSecond() != null && lookLogConfig.getTmpCachedSecond() > 0) {

            List<DBObject> indexs = tmpCollection.getIndexInfo();

            String indexFiledName = "createTime";
            String indexName = "tmp_createTime_expire";

            for (DBObject dbObject : indexs) {
                if (indexName.equals(dbObject.get("name"))) {
                    return;
                }
            }

            BasicDBObject options = new BasicDBObject("name", indexName);
            options.append("expireAfterSeconds", lookLogConfig.getTmpCachedSecond());

            tmpCollection.createIndex(new BasicDBObject(indexFiledName, 1), options);
        }

    }


    /**
     * Log Collection Index
     */
    private void createLogCollectionIndex() {

        DBCollection collection = mongoTemplate.getCollection(lookLogConfig.getMongoDbCollectionName());

        if (lookLogConfig.getCachedSecond() != null && lookLogConfig.getCachedSecond() > 0) {

            List<DBObject> indexs = collection.getIndexInfo();

            String indexFiledName = "createTime";
            String indexName = "createTime_expire";

            for (DBObject dbObject : indexs) {
                if (indexName.equals(dbObject.get("name"))) {
                    return;
                }
            }

            BasicDBObject options = new BasicDBObject("name", indexName);
            options.append("expireAfterSeconds", lookLogConfig.getCachedSecond());

            collection.createIndex(new BasicDBObject(indexFiledName, 1), options);
        }

    }

    @Override
    public boolean isEnable() {
        return isNotBlank(lookLogConfig.getMongoDbCollectionName());
    }

    @Override
    public void putLog(LogDto logDto) {

        mongoTemplate.insert(logDto, lookLogConfig.getMongoDbCollectionName());

    }

    @Override
    public List<String> getLogs(String traceId) {


        List<String> list = new ArrayList<>();

        DBCollection collection = mongoTemplate.getCollection(lookLogConfig.getMongoDbCollectionName());

        DBCursor dbCursor;

        if (isNotBlank(traceId)) {

            Pattern pattern = Pattern.compile("^.*" + traceId + ".*$", Pattern.CASE_INSENSITIVE);

            dbCursor = collection.find(new BasicDBObject("traceId", pattern));

        } else {

            dbCursor = collection.find().sort(new BasicDBObject("createTime", -1));

        }

        dbCursor = dbCursor.limit(lookLogConfig.getLookLogMaxLimit());

        while (dbCursor.hasNext()) {
            DBObject dbObject = dbCursor.next();
            list.add((String) dbObject.get("formattedMessage"));
        }

        return list;
    }

    @Override
    public List<String> getLogs(Map<String, Object> conditions, Integer order, Integer limit) {

        String keyword = (String) conditions.get("keyword");
        String traceId = (String) conditions.get("traceId");
        Date startTime = (Date) conditions.get("startTime");
        Date endTime = (Date) conditions.get("endTime");
        String[] level = (String[]) conditions.get("level");

        List<String> list = new ArrayList<>();

        DBCollection collection = mongoTemplate.getCollection(lookLogConfig.getMongoDbCollectionName());


        BasicDBObject basicDBObject = new BasicDBObject();

        if (isNotBlank(traceId)) {
            basicDBObject.append("traceId", Pattern.compile(traceId));
        }

        if (isNotBlank(keyword)) {
            basicDBObject.append("formattedMessage", Pattern.compile(keyword));
        }

        String createTimeFiledName = "createTime";

        if (startTime != null) {
            basicDBObject.append(createTimeFiledName, new BasicDBObject(QueryOperators.GTE, startTime));
        }

        if (endTime != null) {
            basicDBObject.append(createTimeFiledName, new BasicDBObject(QueryOperators.LTE, endTime));
        }

        if (level != null && level.length > 0) {
            BasicDBList values = new BasicDBList();
            values.addAll(Arrays.asList(level));
            basicDBObject.append("logLevel", new BasicDBObject(QueryOperators.IN, values));
        }


        DBCursor dbCursor;

        if (basicDBObject.isEmpty()) {
            dbCursor = collection.find().sort(new BasicDBObject("createTime", order));
        } else {
            dbCursor = collection.find(basicDBObject).sort(new BasicDBObject("createTime", order));
        }

        dbCursor = dbCursor.limit(limit == null || limit <= 0 ? lookLogConfig.getLookLogMaxLimit() : limit);

        while (dbCursor.hasNext()) {
            DBObject dbObject = dbCursor.next();
            list.add((String) dbObject.get("formattedMessage"));
        }

        return list;
    }

    @Override
    public void destroy() {

        //Do Nothing !

    }

    @Override
    public Object get(String key) {
        DBCollection collection = mongoTemplate.getCollection(lookLogConfig.getMongoDbTmpCollectionName());

        DBCursor dbCursor = collection.find(new BasicDBObject("key", key)).limit(1);

        if (dbCursor.hasNext()) {
            return dbCursor.next().get("value");
        }

        return null;
    }

    @Override
    public void set(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put("createTime", new Date());
        map.put("key", key);
        map.put("value", value);
        mongoTemplate.insert(map, lookLogConfig.getMongoDbTmpCollectionName());
    }

    private boolean isNotBlank(String s) {
        return s != null && !"".equals(s.trim());
    }
}