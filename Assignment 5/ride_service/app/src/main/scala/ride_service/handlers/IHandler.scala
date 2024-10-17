package ride_service.handlers

import io.vertx.ext.web.RoutingContext

trait IHandler {
  def handle(routingContext: RoutingContext): Unit

}
