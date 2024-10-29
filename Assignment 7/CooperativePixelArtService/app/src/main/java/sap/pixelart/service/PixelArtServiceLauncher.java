package sap.pixelart.service;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.ThreadLocalCurrentTraceContext;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * 
 * Cooperative PixelArt Service launcher
 * 
 * @author aricci
 *
 */
public class PixelArtServiceLauncher {
		
    public static void main(String[] args) {

        JvmMetrics.builder().register();

        Gauge cpuUsage = Gauge.builder()
                .name("cpu_usage_percentage")
                .help("CPU usage percentage")
                .register();

        Gauge heapMemoryUsed = Gauge.builder()
                .name("heap_memory_used_bytes")
                .help("Used heap memory in bytes")
                .register();

        Gauge nonHeapMemoryUsed = Gauge.builder()
                .name("non_heap_memory_used_bytes")
                .help("Used non-heap memory in bytes")
                .register();

        // Crea un sender per Zipkin (anche se deprecato, funziona ancora)
        OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v2/spans");

        // Crea un AsyncReporter per inviare gli span a Zipkin
        AsyncReporter<Span> reporter = AsyncReporter.create(sender);

        // Crea lo ZipkinSpanHandler usando il reporter
        SpanHandler zipkinSpanHandler = ZipkinSpanHandler.newBuilder(reporter).build();

        // Imposta il tracing con il zipkinSpanHandler
        Tracing tracing = Tracing.newBuilder()
                .localServiceName("PixelArtService")
                .addSpanHandler(zipkinSpanHandler)
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().build())
                .build();

        Tracer tracer = tracing.tracer();
        ScopedSpan span = tracer.startScopedSpan("PixelArtService-main");

    	PixelArtService service = new PixelArtService();

        try (HTTPServer server = HTTPServer.builder().port(9012).buildAndStart()){
            System.out.println("[CooperativePixelArtService] HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");

            service.launch(cpuUsage, heapMemoryUsed, nonHeapMemoryUsed);

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
