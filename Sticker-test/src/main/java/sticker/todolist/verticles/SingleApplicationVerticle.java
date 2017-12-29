package sticker.todolist.verticles;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import sticker.core.db.model.Todo;
import sticker.todolist.Constants;

public class SingleApplicationVerticle extends AbstractVerticle{

	private static final String HTTP_HOST = "localhost";
	  private static final String REDIS_HOST = "127.0.0.1";
	  private static final int HTTP_PORT = 8888;
	  private static final int REDIS_PORT = 6379;

	  private RedisClient redis;

	  @Override
	  public void start(Future<Void> future) throws Exception {
		  initData();
		  Router router = Router.router(vertx); // <1>
		  // CORS support
		  Set<String> allowHeaders = new HashSet<>();
		  allowHeaders.add("x-requested-with");
		  allowHeaders.add("Access-Control-Allow-Origin");
		  allowHeaders.add("origin");
		  allowHeaders.add("Content-Type");
		  allowHeaders.add("accept");
		  Set<HttpMethod> allowMethods = new HashSet<>();
		  allowMethods.add(HttpMethod.GET);
		  allowMethods.add(HttpMethod.POST);
		  allowMethods.add(HttpMethod.DELETE);
		  allowMethods.add(HttpMethod.PATCH);

		  router.route().handler(CorsHandler.create("*") // <2>
		    .allowedHeaders(allowHeaders)
		    .allowedMethods(allowMethods));
		  router.route().handler(BodyHandler.create()); // <3>

		// routes
		  router.get(Constants.API_GET).handler(this::handleGetTodo);
		  router.get(Constants.API_LIST_ALL).handler(this::handleGetAll);
		  router.post(Constants.API_CREATE).handler(this::handleCreateTodo);
		  router.patch(Constants.API_UPDATE).handler(this::handleUpdateTodo);
		  router.delete(Constants.API_DELETE).handler(this::handleDeleteOne);
		  router.delete(Constants.API_DELETE_ALL).handler(this::handleDeleteAll);

		  vertx.createHttpServer() // <4>
		    .requestHandler(router::accept)
		    .listen(HTTP_PORT, HTTP_HOST, result -> {
		        if (result.succeeded())
		          future.complete();
		        else
		          future.fail(result.cause());
		      });
	  }
	  private void initData() {
		  RedisOptions config = new RedisOptions()
		      .setHost(config().getString("redis.host", REDIS_HOST)) // redis host
		      .setPort(config().getInteger("redis.port", REDIS_PORT)); // redis port

		  this.redis = RedisClient.create(vertx, config); // create redis client

		  redis.hset(Constants.REDIS_TODO_KEY, "24", Json.encodePrettily( // test connection
		    new Todo(24, "Something to do...", false, 1, "todo/ex")), res -> {
		    if (res.failed()) {
		    	System.out.println("Redis service is not running!");
//		      LOGGER.error("Redis service is not running!");
		      res.cause().printStackTrace();
		    }
		  });

		}
	  private void handleGetTodo(RoutingContext context) {
		  String todoID = context.request().getParam("todoId"); // (1)
		  if (todoID == null)
		    sendError(400, context.response()); // (2)
		  else {
		    redis.hget(Constants.REDIS_TODO_KEY, todoID, x -> { // (3)
		      if (x.succeeded()) {
		        String result = x.result();
		        if (result == null)
		          sendError(404, context.response());
		        else {
		          context.response()
		            .putHeader("content-type", "application/json")
		            .end(result); // (4)
		        }
		      } else
		        sendError(503, context.response());
		    });
		  }
		}
	  private void handleGetAll(RoutingContext context) {
		    // ...
		} 
	  private void handleCreateTodo(RoutingContext context) {
		    // ...
		} 
	  private void handleUpdateTodo(RoutingContext context) {
		    // ...
		} 
	  private void handleDeleteOne(RoutingContext context) {
		    // ...
		}
	  private void handleDeleteAll(RoutingContext context) {
		    // ...
		} 
	  private void sendError(int statusCode, HttpServerResponse response) {
		  response.setStatusCode(statusCode).end();
		}
}
