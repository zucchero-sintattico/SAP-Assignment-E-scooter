package user_service.handlers;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.http.Cookie;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import user_service.db.IDbClient;

public class LoginHandler implements IHandler {

    private final IDbClient databaseClient;
    private final CircuitBreaker circuitBreaker;

    public LoginHandler(IDbClient databaseClient, CircuitBreaker circuitBreaker) {
        this.databaseClient = databaseClient;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String email = routingContext.request().getParam("email");
        String password = routingContext.request().getParam("password");

        this.circuitBreaker.execute(promise -> {
            this.databaseClient.findOne("users", new JsonObject().put("email", email), null, ar -> {
                if (ar.succeeded()) {
                    promise.complete(ar.result());
                } else {
                    promise.fail(ar.cause());
                }
            });
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                JsonObject user = (JsonObject) ar.result();
                if (user != null && user.getString("password").equals(password)) {
                    setCookies(routingContext, user);
                    routingContext.response().setStatusCode(200).end("Login successful!");
                } else {
                    routingContext.response().setStatusCode(401).end("Invalid email or password");
                }
            } else {
                routingContext.response().setStatusCode(500).end("Internal server error");
            }
        });
    }

    private void setCookies(RoutingContext routingContext, JsonObject user) {
        Cookie emailCookie = Cookie.cookie("email", user.getString("email")).setMaxAge(86400).setPath("/");
        Boolean isMaintainer = user.getBoolean("is_maintainer");
        Cookie maintainerCookie = Cookie.cookie("isMaintainer", isMaintainer != null ? isMaintainer.toString() : "false").setMaxAge(86400).setPath("/");

        routingContext.response().addCookie(emailCookie).addCookie(maintainerCookie);
        System.out.println("[LoginHandler] Cookies set: Email: " + user.getString("email") + ", Is Maintainer: " + user.getBoolean("maintainer"));
    }
}
