package forwarding_service.routes;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpServerErrorException;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RideServiceRoute {

    private final CircuitBreaker circuitBreaker;
    private final Logger logger = LoggerFactory.getLogger(RideServiceRoute.class);

    public RideServiceRoute(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RouteLocator route(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("rides_service_root_route", r -> r.path("/api/rides/**")
                .filters(f -> f.filter((exchange, chain) -> this.circuitBreaker.executeSupplier(() -> {
                    try {
                        logger.info("Forwarding the request to the ride service: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());
                        var request = exchange.getRequest();
                        var emailCookie = request.getCookies().getFirst("email");
                        if (emailCookie != null) {
                            logger.info(" [Rides Service] Email cookie is set. Request: {}", request.getURI());
                        } else {
                            logger.info(" [Rides Service] Email cookie is not set. Redirecting to login page.");
                            ClassPathResource redirectHtml = new ClassPathResource("redirect.html");
                            String htmlContent = StreamUtils.copyToString(redirectHtml.getInputStream(), StandardCharsets.UTF_8);
                            exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
                            return exchange.getResponse().writeWith(
                                    Mono.just(exchange.getResponse().bufferFactory().wrap(htmlContent.getBytes()))
                            );
                        }
                        return chain.filter(exchange);
                    } catch (HttpServerErrorException | IOException e) {
                        logger.error("Failed to forward the request to the ride service.", e);
                        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                        return exchange.getResponse().setComplete();
                    }
                }).onErrorResume(throwable -> {
                    logger.error("Circuit Breaker is open. Failed to forward the request to the ride service.", throwable);
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                }).then(chain.filter(exchange)))).uri("http://localhost:8081") // This URI is ignored by the filter
        ).build();
    }
}
