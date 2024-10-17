package user_service.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;


public class MongoDbClient implements IDbClient {
    private final MongoClient mongoClient;


    public MongoDbClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public void findOne(String collection, JsonObject query, JsonObject fields, Handler<AsyncResult<JsonObject>> result) {
        this.mongoClient.findOne(collection, query, fields, result);
    }

    @Override
    public void save(String collection, JsonObject document, Handler<AsyncResult<String>> result) {
        this.mongoClient.save(collection, document, result);
    }

}
