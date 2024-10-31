package sap.pixelart.apigateway.infrastructure;

import java.util.logging.Level;
import java.util.logging.Logger;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.handler.SpanHandler;
import brave.propagation.ThreadLocalCurrentTraceContext;
import io.prometheus.metrics.core.metrics.Counter;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.*;
import io.vertx.ext.web.*;
import sap.pixelart.library.PixelArtAsyncAPI;
import sap.pixelart.library.PixelGridEventObserver;
import zipkin2.Span;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.brave.ZipkinSpanHandler;
import zipkin2.reporter.okhttp3.OkHttpSender;

/**
 * 
 * Verticle impementing the behaviour of a REST Adapter for the 
 * PixelArt microservice
 * 
 * @author aricci
 *
 */
public class APIGatewayControllerVerticle extends AbstractVerticle implements PixelGridEventObserver {

	private final int port;
	private final PixelArtAsyncAPI serviceAPI;
	static Logger logger = Logger.getLogger("[PixelArt Service]");
	static String PIXEL_GRID_CHANNEL = "pixel-grid-events";
	private final Counter counter;
	private final Tracer tracer;

	public APIGatewayControllerVerticle(int port, PixelArtAsyncAPI serviceAPI, Counter counter) {
		this.port = port;
		this.serviceAPI = serviceAPI;
		logger.setLevel(Level.INFO);
		this.counter = counter;

		//Initialize the Tracer
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

		this.tracer = tracing.tracer();
	}

	public void start() {
		logger.log(Level.INFO, "PixelArt Service initializing...");
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);

		/* configure the HTTP routes following a REST style */
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

		/* start the server */
		
		server
		.requestHandler(router)
		.listen(port);

