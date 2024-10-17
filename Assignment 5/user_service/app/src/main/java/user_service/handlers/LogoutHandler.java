package user_service.handlers;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;

public class LogoutHandler implements IHandler {

    private final CircuitBreaker circuitBreaker;

    public LogoutHandler(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("[LogoutHandler] Handling logout request...");
        this.circuitBreaker.execute(promise -> {
            try {
                Cookie cookie = Cookie.cookie("email", "").setMaxAge(0).setPath("/").setDomain("localhost");
                routingContext.response().addCookie(cookie);

                System.out.println("[LogoutHandler] User logged out. Cookie deleted.");
                promise.complete();
            } catch (Exception e) {
                System.out.println("[LogoutHandler] Failed to handle logout request: " + e);
                promise.fail(e);
            }
        }).onComplete(ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(200).end("Logout successful!");
            } else {
                System.out.println("Failed to handle logout request: " + ar.cause());
                routingContext.fail(ar.cause());
            }
        });
    }
}
