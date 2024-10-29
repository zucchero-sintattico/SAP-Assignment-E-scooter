package sap.pixelart.library;

import io.prometheus.metrics.core.metrics.Histogram;

/**
 * 
 * Library for interacting with the PixelArt service
 * 
 * - it is a singleton factory
 * 
 * @author aricci
 *
 */
public class PixelArtServiceLib {

	private final String DEFAULT_HOST = "localhost";
	private final int DEFAULT_PORT = 9001;
	
	private static PixelArtServiceLib instance;
	
	private PixelArtServiceLib() {
	}
	
	static public PixelArtServiceLib getInstance() {
		synchronized (PixelArtServiceLib.class) {
			if (instance == null) {
				instance = new PixelArtServiceLib();
			}
			return instance;
		}
	}
	
	public PixelArtAsyncAPI getDefaultInterface(Histogram histogram) {
		PixelArtServiceProxy serviceProxy = new PixelArtServiceProxy();
		serviceProxy.init(DEFAULT_HOST, DEFAULT_PORT, histogram);
		return serviceProxy;
	}
	
}
