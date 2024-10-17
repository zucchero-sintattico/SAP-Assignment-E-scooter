package ride_service.handlers

import io.vertx.core.http.{Cookie, HttpHeaders, HttpServerRequest}
import io.vertx.ext.web.RoutingContext

import scala.io.Source

class HomeHandler extends IHandler {

  override def handle(routingContext: RoutingContext): Unit = {
    println("Received request for home page ride")

    val inputStream = getClass.getResourceAsStream("/home.html")
    if (inputStream != null) {
      val fileContent = Source.fromInputStream(inputStream).getLines().mkString("\n")

      val request: HttpServerRequest = routingContext.request()
      val emailCookie: Option[Cookie] = Option(request.getCookie("email"))

      val updatedFileContent = emailCookie match {
        case Some(cookie) =>
          val email = cookie.getValue
          fileContent.replace("Welcome to the Rides Service!", s"Welcome to the Rides Service, $email!")
        case None => fileContent
      }

      routingContext.response().setStatusCode(200).putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(updatedFileContent)
    } else {
      // Gestisce il caso in cui la pagina non venga trovata
      routingContext.response()
        .setStatusCode(404)
        .end("Page not found")
    }

  }

}
