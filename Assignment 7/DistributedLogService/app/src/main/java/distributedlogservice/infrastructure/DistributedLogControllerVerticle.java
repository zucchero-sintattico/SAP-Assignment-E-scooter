/*
package distributedlogservice.infrastructure;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistributedLogControllerVerticle extends AbstractVerticle {

    public final int port;
    static Logger logger = Logger.getLogger("[Distributed Log]");
    private final List<String> logMessagesList = new ArrayList<>(); // Lista di Log Messages.

    public DistributedLogControllerVerticle(int port) {
        this.port = port;
        logger.setLevel(Level.INFO);
    }

    public void start() {
        logger.log(Level.INFO, "Distributed Log  initializing...");

        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        router.route(HttpMethod.GET, "/api/log").handler(this::sendLogsList);
        router.route(HttpMethod.POST, "/api/log").handler(this::printLogMessage);

        server.requestHandler(router).listen(port);
        logger.log(Level.INFO, "Distributed Log  - port: " + port);
    }

    private void printLogMessage(RoutingContext routingContext) {
        routingContext.request().bodyHandler(buffer -> {
            String message = buffer.toString();
            logMessagesList.add(message);
            System.out.println("Message received: " + message);

            // Invia una risposta al client
            JsonObject responseJson = new JsonObject().put("status", "Message received successfully");
            sendReply(routingContext.response(), responseJson);
        });
    }

    private void sendLogsList(RoutingContext routingContext) {
        JsonArray logsArray = new JsonArray();

        for (String message : logMessagesList) {
            JsonObject logJson = new JsonObject().put("content:", message);
            logsArray.add(logJson);
        }

        JsonObject logsJson = new JsonObject().put("logs", logsArray);
        String prettyLogs = logsJson.encodePrettily();
        routingContext.response().end(prettyLogs);
    }

    private void sendReply(HttpServerResponse response, JsonObject reply) {
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }
}
*/
package distributedlogservice.infrastructure;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistributedLogControllerVerticle extends AbstractVerticle {

    private final int port;
    private static final Logger logger = Logger.getLogger("[Distributed Log]");
    private final List<String> logMessagesList = new ArrayList<>(); // Lista di messaggi di log.

    public DistributedLogControllerVerticle(int port) {
        this.port = port;
        logger.setLevel(Level.INFO);
    }

    @Override
    public void start() {
        logger.log(Level.INFO, "Distributed Log Service avviato sulla porta: " + port);

        // Configura il server e le rotte
        Router router = Router.router(vertx);
        router.route(HttpMethod.GET, "/api/log").handler(this::sendLogsList);  // API GET per visualizzare i log
        router.route(HttpMethod.POST, "/api/log").handler(this::printLogMessage);  // API POST per ricevere i log

        vertx.createHttpServer().requestHandler(router).listen(port);
    }

    // Gestore POST: registra un nuovo log
    private void printLogMessage(RoutingContext context) {
        context.request().bodyHandler(buffer -> {
            String message = buffer.toString();
            logMessagesList.add(message);
            logger.log(Level.INFO, "Messaggio ricevuto: " + message);
            sendReply(context, new JsonObject().put("status", "Messaggio ricevuto con successo"));
        });
    }

    // Gestore GET: restituisce i log salvati in formato JSON
    private void sendLogsList(RoutingContext context) {
        JsonArray logsArray = new JsonArray(logMessagesList.stream()
                .map(msg -> new JsonObject().put("content", msg)).toList());
        context.response().putHeader("content-type", "application/json").end(new JsonObject().put("logs", logsArray).encodePrettily());
    }

    // Helper per inviare una risposta JSON
    private void sendReply(RoutingContext context, JsonObject reply) {
        context.response().putHeader("content-type", "application/json").end(reply.encode());
    }
}
