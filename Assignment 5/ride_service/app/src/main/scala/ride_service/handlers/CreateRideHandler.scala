package ride_service.handlers

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.Vertx.vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.{Json, JsonObject}
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import org.springframework.http.HttpStatus
import ride_service.db.IDbClient

class CreateRideHandler(databaseClient: IDbClient, circuitBreaker: CircuitBreaker) extends IHandler {

  private val webClient: WebClient = WebClient.create(vertx)

  override def handle(routingContext: RoutingContext): Unit = {
    // Recupera il parametro "scooterId" dal form
    val scooterId = routingContext.request().getParam("scooterId")

    if (scooterId == null || scooterId.isEmpty) {
      routingContext.response()
        .setStatusCode(400)
        .end("Scooter ID is required")
      return
    }

    val startLocation = routingContext.request().getParam("startLocation")
    val endLocation = routingContext.request().getParam("endLocation")
    val startTime = routingContext.request().getParam("startTime")
    val endTime = routingContext.request().getParam("endTime")

    // Crea un JsonObject con i dati della ride
    val rideJson = new JsonObject()
      .put("scooterId", scooterId)
      .put("startLocation", startLocation)
      .put("endLocation", endLocation)
      .put("startTime", startTime)
      .put("endTime", endTime)

    // Prepara l'URI per la richiesta al servizio di manutenzione
    val uri = s"http://localhost:8080/api/management/use_scooter/$scooterId"

    // Crea i headers della richiesta
    routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
    routingContext.response().putHeader(HttpHeaders.ORIGIN, "http://localhost:8080")

    // Usa il Circuit Breaker per proteggere la chiamata
    circuitBreaker.execute[Void]({ promise =>
      // Effettua una richiesta HTTP PUT usando Vertx WebClient
      webClient.putAbs(uri)
        .send({ ar =>
          if (ar.succeeded()) {
            val response = ar.result()
            if (response.statusCode() == 200) {
              // Se la richiesta ha successo, crea la nuova ride
              databaseClient.save("rides", rideJson, { res =>
                if (res.succeeded()) {
                  promise.complete()
                } else {
                  promise.fail("Failed to create ride")
                }
              })
            } else {
              promise.fail("Maintenance service error")
            }
          } else {
            promise.fail("Request to maintenance service failed")
          }
        })
    }).onComplete(ar => {
      if (ar.succeeded()) {
        routingContext.response()
          .setStatusCode(200)
          .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
          .end(Json.encode("Ride created successfully!"))
      } else {
        routingContext.response().setStatusCode(503).putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end("Error: " + ar.cause().getMessage)
      }
    })
  }
}
