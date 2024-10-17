package ride_service.handlers

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import io.vertx.core.Promise

import scala.io.Source

class CreatePageHandler(private val circuitBreaker: CircuitBreaker) extends IHandler {


  override def handle(routingContext: RoutingContext): Unit = {
    println("Received request for create page ride")

    circuitBreaker.execute[Void]((promise: Promise[Void]) => {
      try {
        val inputStream = getClass.getResourceAsStream("/create_ride.html")
        val fileContent = if (inputStream != null) {
          Source.fromInputStream(inputStream).mkString
        } else {
          // Gestisci il caso in cui il file non viene trovato
          promise.fail(new Exception("HTML file not found"))
          return // Esci dal blocco
        }

        // Restituisci il contenuto del file HTML nella risposta
        routingContext.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
          .end(fileContent)

        promise.complete() // Completa la promessa
      } catch {
        case e: Exception => promise.fail(e) // Fallisce la promessa in caso di eccezione
      }
    }).onComplete { ar =>
      if (ar.failed()) {
        routingContext.response()
          .setStatusCode(500)
          .end("Internal Server Error")
        println(s"Failed to read HTML file.")
        println(s"Failed to handle GET request: ${ar.cause()}")
      }
    }
  }
}
