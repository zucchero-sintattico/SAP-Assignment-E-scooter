package user_service.handlers;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import user_service.db.IDbClient;
import user_service.entities.User;

public class RegisterHandler implements IHandler {
    private final IDbClient databaseClient;
    private final CircuitBreaker circuitBreaker;

    public RegisterHandler(IDbClient databaseClient, CircuitBreaker circuitBreaker) {
        this.databaseClient = databaseClient;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        User user = getFormAttributes(routingContext);

        JsonObject userJson = new JsonObject()
                .put("name", user.getName())
                .put("email", user.getEmail())
                .put("password", user.getPassword())
                .put("is_maintainer", user.isMaintainer());

        this.circuitBreaker.execute(promise -> {
            this.databaseClient.save("users", userJson, ar -> {
                if (ar.succeeded()) {
                    promise.complete();
                } else {
                    promise.fail(ar.cause());
                }
            });
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).end("Registration successful!");
            } else {
                routingContext.fail(ar.cause());
            }
        });
    }

    private User getFormAttributes(RoutingContext routingContext) {
        boolean isMaintainer = "true".equals(routingContext.request().getParam("maintainer"));
        return new User(routingContext.request().getParam("name"),
                routingContext.request().getParam("email"),
                routingContext.request().getParam("password"),
                isMaintainer);
    }
}
