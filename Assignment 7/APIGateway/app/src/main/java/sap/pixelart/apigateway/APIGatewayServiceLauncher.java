package sap.pixelart.apigateway;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.ThreadLocalCurrentTraceContext;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.Sender;
import zipkin2.Span;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

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


        // Crea un sender per Zipkin (anche se deprecato, funziona ancora)
        OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v2/spans");

        // Crea un AsyncReporter per inviare gli span a Zipkin
        AsyncReporter<Span> reporter = AsyncReporter.create(sender);

        // Crea lo ZipkinSpanHandler usando il reporter
        SpanHandler zipkinSpanHandler = ZipkinSpanHandler.newBuilder(reporter).build();

        // Imposta il tracing con il zipkinSpanHandler
        Tracing tracing = Tracing.newBuilder()
                .localServiceName("APIGatewayService")
                .addSpanHandler(zipkinSpanHandler)
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().build())
                .build();

        Tracer tracer = tracing.tracer();
        ScopedSpan span = tracer.startScopedSpan("APIGatewayService-main");

    	APIGatewayService service = new APIGatewayService();

        try (HTTPServer server = HTTPServer.builder().port(9010).buildAndStart()){
            System.out.println("[API Gateway] HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

            service.launch(counter);

            // Attendi indefinitamente
            Thread.currentThread().join();
        } catch (Exception e) {
            span.error(e);
        } finally {
            span.finish();
        }

        tracing.close();
        reporter.close();
        sender.close();
    }
}
