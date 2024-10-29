package sap.pixelart.dashboard;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import sap.pixelart.dashboard.controller.Controller;
import sap.pixelart.library.PixelArtAsyncAPI;
import sap.pixelart.library.PixelArtServiceLib;

public class PixelArtDashboardMain {
	
	public static void main(String[] args) {

		JvmMetrics.builder().register();

		Histogram histogram = Histogram.builder()
				.name("api_response_time_seconds")
				.help("Response time of API calls")
				.labelNames("endpoint")
				.nativeMaxNumberOfBuckets(10)
				.register();

		PixelArtAsyncAPI proxy = PixelArtServiceLib.getInstance().getDefaultInterface(histogram);
		Controller controller = new Controller(proxy);
		try (HTTPServer server = HTTPServer.builder().port(9011).buildAndStart()) {
			System.out.println("[Dashboard] HTTPServer listening on port http://localhost:" + server.getPort() + "/metrics");
			controller.init();
			Thread.currentThread().join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
