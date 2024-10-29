package sap.pixelart.service;

import io.prometheus.metrics.core.metrics.Gauge;
import sap.pixelart.service.application.PixelArtServiceImpl;
import sap.pixelart.service.infrastructure.RestPixelArtServiceController;

public class PixelArtService {

	private static int DEFAULT_HTTP_PORT = 9000;

	private PixelArtServiceImpl service;
	private RestPixelArtServiceController restBasedAdapter;
	private int restAPIPort; 
	
	public PixelArtService() {
    	service = new PixelArtServiceImpl();
    	restAPIPort = DEFAULT_HTTP_PORT;
	}
	
	public void configure(int port) {
		restAPIPort = port;
	}
	
	public void launch(Gauge cpu, Gauge heapMemory, Gauge nonHeapMemory) {
    	service.init();
		restBasedAdapter = new RestPixelArtServiceController(restAPIPort);	    	
    	restBasedAdapter.init(service, cpu, heapMemory, nonHeapMemory);
	}
}
