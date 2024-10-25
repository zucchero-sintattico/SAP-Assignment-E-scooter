package sap.pixelart.apigateway.infrastructure;

import java.util.logging.Logger;
import io.vertx.core.Vertx;
import sap.pixelart.library.PixelArtAsyncAPI;

public class APIGatewayController {
    static Logger logger = Logger.getLogger("[APIGatewayController]");	
	private final int port;

    public APIGatewayController(int port) {
		this.port = port;
	}
		
	public void init(PixelArtAsyncAPI pixelArtAPI) {
    	Vertx vertx = Vertx.vertx();
        APIGatewayControllerVerticle verticle = new APIGatewayControllerVerticle(port, pixelArtAPI);
		vertx.deployVerticle(verticle);
	}

}
