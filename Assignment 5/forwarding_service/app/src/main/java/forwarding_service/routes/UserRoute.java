package forwarding_service.routes;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.http.HttpStatus;

public class UserRoute {

    private final CircuitBreaker circuitBreaker;
    private final Logger logger = LoggerFactory.getLogger(UserRoute.class);

    public UserRoute(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RouteLocator route(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user_route", r -> r.path("/api/users/**")
                .filters(f -> f.filter((exchange, chain) -> circuitBreaker.executeSupplier(() -> {
                    logger.info("Forwarding the request to the user service.");
                    logger.info(" [Users Service] Request: {}", exchange.getRequest().getURI());
                    return chain.filter(exchange);
                }).onErrorResume(throwable -> {
                    logger.error("Circuit Breaker is open. Failed to forward the request to the user service.", throwable);
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                }))).uri("http://localhost:8888")
            ).build();
    }
}
