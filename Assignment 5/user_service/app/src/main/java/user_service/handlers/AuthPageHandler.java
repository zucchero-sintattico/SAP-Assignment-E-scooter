package user_service.handlers;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class AuthPageHandler implements IHandler{
    private final CircuitBreaker circuitBreaker;
    private final String htmlFilePath;

    public AuthPageHandler(CircuitBreaker circuitBreaker, String htmlFilePath) {
        this.circuitBreaker = circuitBreaker;
        this.htmlFilePath = htmlFilePath;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("Received request for page: " + htmlFilePath);
        this.circuitBreaker.execute(promise -> {
            try {
                InputStream inputStream = getClass().getResourceAsStream(htmlFilePath);
                BufferedReader reader = inputStream != null ? new BufferedReader(new InputStreamReader(inputStream)) : null;
                String fileContent = reader != null ? reader.lines().collect(Collectors.joining("\n")) : null;
                routingContext.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(fileContent);
                promise.complete();
            } catch (Exception e) {
                promise.fail(e);
            }
        }).onComplete(ar -> {
            if (ar.failed()) {
                routingContext.response().setStatusCode(500).end("Internal Server Error");
                System.out.println("Failed to read " + htmlFilePath + " file.");
                System.out.println("Failed to handle GET request: " + ar.cause());
            }
        });
    }
}
