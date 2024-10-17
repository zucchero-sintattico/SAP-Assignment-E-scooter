package forwarding_service.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DefaultRoute {
    private final CircuitBreaker circuitBreaker;
    private final Logger logger = LoggerFactory.getLogger(DefaultRoute.class);

    public DefaultRoute(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RouteLocator route(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("default_route", r -> r.path("/")
                .filters(f -> f.filter((exchange, chain) -> circuitBreaker.executeSupplier(() -> {
                    try {
                        logger.info("Reading home.html file and writing its content to the HTTP response.");
                        ClassPathResource homeHtml = new ClassPathResource("home.html");
                        String htmlContent = StreamUtils.copyToString(homeHtml.getInputStream(), StandardCharsets.UTF_8);
                        logger.info("Successfully read home.html file.");

                        exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
                        logger.info("Writing content to the HTTP response.");
                        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(htmlContent.getBytes())));
                    } catch (IOException e) {
                        logger.error("Failed to read home.html file.", e);
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    }
                }).onErrorResume(throwable -> {
                    logger.error("Circuit Breaker is open. Failed to read home.html file and write its content to the HTTP response.", throwable);
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                }).then(chain.filter(exchange)))).uri("no://op") // This URI is ignored by the filter
            ).build();
    }
}
