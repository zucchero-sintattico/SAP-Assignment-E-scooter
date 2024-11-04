package sap.pixelart.service.infrastructure;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.logging.Level;
import java.util.logging.Logger;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.ThreadLocalCurrentTraceContext;
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
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

public class RestPixelArtServiceControllerVerticle extends AbstractVerticle implements PixelGridEventObserver {

	private final int port;
	private final PixelArtAPI pixelArtAPI;
	static Logger logger = Logger.getLogger("[PixelArt Service]");
	static String PIXEL_GRID_CHANNEL = "pixel-grid-events";
	private final HttpClient client;
	private final Gauge cpu;
	private final Gauge heapMemory;
	private final Gauge nonHeapMemory;
	private final Tracer tracer;

	public RestPixelArtServiceControllerVerticle(int port, PixelArtAPI appAPI, Gauge cpu, Gauge heapMemory, Gauge nonHeapMemory) {
		this.port = port;
		this.pixelArtAPI = appAPI;
		logger.setLevel(Level.INFO);
		this.vertx = Vertx.vertx();
		this.cpu = cpu;
		this.heapMemory = heapMemory;
		this.nonHeapMemory = nonHeapMemory;

		HttpClientOptions options = new HttpClientOptions().setDefaultHost("localhost").setDefaultPort(port);
		this.client = this.vertx.createHttpClient(options);

		OkHttpSender sender = OkHttpSender.create("http://localhost:9411/api/v2/spans");
		AsyncReporter<Span> reporter = AsyncReporter.create(sender);
		SpanHandler zipkinSpanHandler = ZipkinSpanHandler.newBuilder(reporter).build();
		Tracing tracing = Tracing.newBuilder().localServiceName("PixelArtService")
				.addSpanHandler(zipkinSpanHandler)
				.currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().build())
				.build();

		this.tracer = tracing.tracer();
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

		router.route(HttpMethod.GET, "/health").handler(this::checkHealth);
		router.route(HttpMethod.POST, "/api/brushes").handler(this::createBrush);
		router.route(HttpMethod.GET, "/api/brushes").handler(this::getCurrentBrushes);
		router.route(HttpMethod.GET, "/api/brushes/:brushId").handler(this::getBrushInfo);
		router.route(HttpMethod.DELETE, "/api/brushes/:brushId").handler(this::destroyBrush);
		router.route(HttpMethod.POST, "/api/brushes/:brushId/move-to").handler(this::moveBrushTo);
		router.route(HttpMethod.POST, "/api/brushes/:brushId/change-color").handler(this::changeBrushColor);
		router.route(HttpMethod.POST, "/api/brushes/:brushId/select-pixel").handler(this::selectPixel);
		router.route(HttpMethod.GET, "/api/pixel-grid").handler(this::getPixelGridState);
		this.handleEventSubscription(server, "/api/pixel-grid/events");

