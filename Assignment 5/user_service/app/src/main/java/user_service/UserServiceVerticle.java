package user_service;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import user_service.db.IDbClient;
import user_service.db.MongoDbClient;
import user_service.handlers.*;

public class UserServiceVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        try {
            JsonObject config = new JsonObject().put("connection_string", "mongodb://localhost:27017")
                    .put("db_name", "Users_Service");

            MongoClient mongoClient = MongoClient.createShared(vertx, config);
            IDbClient databaseClient = new MongoDbClient(mongoClient);

            // Initialize the CircuitBreaker
            CircuitBreaker circuitBreaker = CircuitBreaker.create("db-circuit-breaker", vertx, new CircuitBreakerOptions());

            // Create a router with all routes
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create()); // Aggiungo questo handler per poter leggere il body delle richieste
            router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))); // Aggiungo questo handler per gestire le sessioni

            // Rotta per visualizzare la dashboard
            router.route(HttpMethod.GET, "/api/users/dashboard").handler(new HomeHandler()::handle);

            // Rotta per visualizzare la pagina di login
            router.route(HttpMethod.GET, "/api/users/login-form").handler(new AuthPageHandler(circuitBreaker, "/login.html")::handle);

            // Rotta per visualizzare la pagina di registrazione
            router.route(HttpMethod.GET, "/api/users/register-form").handler(new AuthPageHandler(circuitBreaker, "/registration.html")::handle);

            // Rotta per creare un nuovo utente (registrazione)
            router.route(HttpMethod.POST, "/api/users/auth/register").handler(new RegisterHandler(databaseClient, circuitBreaker)::handle);

            // API per il login (creazione di una sessione utente)
            router.route(HttpMethod.POST, "/api/users/auth/login").handler(new LoginHandler(databaseClient, circuitBreaker)::handle);

            // API per il logout (eliminazione di una sessione utente)
            router.route(HttpMethod.DELETE, "/api/users/auth/logout").handler(new LogoutHandler(circuitBreaker)::handle);

            // Create a http server with the router
            vertx.createHttpServer().requestHandler(router).listen(8888, http -> {
                if (http.succeeded()) {
                    startPromise.complete();
                    System.out.println("HTTP server started on port 8888");
                } else {
                    startPromise.fail(http.cause());
                    System.err.println("Failed to start HTTP server: " + http.cause().getMessage());
                }
            });
        } catch (Exception e) {
            startPromise.fail(e);
            System.err.println("Exception during start: " + e.getMessage());
        }

    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        try {
            super.stop(stopPromise);
        } catch (Exception e) {
            stopPromise.fail(e);
        }
    }
}
