package ride_service.db

import io.vertx.core.{AsyncResult, Handler}
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.{MongoClient, MongoClientDeleteResult}

trait IDbClient {
  def findOne(collection: String, query: JsonObject, fields: JsonObject, result: Handler[AsyncResult[JsonObject]]): Unit

  def save(collection: String, document: JsonObject, result: Handler[AsyncResult[String]]): Unit

  def findAll(collection: String, query: JsonObject, result: Handler[AsyncResult[java.util.List[JsonObject]]]): Unit

  def deleteById(collection: String, id: String, result: Handler[AsyncResult[MongoClientDeleteResult]]): Unit
}

class MongoDbClient(private val mongoClient: MongoClient) extends IDbClient {

  override def findOne(collection: String, query: JsonObject, fields: JsonObject, result: Handler[AsyncResult[JsonObject]]): Unit = {
    mongoClient.findOne(collection, query, fields, result)
  }

  override def save(collection: String, document: JsonObject, result: Handler[AsyncResult[String]]): Unit = {
    mongoClient.save(collection, document, result)
  }

  override def findAll(collection: String, query: JsonObject, result: Handler[AsyncResult[java.util.List[JsonObject]]]): Unit = {
    mongoClient.find(collection, query, result)
  }

  def deleteById(collection: String, id: String, result: Handler[AsyncResult[MongoClientDeleteResult]]): Unit = {
    val query = new JsonObject().put("_id", id)
    this.mongoClient.removeDocuments(collection, query, result)
  }


}
