package sap.pixelart.apigateway;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

/**
 * 
 * Cooperative PixelArt Service launcher
 * 
 * @author aricci
 *
 */
public class APIGatewayServiceLauncher {
		
    public static void main(String[] args) {

        JvmMetrics.builder().register();

        Counter counter = Counter.builder()
                .name("API_Gateway_http_requests_total")
                .help("Total number of HTTP requests")
                .labelNames("method", "path", "status")
                .register();

    	APIGatewayService service = new APIGatewayService();

        try (HTTPServer server = HTTPServer.builder().port(9010).buildAndStart()){
            System.out.println("[API Gateway] HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

            service.launch(counter);

            // Attendi indefinitamente
            Thread.currentThread().join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
