package distributedlogservice.infrastructure;

import io.vertx.core.Vertx;

public class DistributedLogController {

    private final int port;

    public DistributedLogController(int port) {
        this.port = port;
    }

    public void init() {
        Vertx vertx = Vertx.vertx();
        DistributedLogControllerVerticle service = new DistributedLogControllerVerticle(port);
        vertx.deployVerticle(service);
    }
}
