package ride_service.handlers

import io.vertx.circuitbreaker.CircuitBreaker
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import ride_service.db.IDbClient

class GetRidesHandler(private val db: IDbClient, private val circuitBreaker: CircuitBreaker) extends IHandler {

  override def handle(routingContext: RoutingContext): Unit = {
    println("Received request for get rides")

    db.findAll("rides", new JsonObject(), { result =>
      if (result.succeeded()) {
        val rides = result.result()

        val htmlContent = new StringBuilder
        htmlContent.append("<!DOCTYPE html>")
          .append("<html lang=\"en\">")
          .append("<head>")
          .append("<meta charset=\"UTF-8\">")
          .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
          .append("<title>Rides</title>")
          .append("<link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css\">") // Inclusione di Bootstrap
          .append("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js\"></script>") // Inclusione di jQuery
          .append("</head>")
          .append("<body>")
          .append("<div class=\"container mt-5\">") // Contenitore Bootstrap
          .append("<h2>Rides</h2>")
          .append("<button class=\"btn btn-secondary mb-3\" onclick=\"location.href='/api/rides/dashboard'\">Go back to Home</button>")
          .append("<ul class=\"list-group\">") // Lista con stili Bootstrap

        rides.forEach { ride =>
          val startLocation = ride.getString("startLocation")
          val endLocation = ride.getString("endLocation")
          val startTime = ride.getString("startTime")
          val endTime = ride.getString("endTime")
          val rideId = ride.getString("_id")

          htmlContent.append("<li class=\"list-group-item\">") // Elemento della lista
            .append(s"<p><strong>Start Location:</strong> <span>$startLocation</span></p>")
            .append(s"<p><strong>End Location:</strong> <span>$endLocation</span></p>")
            .append(s"<p><strong>Start Time:</strong> <span>$startTime</span></p>")
            .append(s"<p><strong>End Time:</strong> <span>$endTime</span></p>")
            .append(s"""<button class="btn btn-danger delete-ride" data-id="$rideId">Remove Ride</button>""")
            .append("</li>")
        }
        htmlContent.append("</ul>").append("</div>").append("<script>").append(
          """
            $(document).on('click', '.delete-ride', function() {
                var rideId = $(this).data('id'); // Ottieni l'ID della ride da eliminare

                $.ajax({
                    url: '/api/rides/' + rideId, // URL della richiesta DELETE
                    type: 'DELETE',
                    success: function(response) {
                        alert('Ride deleted successfully!');
                        location.reload(); // Ricarica la pagina per aggiornare la lista
                    },
                    error: function(jqXHR, textStatus, errorThrown) {
                        alert('An error occurred: ' + textStatus);
                    }
                });
            });
          """).append("</script>").append("</body>").append("</html>")

        // Invia la risposta HTML al client
        routingContext.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, "text/html")
          .end(htmlContent.toString())
      } else {
        routingContext.response()
          .setStatusCode(500)
          .end("Internal Server Error")
        println(s"Failed to get rides: ${result.cause()}")
      }
    })
  }
}
