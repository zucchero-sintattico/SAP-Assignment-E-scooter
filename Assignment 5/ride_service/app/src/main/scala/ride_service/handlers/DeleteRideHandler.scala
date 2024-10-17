package ride_service.handlers

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.Json
import io.vertx.ext.web.RoutingContext
import ride_service.db.IDbClient

class DeleteRideHandler(databaseClient: IDbClient, circuitBreaker: CircuitBreaker) extends IHandler {

  override def handle(routingContext: RoutingContext): Unit = {
    println("Received request for get rides")

    val rideId = routingContext.request().getParam("id")
    println("Ride ID: " + rideId)

    if (rideId == null || rideId.isEmpty) {
      routingContext.response()
        .setStatusCode(400)
        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
        .end("Ride ID is required")
      return
    }

    circuitBreaker.execute[Void]({ promise =>
      // Logica per eliminare la ride dal database
      databaseClient.deleteById("rides", rideId, { result =>
        if (result.succeeded()) {
          promise.complete()
        } else {
          promise.fail("Failed to delete ride")
        }
      })
    }).onComplete({ ar =>
      if (ar.succeeded()) {
        routingContext.response().setStatusCode(200).putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(Json.encode("Ride deleted successfully"))
      } else {
        routingContext.response().setStatusCode(500).putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end("Failed to delete ride")
      }
    })

  }
}
