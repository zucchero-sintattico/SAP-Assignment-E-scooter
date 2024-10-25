package sap.pixelart.apigateway;

import sap.pixelart.apigateway.infrastructure.APIGatewayController;
import sap.pixelart.library.PixelArtAsyncAPI;
import sap.pixelart.library.PixelArtServiceLib;

public class APIGatewayService {

	private static final int DEFAULT_HTTP_PORT = 9001;

	private final PixelArtAsyncAPI service;
    private int restAPIPort;
	
	public APIGatewayService() {
    	service = PixelArtServiceLib.getInstance().getDefaultInterface();
    	restAPIPort = DEFAULT_HTTP_PORT;
	}
	
	public void configure(int port) {
		restAPIPort = port;
	}
	
	public void launch() {
        APIGatewayController restBasedAdapter = new APIGatewayController(restAPIPort);
    	restBasedAdapter.init(service);
	}
}
