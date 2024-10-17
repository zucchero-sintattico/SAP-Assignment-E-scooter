package user_service.handlers;

import io.vertx.ext.web.RoutingContext;

public interface IHandler {
    void handle(RoutingContext routingContext);
}