		logger.log(Level.INFO, "PixelArt Service ready - port: " + port);
	}

	protected void checkHealth(RoutingContext routingContext) {
		JsonObject status = new JsonObject();

		boolean isCreateBrushSuccessfull = executeHealthCheck(() -> createBrush(routingContext));
		boolean isGetCurrentBrushesSuccessfull = executeHealthCheck(() -> getCurrentBrushes(routingContext));
		boolean isGetBrushInfoSuccessfull = executeHealthCheck(() -> getBrushInfo(routingContext));
		boolean isDestroyBrushSuccessfull = executeHealthCheck(() -> destroyBrush(routingContext));
		boolean isMoveBrushToSuccessfull = executeHealthCheck(() -> moveBrushTo(routingContext));
		boolean isChangeBrushColorSuccessfull = executeHealthCheck(() -> changeBrushColor(routingContext));
		boolean isSelectPixelSuccessfull = executeHealthCheck(() -> selectPixel(routingContext));
		boolean isGetPixelGridStateSuccessfull = executeHealthCheck(() -> getPixelGridState(routingContext));

		status.put("createBrush", isCreateBrushSuccessfull);
		status.put("getCurrentBrushes", isGetCurrentBrushesSuccessfull);
		status.put("getBrushInfo", isGetBrushInfoSuccessfull);
		status.put("destroyBrush", isDestroyBrushSuccessfull);
		status.put("moveBrushTo", isMoveBrushToSuccessfull);
		status.put("changeBrushColor", isChangeBrushColorSuccessfull);
		status.put("selectPixel", isSelectPixelSuccessfull);
		status.put("getPixelGridState", isGetPixelGridStateSuccessfull);

		boolean isSystemHealthy = isCreateBrushSuccessfull && isGetCurrentBrushesSuccessfull &&
				isGetBrushInfoSuccessfull && isDestroyBrushSuccessfull && isMoveBrushToSuccessfull &&
				isChangeBrushColorSuccessfull && isSelectPixelSuccessfull && isGetPixelGridStateSuccessfull;

		status.put("status", isSystemHealthy ? "UP" : "DOWN");

		logger.log(Level.INFO, "API HealthCheck request - " + routingContext.currentRoute().getPath());
		logger.log(Level.INFO, "Body: " + status.encodePrettily());

		if (isSystemHealthy) counter.labelValues("GET", "/health", "success").inc();
		else counter.labelValues("GET", "/health", "error").inc();
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

	/* List of handlers, mapping the API */
	
	protected void createBrush(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("createBrush");
		try {
			this.counter.labelValues("POST", "/api/brushes", "success").inc();
			logger.log(Level.INFO, "CreateBrush request - " + context.currentRoute().getPath());
			JsonObject reply = new JsonObject();
			serviceAPI
				.createBrush()
				.onSuccess((String brushId) -> {
					try {
						reply.put("brushId", brushId);
						sendReply(context.response(), reply);
					} catch (Exception ex) {
						sendServiceError(context.response());
					}
				})
				.onFailure((e) -> {
					sendServiceError(context.response());
					this.counter.labelValues("POST", "/api/brushes", "error").inc();
				});
		}finally {
			span.finish();
		}
	}

	protected void getCurrentBrushes(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("getCurrentBrushes");
		try {
			this.counter.labelValues("GET", "/api/brushes", "success").inc();
			logger.log(Level.INFO, "GetCurrentBrushes request - " + context.currentRoute().getPath());

			JsonObject reply = new JsonObject();
			serviceAPI
				.getCurrentBrushes()
				.onSuccess((JsonArray brushes) -> {
					try {
						reply.put("brushes", brushes);
						sendReply(context.response(), reply);
					} catch (Exception ex) {
						sendServiceError(context.response());
					}
				})
				.onFailure((e) -> {
					sendServiceError(context.response());
					this.counter.labelValues("GET", "/api/brushes", "error").inc();
				});
		} finally {
			span.finish();
		}
	}

	protected void getBrushInfo(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("getBrushInfo");
		try {
			this.counter.labelValues("GET", "/api/brushes/:brushId", "success").inc();
			logger.log(Level.INFO, "Get Brush info request: " + context.currentRoute().getPath());
			String brushId = context.pathParam("brushId");
			JsonObject reply = new JsonObject();
			serviceAPI
				.getBrushInfo(brushId)
				.onSuccess((JsonObject info) -> {
					try {
						reply.put("brushInfo", info);
						sendReply(context.response(), reply);
					} catch (Exception ex) {
						sendServiceError(context.response());
					}
				})
				.onFailure((e) -> {
					sendServiceError(context.response());
					this.counter.labelValues("GET", "/api/brushes/:brushId", "error").inc();
				});
		} finally {
			span.finish();
		}
	}

	protected void moveBrushTo(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("moveBrushTo");
		try {
			this.counter.labelValues("POST", "/api/brushes/:brushId/move-to", "success").inc();
			logger.log(Level.INFO, "MoveBrushTo request: " + context.currentRoute().getPath());
			String brushId = context.pathParam("brushId");
			logger.log(Level.INFO, "Brush id: " + brushId);
			context.request().handler(buf -> {
				JsonObject brushInfo = buf.toJsonObject();
				int x = brushInfo.getInteger("x");
				int y = brushInfo.getInteger("y");
				JsonObject reply = new JsonObject();

				serviceAPI
					.moveBrushTo(brushId, y, x)
					.onSuccess((v) -> {
						try {
							sendReply(context.response(), reply);
						} catch (Exception ex) {
							sendServiceError(context.response());
							this.counter.labelValues("POST", "/api/brushes/:brushId/move-to", "error").inc();
						}
					})
					.onFailure((e) -> {
						sendServiceError(context.response());
					});
			});
		} finally {
			span.finish();
		}
	}

	protected void changeBrushColor(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("changeBrushColor");
		try {
			this.counter.labelValues("POST", "/api/brushes/:brushId/change-color", "success").inc();
			logger.log(Level.INFO, "ChangeBrushColor request: " + context.currentRoute().getPath());
			String brushId = context.pathParam("brushId");
			context.request().handler(buf -> {
				JsonObject brushInfo = buf.toJsonObject();
				logger.log(Level.INFO, "Body: " + brushInfo.encodePrettily());
				int c = brushInfo.getInteger("color");
				JsonObject reply = new JsonObject();
				serviceAPI
					.changeBrushColor(brushId, c)
					.onSuccess((v) -> {
						try {
							sendReply(context.response(), reply);
						} catch (Exception ex) {
							sendServiceError(context.response());
						}
					})
					.onFailure((e) -> {
						sendServiceError(context.response());
						this.counter.labelValues("POST", "/api/brushes/:brushId/change-color", "error").inc();
					});
			});
		} finally {
			span.finish();
		}
	}

	protected void selectPixel(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("selectPixel");
		try {
			this.counter.labelValues("POST", "/api/brushes/:brushId/select-pixel", "success").inc();
			logger.log(Level.INFO, "SelectPixel request: " + context.currentRoute().getPath());
			String brushId = context.pathParam("brushId");
			JsonObject reply = new JsonObject();
			serviceAPI
				.selectPixel(brushId)
				.onSuccess((v) -> {
					try {
						sendReply(context.response(), reply);
					} catch (Exception ex) {
						sendServiceError(context.response());
					}
				})
				.onFailure((e) -> {
					sendServiceError(context.response());
					this.counter.labelValues("POST", "/api/brushes/:brushId/select-pixel", "error").inc();
				});
		} finally {
			span.finish();
		}
	}

	protected void destroyBrush(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("destroyBrush");
		try {
			this.counter.labelValues("DELETE", "/api/brushes/:brushId", "success").inc();
			logger.log(Level.INFO, "Destroy Brush request: " + context.currentRoute().getPath());
			String brushId = context.pathParam("brushId");
			JsonObject reply = new JsonObject();
			serviceAPI
				.destroyBrush(brushId)
				.onSuccess((v) -> {
					try {
						sendReply(context.response(), reply);
					} catch (Exception ex) {
						sendServiceError(context.response());
					}
				})
				.onFailure((e) -> {
					sendServiceError(context.response());
					this.counter.labelValues("DELETE", "/api/brushes/:brushId", "error").inc();
				});
		} finally {
			span.finish();
		}
	}

	protected void getPixelGridState(RoutingContext context) {
		ScopedSpan span = this.tracer.startScopedSpan("getPixelGridState");
		try {
			this.counter.labelValues("GET", "/api/pixel-grid", "success").inc();
			logger.log(Level.INFO, "Get Pixel Grid state request: " + context.currentRoute().getPath());
			JsonObject reply = new JsonObject();
			serviceAPI
				.getPixelGridState()
				.onSuccess((JsonObject info) -> {
					try {
						reply.put("pixelGrid", info);
						sendReply(context.response(), reply);
					} catch (Exception ex) {
						sendServiceError(context.response());
					}
				})
				.onFailure((e) -> {
					sendServiceError(context.response());
					this.counter.labelValues("GET", "/api/pixel-grid", "error").inc();
				});
		} finally {
			span.finish();
		}
	}

	/* Handling subscribers using web sockets */
	
	protected void handleEventSubscription(HttpServer server, String path) {
		server.webSocketHandler(webSocket -> {
			if (webSocket.path().equals(path)) {
				webSocket.accept();
				logger.log(Level.INFO, "New PixelGrid subscription accepted.");
				
				JsonObject reply = new JsonObject();
				serviceAPI
				.subscribePixelGridEvents(this) //this::pixelColorChanged
				.onSuccess((JsonObject grid) -> {
					reply.put("event", "subscription-started");
					reply.put("pixelGridCurrentState", grid);
					webSocket.writeTextMessage(reply.encodePrettily());					

					EventBus eb = vertx.eventBus();
					eb.consumer(PIXEL_GRID_CHANNEL, msg -> {
						JsonObject ev = (JsonObject) msg.body();
						logger.log(Level.INFO, "Event: " + ev.encodePrettily());
						webSocket.writeTextMessage(ev.encodePrettily());
					});
				})
				.onFailure((e) -> {
					logger.log(Level.INFO, "PixelGrid subscription refused.");
					webSocket.reject();
				});
			} else {
				logger.log(Level.INFO, "PixelGrid subscription refused.");
				webSocket.reject();
			}
		});
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
	}

	/* Aux methods */
	

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

}
