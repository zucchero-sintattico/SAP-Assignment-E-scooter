package sap.pixelart.service.infrastructure;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.prometheus.metrics.core.metrics.Gauge;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.*;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import sap.pixelart.service.application.*;
import sap.pixelart.service.domain.*;

/**
 * 
 * Verticle impementing the behaviour of a REST Adapter for the 
 * PixelArt microservice
 * 
 * @author aricci
 *
 */
public class RestPixelArtServiceControllerVerticle extends AbstractVerticle implements PixelGridEventObserver {

	private final int port;
	private final PixelArtAPI pixelArtAPI;
	static Logger logger = Logger.getLogger("[PixelArt Service]");
	static String PIXEL_GRID_CHANNEL = "pixel-grid-events";
	private final HttpClient client;
	private Vertx vertx;

	private final Gauge cpu;
	private final Gauge heapMemory;
	private final Gauge nonHeapMemory;

	public RestPixelArtServiceControllerVerticle(int port, PixelArtAPI appAPI, Gauge cpu, Gauge heapMemory, Gauge nonHeapMemory) {
		this.port = port;
		this.pixelArtAPI = appAPI;
		logger.setLevel(Level.INFO);
		this.vertx = Vertx.vertx();

		HttpClientOptions options = new HttpClientOptions()
				.setDefaultHost("localhost")
				.setDefaultPort(port);
		this.client = this.vertx.createHttpClient(options);

		this.cpu = cpu;
		this.heapMemory = heapMemory;
		this.nonHeapMemory = nonHeapMemory;

		sendLogRequest("[CooperativePixelArtService] - Init the RestPixelArtServiceControllerVerticle!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});
	}

	public void start() {
		logger.log(Level.INFO, "PixelArt Service initializing...");
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		/* configure the HTTP routes following a REST style */
		
		router.route(HttpMethod.POST, "/api/brushes").handler(this::createBrush);
		router.route(HttpMethod.GET, "/api/brushes").handler(this::getCurrentBrushes);
		router.route(HttpMethod.GET, "/api/brushes/:brushId").handler(this::getBrushInfo);
		router.route(HttpMethod.DELETE, "/api/brushes/:brushId").handler(this::destroyBrush);
		router.route(HttpMethod.POST, "/api/brushes/:brushId/move-to").handler(this::moveBrushTo);
		router.route(HttpMethod.POST, "/api/brushes/:brushId/change-color").handler(this::changeBrushColor);
		router.route(HttpMethod.POST, "/api/brushes/:brushId/select-pixel").handler(this::selectPixel);
		router.route(HttpMethod.GET, "/api/pixel-grid").handler(this::getPixelGridState);
		this.handleEventSubscription(server, "/api/pixel-grid/events");

		/* start the server */
		
		server
		.requestHandler(router)
		.listen(port);

		logger.log(Level.INFO, "PixelArt Service ready - port: " + port);
	}

	/* List of handlers, mapping the API */
	
	protected void createBrush(RoutingContext context) {
		logger.log(Level.INFO, "CreateBrush request - " + context.currentRoute().getPath());

		JsonObject reply = new JsonObject();
		try {
			String brushId = pixelArtAPI.createBrush();
			logger.log(Level.INFO, "Brush Number: " + brushId);
			reply.put("brushId", brushId);
			sendReply(context.response(), reply);
		} catch (Exception ex) {
			sendServiceError(context.response());
		}
		sendLogRequest("[CooperativePixelArtService] - Terminated createBrush!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void getCurrentBrushes(RoutingContext context) {
		logger.log(Level.INFO, "GetCurrentBrushes request - " + context.currentRoute().getPath());

		JsonObject reply = new JsonObject();
		try {
			JsonArray brushes = pixelArtAPI.getCurrentBrushes();
			reply.put("brushes", brushes);
			sendReply(context.response(), reply);
		} catch (Exception ex) {
			sendServiceError(context.response());
		}
		sendLogRequest("[CooperativePixelArtService] - Terminated getCurrentBrushes!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void getBrushInfo(RoutingContext context) {
		logger.log(Level.INFO, "Get Brush info request: " + context.currentRoute().getPath());
		String brushId = context.pathParam("brushId");
		JsonObject reply = new JsonObject();
		try {
			JsonObject info = pixelArtAPI.getBrushInfo(brushId);
			reply.put("brushInfo", info);
			sendReply(context.response(), reply);
		} catch (Exception ex) {
			sendServiceError(context.response());
		}
		sendLogRequest("[CooperativePixelArtService] - Terminated getBrushInfo!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void moveBrushTo(RoutingContext context) {
		logger.log(Level.INFO, "MoveBrushTo request: " + context.currentRoute().getPath());
		String brushId = context.pathParam("brushId");
		logger.log(Level.INFO, "Brush id: " + brushId);
		// context.body().asJsonObject();
		context.request().handler(buf -> {
			JsonObject brushInfo = buf.toJsonObject();
			int x = brushInfo.getInteger("x");
			int y = brushInfo.getInteger("y");
			JsonObject reply = new JsonObject();
			try {
				pixelArtAPI.moveBrushTo(brushId, y, x);
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				sendServiceError(context.response());
			}
		});
		sendLogRequest("[CooperativePixelArtService] - Terminated moveBrushTo!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void changeBrushColor(RoutingContext context) {
		logger.log(Level.INFO, "ChangeBrushColor request: " + context.currentRoute().getPath());
		String brushId = context.pathParam("brushId");
		context.request().handler(buf -> {
			JsonObject brushInfo = buf.toJsonObject();
			logger.log(Level.INFO, "Body: " + brushInfo.encodePrettily());
			int c = brushInfo.getInteger("color");
			JsonObject reply = new JsonObject();
			try {
				pixelArtAPI.changeBrushColor(brushId, c);
				sendReply(context.response(), reply);
			} catch (Exception ex) {
				sendServiceError(context.response());
			}
		});
		sendLogRequest("[CooperativePixelArtService] - Terminated changeBrushColor!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void selectPixel(RoutingContext context) {
		logger.log(Level.INFO, "SelectPixel request: " + context.currentRoute().getPath());
		String brushId = context.pathParam("brushId");
		JsonObject reply = new JsonObject();
		try {
			pixelArtAPI.selectPixel(brushId);
			sendReply(context.response(), reply);
		} catch (Exception ex) {
			sendServiceError(context.response());
		}
		sendLogRequest("[CooperativePixelArtService] - Terminated selectPixel!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void destroyBrush(RoutingContext context) {
		logger.log(Level.INFO, "Destroy Brush request: " + context.currentRoute().getPath());
		String brushId = context.pathParam("brushId");
		JsonObject reply = new JsonObject();
		try {
			pixelArtAPI.destroyBrush(brushId);
			sendReply(context.response(), reply);
		} catch (Exception ex) {
			sendServiceError(context.response());
		}
		sendLogRequest("[CooperativePixelArtService] - Terminated destroyBrush!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void getPixelGridState(RoutingContext context) {
		logger.log(Level.INFO, "Get Pixel Grid state request: " + context.currentRoute().getPath());
		JsonObject reply = new JsonObject();
		try {
			JsonObject info = pixelArtAPI.getPixelGridState();
			reply.put("pixelGrid", info);
			sendReply(context.response(), reply);
		} catch (Exception ex) {
			sendServiceError(context.response());
		}
		sendLogRequest("[CooperativePixelArtService] - Terminated getPixelGridState!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	/* Handling subscribers using web sockets */
	
	protected void handleEventSubscription(HttpServer server, String path) {
		server.webSocketHandler(webSocket -> {
			if (webSocket.path().equals(path)) {
				webSocket.accept();
				logger.log(Level.INFO, "New PixelGrid subscription accepted.");
				JsonObject reply = new JsonObject();
				JsonObject grid = pixelArtAPI.getPixelGridState();
				reply.put("event", "subscription-started");
				reply.put("pixelGridCurrentState", grid);
				webSocket.writeTextMessage(reply.encodePrettily());
				EventBus eb = vertx.eventBus();
				eb.consumer(PIXEL_GRID_CHANNEL, msg -> {
					JsonObject ev = (JsonObject) msg.body();
					logger.log(Level.INFO, "Event: " + ev.encodePrettily());
					webSocket.writeTextMessage(ev.encodePrettily());
				});
			} else {
				logger.log(Level.INFO, "PixelGrid subscription refused.");
				webSocket.reject();
			}
		});
		sendLogRequest("[CooperativePixelArtService] - Terminated handleEventSubscription!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}
	
	/* This is notified by the application/domain layer */
	
	@Override
	public void pixelColorChanged(int x, int y, int color) {
		logger.log(Level.INFO, "New PixelGrid event - pixel selected");
		EventBus eb = vertx.eventBus();
		JsonObject obj = new JsonObject();
		obj.put("event", "pixel-selected");
		obj.put("x", x);
		obj.put("y", y);
		obj.put("color", color);
		eb.publish(PIXEL_GRID_CHANNEL, obj);

		sendLogRequest("[CooperativePixelArtService] - Terminated pixelColorChanged!")
				.onComplete(logRes -> {
					if (logRes.failed()) {
						logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
					}
				});

		/* Prometheus */
		// Update CPU usage metric
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);

		// Update memory metrics
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();

		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	/* Aux methods */

	//Part of the Pattern for the Distributed Log.
	private Future<Void> sendLogRequest(String messageLog) {
		Promise<Void> p = Promise.promise();

		JsonObject logData = new JsonObject().put("message", messageLog);
		client
				.request(HttpMethod.POST, 9003, "localhost", "/api/logs")
				.onSuccess(request -> {
					// Imposta l'header content-type
					request.putHeader("content-type", "application/json");

					// Converti l'oggetto JSON in una stringa e invialo come corpo della richiesta
					String payload = logData.encodePrettily();
					request.putHeader("content-length", "" + payload.length());

					request.write(payload);

					request.response().onSuccess(resp -> {
						p.complete();
					});

					System.out.println("[Log] Received response with status code " + request.getURI());
					// Invia la richiesta
					request.end();
				})
				.onFailure(f -> {
					p.fail(f.getMessage());
				});

		return p.future();
	}
	

	private void sendReply(HttpServerResponse response, JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}
	
	private void sendBadRequest(HttpServerResponse response, JsonObject reply) {
		response.setStatusCode(400);
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}

	private void sendServiceError(HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}

	/* Aux Methods for Prometheus */
	private static double getProcessCpuLoad() {
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
			return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
		}
		return -1.0; // Not supported on this platform
	}

	private static MemoryUsage getHeapMemoryUsage() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		return memoryBean.getHeapMemoryUsage();
	}

	private static MemoryUsage getNonHeapMemoryUsage() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		return memoryBean.getNonHeapMemoryUsage();
	}

}
