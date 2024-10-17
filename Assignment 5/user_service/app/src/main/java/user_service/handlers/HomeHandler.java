package user_service.handlers;

import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class HomeHandler implements IHandler {

    @Override
    public void handle(RoutingContext routingContext) {
        System.out.println("Received request for home page user");
        InputStream inputStream = getClass().getResourceAsStream("/home.html");
        if (inputStream != null) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String fileContent = reader.lines().collect(Collectors.joining("\n"));

            HttpServerRequest request = routingContext.request();
            Cookie emailCookie = request.getCookie("email");
            if (emailCookie != null) {
                String email = emailCookie.getValue();
                fileContent = fileContent.replace("Welcome to the User Service!", "Welcome to the User Service, " + email + "!");
            }
            routingContext.response().setStatusCode(200).putHeader(HttpHeaders.CONTENT_TYPE, "text/html").end(fileContent);
        } else {
            routingContext.response().setStatusCode(404).end("Page not found");
        }
    }
}
