package user_service.db;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface IDbClient {
    void findOne(String collection, JsonObject query, JsonObject fields, Handler<AsyncResult<JsonObject>> result);

    void save(String collection, JsonObject document, Handler<AsyncResult<String>> result);
}
