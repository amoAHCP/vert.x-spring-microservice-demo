package org.jacpfx.vertx.spring.rest;

import com.google.gson.Gson;
import org.jacpfx.model.common.Parameter;
import org.jacpfx.model.common.ServiceInfo;
import org.jacpfx.model.common.Type;
import org.jacpfx.model.common.TypeTool;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by amo on 26.09.14.
 */
public class RestEntryVerticle extends Verticle {
    private final Gson gson = new Gson();
    public static final String SERVICE_REGISTER_HANDLER = "services.register.handler";
    public static final String SERVICE_UNREGISTER_HANDLER = "services.unregister.handler";
    private final RouteMatcher routeMatcher = new RouteMatcher();

    private void serviceRegisterHandler(Message<String> message) {
        final ServiceInfo info = gson.fromJson(message.body(), ServiceInfo.class);
        final EventBus eventBus = vertx.eventBus();
        Stream.of(info.getOperations()).forEach(operation -> {
                    String type = operation.getType();
                    final String url = operation.getUrl();
                    switch (Type.valueOf(type)) {
                        case REST_GET:
                            routeMatcher.get(url, request -> {
                                eventBus.
                                        sendWithTimeout(
                                                url,
                                                gson.toJson(getParameterEntity(request.params())),
                                                10000,
                                                (Handler<AsyncResult<Message<Object>>>) event -> {
                                                    if(operation.getMime()!=null && operation.getMime().length>1)request.response().putHeader("content-type", operation.getMime()[0]);
                                                    handleRESTEvent(event, request);
                                                });
                            });
                            break;
                        case REST_POST:
                            routeMatcher.post(url, request -> {
                                eventBus.
                                        sendWithTimeout(
                                                url,
                                                gson.toJson(getParameterEntity(request.params())),
                                                10000,
                                                (Handler<AsyncResult<Message<Object>>>) event -> {
                                                    if(operation.getMime()!=null && operation.getMime().length>1)request.response().putHeader("content-type", operation.getMime()[0]);
                                                    handleRESTEvent(event, request);
                                                });
                            });
                            break;
                        case EVENTBUS:
                            break;
                        case WEBSOCKET:
                            break;
                        default:


                    }
                }
        );

    }

    private void handleRESTEvent(AsyncResult<Message<Object>> event, HttpServerRequest request) {
        if (event.succeeded()) {
            final Object result = event.result().body();
            if(result==null) request.response().end();
            final String stringResult = TypeTool.trySerializeToString(result);
            if(stringResult!=null) {
                request.response().end(stringResult);
            } else {
                request.response().end();
            }

        } else {

            request.response().end("error");
        }
    }

    @Override
    public void start() {
        System.out.println("START RestEntryVerticle  THREAD: " + Thread.currentThread() + "  this:" + this);

        vertx.eventBus().registerHandler(SERVICE_REGISTER_HANDLER, this::serviceRegisterHandler);

        HttpServer server = vertx.createHttpServer();
        routeMatcher.get("/serviceInfo", this::registerInfoHandler);
        routeMatcher.noMatch(handler->handler.response().end("no route found"));
        server.requestHandler(routeMatcher).listen(8080, "localhost");

        this.container.deployVerticle("org.jacpfx.vertx.spring.verticle.ServiceRegistry");
    }

    private void registerInfoHandler(HttpServerRequest request) {
        request.response().putHeader("content-type", "text/json");
        vertx.eventBus().send("services.registry.get", "xyz", (Handler<Message<String>>) h -> {
            request.response().end(h.body());
        });
    }

    private Parameter<String> getParameterEntity(final MultiMap params) {
        final List<Parameter<String>> parameters = params.
                entries().
                stream().
                map(entry -> new Parameter<>(entry.getKey(), entry.getValue())).
                collect(Collectors.toList());
        return new Parameter<>(parameters);
    }

    private void registerWebSocketHandler(HttpServer server) {
        server.websocketHandler((serverSocket) -> {
            final String path = serverSocket.path();
            switch (path) {
                case "/all":

                    System.out.println("Call");
                    serverSocket.dataHandler(data -> {
                        System.out.println("DataHandler");
                        serverSocket.writeTextFrame("hallo2");
                    });
                    break;
            }
        });
    }
}
