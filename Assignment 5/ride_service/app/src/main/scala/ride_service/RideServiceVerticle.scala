package ride_service

import io.vertx.circuitbreaker.{CircuitBreaker, CircuitBreakerOptions}
import io.vertx.core.http.{HttpMethod, HttpServer}
import io.vertx.core.json.JsonObject
import io.vertx.core.{AbstractVerticle, AsyncResult, Promise}
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.{BodyHandler, SessionHandler}
import io.vertx.ext.web.sstore.LocalSessionStore
import ride_service.db.{IDbClient, MongoDbClient}
import ride_service.handlers.{CreatePageHandler, CreateRideHandler, DeleteRideHandler, GetRidesHandler, HomeHandler}

class RideServiceVerticle extends AbstractVerticle{

  override def start(startPromise: Promise[Void]): Unit = {
    try{
      val config = new JsonObject()
        .put("connection_string", "mongodb://localhost:27017")
        .put("db_name", "Ride_Service")

      val mongoClient = MongoClient.createShared(vertx, config)
      val databaseClient: IDbClient = new MongoDbClient(mongoClient)

      // Initialize the CircuitBreaker
      val circuitBreaker = CircuitBreaker.create("db-circuit-breaker", vertx, new CircuitBreakerOptions())

      // Create a router with all routes
      val router = Router.router(vertx)
      router.route().handler(BodyHandler.create()) // Aggiungo questo handler per poter leggere il body delle richieste
      router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))) // Aggiungo questo handler per gestire le sessioni

      // Rotta per visualizzare la dashboard
      router.route(HttpMethod.GET, "/api/rides/dashboard").handler(new HomeHandler().handle _)

      //Rotta per visualizzare la pagina per creare una ride
      router.route(HttpMethod.GET, "/api/rides/create-form").handler(new CreatePageHandler(circuitBreaker).handle _)

      //Rotta per visualizzare la pagina delle rides popolandola
      router.route(HttpMethod.GET, "/api/rides/get_rides").handler(new GetRidesHandler(databaseClient, circuitBreaker).handle _)

      //Rotta per creare una ride
      router.route(HttpMethod.POST, "/api/rides/create_ride").handler(new CreateRideHandler(databaseClient, circuitBreaker).handle _)

      //Rotta per eliminare una ride
      router.route(HttpMethod.DELETE, "/api/rides/:id").handler(new DeleteRideHandler(databaseClient, circuitBreaker).handle _)

      // Create a http server with the router
      vertx.createHttpServer().requestHandler(router).listen(8081, (http: AsyncResult[HttpServer]) => {
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8081")
        } else {
          startPromise.fail(http.cause())
        }
      })
    } catch {
      case e: Exception =>
        startPromise.fail(e)
        System.err.println("Exception during start: " + e.getMessage)
    }
  }

  override def stop(stopPromise: Promise[Void]): Unit = {
    try {
      super.stop(stopPromise)
    } catch {
      case e: Exception =>
        stopPromise.fail(e)
    }
  }
}
