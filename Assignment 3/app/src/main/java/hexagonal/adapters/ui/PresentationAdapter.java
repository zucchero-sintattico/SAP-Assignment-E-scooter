package hexagonal.adapters.ui;

import hexagonal.adapters.HttpServerAdapter;
import hexagonal.domain.services.EScooterService;
import hexagonal.domain.services.RideService;
import hexagonal.domain.services.UserService;
import hexagonal.ports.IPresentationPort;
import io.vertx.core.Vertx;

public class PresentationAdapter implements IPresentationPort {
    private final UserService userService;
    private final EScooterService escooterService;
    private final RideService rideService;
    private final int port;

    public PresentationAdapter(UserService userService, EScooterService escooterService, RideService rideService, int port) {
        this.userService = userService;
        this.escooterService = escooterService;
        this.rideService = rideService;
        this.port = port;
    }

    @Override
    public void init() {
        Vertx vertx = Vertx.vertx();
        HttpServerAdapter myVerticle = new HttpServerAdapter(port, userService, escooterService, rideService);
        vertx.deployVerticle(myVerticle);
    }
}