		server.requestHandler(router).listen(port);
		logger.log(Level.INFO, "PixelArt Service ready - port: " + port);
	}

	private void checkHealth(RoutingContext routingContext) {
		JsonObject status = new JsonObject();
		status.put("createBrush", executeHealthCheck(() -> createBrush(routingContext)));
		status.put("getCurrentBrushes", executeHealthCheck(() -> getCurrentBrushes(routingContext)));
		status.put("getBrushInfo", executeHealthCheck(() -> getBrushInfo(routingContext)));
		status.put("moveBrushTo", executeHealthCheck(() -> moveBrushTo(routingContext)));
		status.put("changeBrushColor", executeHealthCheck(() -> changeBrushColor(routingContext)));
		status.put("selectPixel", executeHealthCheck(() -> selectPixel(routingContext)));
		status.put("destroyBrush", executeHealthCheck(() -> destroyBrush(routingContext)));
		status.put("getPixelGridState", executeHealthCheck(() -> getPixelGridState(routingContext)));

		boolean isSystemHealth = status.stream().allMatch(entry -> (boolean) entry.getValue());
		status.put("status", isSystemHealth ? "UP" : "DOWN");

		logger.log(Level.INFO, "API HealthCheck request - " + routingContext.currentRoute().getPath());
		logger.log(Level.INFO, "Body: " + status.encodePrettily());
	}

	private boolean executeHealthCheck(Runnable runnable) {
		try {
			runnable.run();
			return true;
		} catch (IllegalStateException e) {
			return true;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Triggered the exception: " + e);
			return false;
		}
	}

	private void handleRequest(RoutingContext context, String spanName, RequestHandler handler) {
		ScopedSpan span = this.tracer.startScopedSpan(spanName);
		try {
			logger.log(Level.INFO, spanName + " request - " + context.currentRoute().getPath());
			handler.handle(context);
			sendLogRequest("[CooperativePixelArtService] - Terminated " + spanName + "!")
					.onComplete(logRes -> {
						if (logRes.failed()) {
							logger.log(Level.WARNING, "Errore durante l'invio del log: " + logRes.cause());
						}
					});
			updateMetrics();
		} finally {
			span.finish();
		}
	}

	private void updateMetrics() {
		double cpuUsageValue = getProcessCpuLoad();
		this.cpu.inc(cpuUsageValue);
		long heapMemoryUsedValue = getHeapMemoryUsage().getUsed();
		long nonHeapMemoryUsedValue = getNonHeapMemoryUsage().getUsed();
		this.heapMemory.inc(heapMemoryUsedValue);
		this.nonHeapMemory.inc(nonHeapMemoryUsedValue);
	}

	protected void createBrush(RoutingContext context) {
		handleRequest(context, "createBrush", ctx -> {
			JsonObject reply = new JsonObject();
			try {
				String brushId = pixelArtAPI.createBrush();
				reply.put("brushId", brushId);
				sendReply(ctx.response(), reply);
			} catch (Exception ex) {
				sendServiceError(ctx.response());
			}
		});
	}

	protected void getCurrentBrushes(RoutingContext context) {
		handleRequest(context, "getCurrentBrushes", ctx -> {
			JsonObject reply = new JsonObject();
			try {
				JsonArray brushes = pixelArtAPI.getCurrentBrushes();
				reply.put("brushes", brushes);
				sendReply(ctx.response(), reply);
			} catch (Exception ex) {
				sendServiceError(ctx.response());
			}
		});
	}

	protected void getBrushInfo(RoutingContext context) {
		handleRequest(context, "getBrushInfo", ctx -> {
			String brushId = ctx.pathParam("brushId");
			JsonObject reply = new JsonObject();
			try {
				JsonObject info = pixelArtAPI.getBrushInfo(brushId);
				reply.put("brushInfo", info);
				sendReply(ctx.response(), reply);
			} catch (Exception ex) {
				sendServiceError(ctx.response());
			}
		});
	}

	protected void moveBrushTo(RoutingContext context) {
		handleRequest(context, "moveBrushTo", ctx -> {
			String brushId = ctx.pathParam("brushId");
			ctx.request().handler(buf -> {
				JsonObject brushInfo = buf.toJsonObject();
				int x = brushInfo.getInteger("x");
				int y = brushInfo.getInteger("y");
				JsonObject reply = new JsonObject();
				try {
					pixelArtAPI.moveBrushTo(brushId, y, x);
					sendReply(ctx.response(), reply);
				} catch (Exception ex) {
					sendServiceError(ctx.response());
				}
			});
		});
	}

	protected void changeBrushColor(RoutingContext context) {
		handleRequest(context, "changeBrushColor", ctx -> {
			String brushId = ctx.pathParam("brushId");
			ctx.request().handler(buf -> {
				JsonObject brushInfo = buf.toJsonObject();
				int c = brushInfo.getInteger("color");
				JsonObject reply = new JsonObject();
				try {
					pixelArtAPI.changeBrushColor(brushId, c);
					sendReply(ctx.response(), reply);
				} catch (Exception ex) {
					sendServiceError(ctx.response());
				}
			});
		});
	}

	protected void selectPixel(RoutingContext context) {
		handleRequest(context, "selectPixel", ctx -> {
			String brushId = ctx.pathParam("brushId");
			JsonObject reply = new JsonObject();
			try {
				pixelArtAPI.selectPixel(brushId);
				sendReply(ctx.response(), reply);
			} catch (Exception ex) {
				sendServiceError(ctx.response());
			}
		});
	}

	protected void destroyBrush(RoutingContext context) {
		handleRequest(context, "destroyBrush", ctx -> {
			String brushId = ctx.pathParam("brushId");
			JsonObject reply = new JsonObject();
			try {
				pixelArtAPI.destroyBrush(brushId);
				sendReply(ctx.response(), reply);
			} catch (Exception ex) {
				sendServiceError(ctx.response());
			}
		});
	}

	protected void getPixelGridState(RoutingContext context) {
		handleRequest(context, "getPixelGridState", ctx -> {
			JsonObject reply = new JsonObject();
			try {
				JsonObject info = pixelArtAPI.getPixelGridState();
				reply.put("pixelGrid", info);
				sendReply(ctx.response(), reply);
			} catch (Exception ex) {
				sendServiceError(ctx.response());
			}
		});
	}

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
		updateMetrics();
	}

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
		updateMetrics();
	}

	private Future<Void> sendLogRequest(String messageLog) {
		Promise<Void> p = Promise.promise();
		JsonObject logData = new JsonObject().put("message", messageLog);
		client.request(HttpMethod.POST, 9003, "localhost", "/api/logs")
				.onSuccess(request -> {
					request.putHeader("content-type", "application/json");
					String payload = logData.encodePrettily();
					request.putHeader("content-length", "" + payload.length());
					request.write(payload);
					request.response().onSuccess(resp -> p.complete());
					System.out.println("[Log] Received response with status code " + request.getURI());
					request.end();
				})
				.onFailure(f -> p.fail(f.getMessage()));
		return p.future();
	}

	private void sendReply(HttpServerResponse response, JsonObject reply) {
		response.putHeader("content-type", "application/json");
		response.end(reply.toString());
	}

	private void sendServiceError(HttpServerResponse response) {
		response.setStatusCode(500);
		response.putHeader("content-type", "application/json");
		response.end();
	}

	private static double getProcessCpuLoad() {
		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
			return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
		}
		return -1.0;
	}

	private static MemoryUsage getHeapMemoryUsage() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		return memoryBean.getHeapMemoryUsage();
	}

	private static MemoryUsage getNonHeapMemoryUsage() {
		MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
		return memoryBean.getNonHeapMemoryUsage();
	}

	@FunctionalInterface
	private interface RequestHandler {
		void handle(RoutingContext context);
	}
}
