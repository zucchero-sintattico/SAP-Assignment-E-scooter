package forwarding_service.routes;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

public class ManagementServiceRoute {
    private final CircuitBreaker circuitBreaker;
    private final Logger logger = LoggerFactory.getLogger(ManagementServiceRoute.class);

    public ManagementServiceRoute(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public RouteLocator route(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("management_service_route", r -> r.path("/api/management/**")
                .filters(f -> f.filter(this::applyFilter))
                .uri("http://localhost:8082")
            ).build();
    }

    private Mono<Void> applyFilter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var request = exchange.getRequest();

        this.logger.info("Forwarding the request to the management service: {} {}", request.getMethod(), request.getURI());

        if (request.getMethod() != HttpMethod.PUT) {
            var isMaintenanceCookie = request.getCookies().getFirst("isMaintainer");
            if (isMaintenanceCookie != null) {
                this.logger.info("Request is from a maintainer. Proceeding with the request.");
            } else {
                this.logger.info("Request is not from a maintainer. Redirecting to login page.");
                return this.redirectToLogin(exchange);
            }
        }

        try {
            return circuitBreaker.executeSupplier(() -> chain.filter(exchange));
        } catch (Exception e) {
            logger.error("Circuit Breaker is open. Failed to handle incoming request.");
            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return exchange.getResponse().setComplete();
        }
    }

    private Mono<Void> redirectToLogin(ServerWebExchange exchange) {
        try {
            var redirectHtml = new ClassPathResource("redirect.html");
            var htmlContent = StreamUtils.copyToString(redirectHtml.getInputStream(), StandardCharsets.UTF_8);
            exchange.getResponse().getHeaders().setContentType(MediaType.TEXT_HTML);
            return exchange.getResponse().writeWith(
                    Mono.just(exchange.getResponse().bufferFactory().wrap(htmlContent.getBytes()))
            );
        } catch (Exception e) {
            logger.error("Failed to load redirect HTML", e);
            return Mono.error(e);
        }
    }
}
