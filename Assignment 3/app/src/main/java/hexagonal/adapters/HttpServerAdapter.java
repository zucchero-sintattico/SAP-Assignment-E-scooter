package hexagonal.adapters;

import hexagonal.domain.services.EScooterService;
import hexagonal.domain.services.RideService;
import hexagonal.domain.services.UserService;
import hexagonal.ports.ServerPort;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServerAdapter extends AbstractVerticle implements ServerPort {

    static Logger LOGGER = Logger.getLogger("[EScooter Server]");
    private final int port;
    private final UserService userService;
    private final EScooterService escooterService;
    private final RideService rideService;

    public HttpServerAdapter(int port, UserService userService, EScooterService escooterService, RideService rideService) {
        this.port = port;
        this.userService = userService;
        this.escooterService = escooterService;
        this.rideService = rideService;
        LOGGER.setLevel(Level.INFO);
    }

    public void start() {
        LOGGER.log(Level.INFO, "EScooterMan server initializing...");
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        /* static files by default searched in "webroot" directory */
        router.route("/static/*").handler(StaticHandler.create().setCachingEnabled(false));
        router.route().handler(BodyHandler.create());
        router.route().handler(CorsHandler.create()
                .addOrigin("*")
                .allowedMethod(io.vertx.core.http.HttpMethod.GET)
                .allowedMethod(io.vertx.core.http.HttpMethod.POST)
                .allowedHeader("Access-Control-Allow-Method")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedHeader("Content-Type"));

        // manage the root
        router.route(HttpMethod.GET, "/api/dashboard").handler(this::getDashboard);
        router.route(HttpMethod.POST, "/api/users").handler(this::registerNewUser);
        router.route(HttpMethod.GET, "/api/users/:userId").handler(this::getUserInfo);
        router.route(HttpMethod.POST, "/api/escooters").handler(this::registerNewEScooter);
        router.route(HttpMethod.GET, "/api/escooters/:escooterId").handler(this::getEScooterInfo);
        router.route(HttpMethod.POST, "/api/rides").handler(this::startNewRide);
        router.route(HttpMethod.GET, "/api/rides/ongoing_dashboard").handler(this::getOngoingRides);
        router.route(HttpMethod.GET, "/api/rides/ongoing").handler(this::getOngoingRidesCount);
        router.route(HttpMethod.GET, "/api/rides/:rideId").handler(this::getRideInfo);
        router.route(HttpMethod.POST, "/api/rides/:rideId/end").handler(this::endRide);

        server.requestHandler(router).listen(port);

        LOGGER.log(Level.INFO, "EScooterMan server ready - port: " + port);
    }

    public void stop() {
        LOGGER.log(Level.INFO, "EScooterMan server stopped");
        vertx.close();
    }

    private void getOngoingRidesCount(RoutingContext routingContext) {
        LOGGER.log(Level.INFO, "New ongoing rides count request: " + routingContext.currentRoute().getPath());
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json");

        rideService.getNumberOfOngoingRides().onSuccess(ongoingRidesCount -> {
            JsonObject reply = new JsonObject();
            reply.put("result", "ok");
            reply.put("numberOfOngoingRides", ongoingRidesCount);
            response.end(reply.toString());
        }).onFailure(throwable -> {
            JsonObject reply = new JsonObject();
            reply.put("result", "error");
            reply.put("message", throwable.getMessage());
            response.end(reply.toString());
        });
    }

    private void getOngoingRides(RoutingContext routingContext) {
        LOGGER.log(Level.INFO, "New ongoing rides request: " + routingContext.currentRoute().getPath());
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/html");

        vertx.fileSystem().readFile("resources/webroot/ongoing_rides.html", result -> {
            if (result.succeeded()) {
                response.end(result.result());
            } else {
                LOGGER.log(Level.SEVERE, "Failed to read file", result.cause());
                response.setStatusCode(500).end();
            }
        });
    }

    private void getDashboard(RoutingContext routingContext) {
        LOGGER.log(Level.INFO, "New dashboard request: " + routingContext.currentRoute().getPath());
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/html");

        vertx.fileSystem().readFile("resources/webroot/ride-dashboard.html", result -> {
            if (result.succeeded()) {
                response.end(result.result());
            } else {
                LOGGER.log(Level.SEVERE, "Failed to read file", result.cause());
                response.setStatusCode(500).end();
            }
        });
    }

    private void registerNewUser(RoutingContext context) {
        LOGGER.log(Level.INFO, "New user request: " + context.currentRoute().getPath());
        JsonObject userInfo = context.body().asJsonObject();
        String id = userInfo.getString("id");
        String name = userInfo.getString("name");
        String surname = userInfo.getString("surname");

        JsonObject reply = new JsonObject();
        try {
            userService.registerNewUser(id, name, surname);
            reply.put("result", "ok");
        } catch (Exception ex) {
            reply.put("result", "user-id-already-existing");
        }
        sendReply(context, reply);
    }

    protected void getUserInfo(RoutingContext context) {
        LOGGER.log(Level.INFO, "Get user info request: " + context.currentRoute().getPath());
        String userId = context.pathParam("userId");
        JsonObject reply = new JsonObject();
        try {
            String info = userService.getUserInfo(userId);
            reply.put("result", "ok");
            reply.put("user", info);
        } catch (Exception ex) {
            reply.put("result", "user-not-found");
        }
        sendReply(context, reply);
    }

    protected void registerNewEScooter(RoutingContext context) {
        JsonObject escooterInfo = context.body().asJsonObject();
        String id = escooterInfo.getString("id");

        JsonObject reply = new JsonObject();
        try {
            LOGGER.info("Attempting to register new e-scooter with id: " + id);
            escooterService.registerNewEScooter(id);
            reply.put("result", "ok");
            LOGGER.info("E-scooter registered successfully with id: " + id);
        } catch (Exception ex) {
            reply.put("result", "escooter-id-already-existing");
            LOGGER.severe("Failed to register new e-scooter with id: " + id + ". Error: " + ex.getMessage());
        }
        sendReply(context, reply);
    }

    protected void getEScooterInfo(RoutingContext context) {
        LOGGER.log(Level.INFO, "Get escooter info request: " + context.currentRoute().getPath());
        String escooterId = context.pathParam("escooterId");
        JsonObject reply = new JsonObject();
        try {
            String info = escooterService.getEScooterInfo(escooterId);
            reply.put("result", "ok");
            reply.put("escooter", info);
        } catch (Exception ex) {
            reply.put("result", "escooter-not-found");
        }
        sendReply(context, reply);
    }

    protected void startNewRide(RoutingContext context) {
        LOGGER.log(Level.INFO, "Start new ride request: " + context.currentRoute().getPath());
        JsonObject rideInfo = context.body().asJsonObject();
        String userId = rideInfo.getString("userId");
        String escooterId = rideInfo.getString("escooterId");

        JsonObject reply = new JsonObject();
        try {
            if (!userService.userExists(userId)) {
                reply.put("result", "user-does-not-exist");
                LOGGER.severe("Failed to start new ride because user does not exist: " + userId);
                sendReply(context, reply);
                return;
            }

            if (!escooterService.escooterExists(escooterId)) {
                reply.put("result", "escooter-does-not-exist");
                LOGGER.severe("Failed to start new ride because e-scooter does not exist: " + escooterId);
                sendReply(context, reply);
                return;
            }

            String rideId = rideService.startNewRide(userId, escooterId);
            reply.put("result", "ok");
            reply.put("rideId", rideId);
            LOGGER.info("Ride started successfully with rideId: " + rideId);
        } catch (Exception ex) {
            reply.put("result", "start-new-ride-failed");
            LOGGER.severe("Failed to start new ride for user: " + userId + " with e-scooter: " + escooterId + ". Error: " + ex.getMessage());
        }
        sendReply(context, reply);
    }

    protected void getRideInfo(RoutingContext context) {
        LOGGER.log(Level.INFO, "Get ride info request: " + context.currentRoute().getPath());
        String rideId = context.pathParam("rideId");
        JsonObject reply = new JsonObject();

        rideService.getRideInfo(rideId).onSuccess(info -> {
            reply.put("result", "ok");
            reply.put("ride", info);
            sendReply(context, reply);
        }).onFailure(ex -> {
            reply.put("result", "ride-not-found");
            sendReply(context, reply);
        });
    }

    protected void endRide(RoutingContext context) {
        String rideId = context.pathParam("rideId");
        LOGGER.info("Received request to end ride with ID: " + rideId);
        JsonObject reply = new JsonObject();
        try {
            rideService.endRide(rideId);
            reply.put("result", "ok");
            LOGGER.info("Successfully ended ride with ID: " + rideId);
        } catch (Exception ex) {
            reply.put("result", "ride-already-ended");
            LOGGER.warning("Attempted to end ride with ID: " + rideId + ", but it was already ended");
        }
        sendReply(context, reply);
    }

    private void sendReply(RoutingContext request, JsonObject reply) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        response.end(reply.toString());
    }
}
