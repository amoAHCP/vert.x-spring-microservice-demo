package org.jacpfx.vertx.spring.rest;

import com.google.gson.Gson;
import org.jacpfx.model.common.Parameter;
import org.jacpfx.model.common.ServiceInfo;
import org.jacpfx.model.common.Type;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
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
    private final  RouteMatcher routeMatcher = new RouteMatcher();
    private void serviceRegisterHandler(Message<String> message) {
        final ServiceInfo info = gson.fromJson(message.body(), ServiceInfo.class);
        Stream.of(info.getOperations()).forEach(operation->{
                  String type = operation.getType(); 
                    switch (Type.valueOf(type)){
                        case REST_GET:
                            final String url = operation.getUrl();
                             routeMatcher.get(url, request->{

                                 vertx.
                                         eventBus().
                                         sendWithTimeout(
                                                 url,
                                                 gson.toJson(getParameterEntity(request.params())),
                                                 10000,
                                                 (Handler<AsyncResult<Message<String>>>) event -> {
                                                     request.response().putHeader("content-type", "text/json");
                                                     if (event.succeeded()) {
                                                         request.response().end(event.result().body());
                                                     } else {

                                                         request.response().end("error");
                                                     }
                                                 });
                             });
                            break;
                        case REST_POST:
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

    @Override
    public void start() {
        System.out.println("START RestEntryVerticle  THREAD: " + Thread.currentThread() + "  this:" + this );
        
        vertx.eventBus().registerHandler(SERVICE_REGISTER_HANDLER,this::serviceRegisterHandler);
        
        HttpServer server = vertx.createHttpServer();


        routeMatcher.get("/serviceInfo", request->{
            request.response().putHeader("content-type", "text/json");
            vertx.eventBus().send("services.registry.get","xyz",(Handler<Message<String>>)h->{
                request.response().end(h.body());
            });
        });

        // Test1
        routeMatcher.get("/test/:id", request->{
            System.out.println("path: "+request.path()+"  paramater id:"+request.params().get("id"));
            request.response().end();
        });

        // Test2
        routeMatcher.get("/test/:id/xyz/:name", request->{
            System.out.println("path: "+request.path()+"  paramater id:"+request.params().get("id")+"  paramater name:"+request.params().get("name"));
            request.response().end();
        });

        /**
         *
         * Idea: instead of passing all url user the ServiceRegistry (subscribe for notification), for any new service register by type with http://blog.zenika.com/index.php?post/2013/02/12/Rest-with-Scala-and-Vert.x RouteMatcher
         */
        /*server.requestHandler(handler -> {
                    handler.response().putHeader("content-type", "text/json");


                    final String path = handler.path();
                    if (path.equals("/serviceInfo")) {
                        vertx.eventBus().send("services.registry.get","xyz",(Handler<Message<String>>)h->{
                            handler.response().end(h.body());
                        });
                    } else {


                        vertx.
                                eventBus().
                                sendWithTimeout(
                                        path,
                                        gson.toJson(getParameterEntity(handler.params())),
                                        10000,
                                        (Handler<AsyncResult<Message<String>>>) event -> {
                                            if (event.succeeded()) {
                                                handler.response().end(event.result().body());
                                            } else {

                                                handler.response().end("error");
                                            }
                                        });
                    }
                    System.out.println("http method: " + handler.method());
                }

        ).listen(8080, "localhost", asyncResult -> System.out.println("Listen succeeded? " + asyncResult.succeeded()));
*/
        server.requestHandler(routeMatcher).listen(8080, "localhost");
        this.container.deployVerticle("org.jacpfx.vertx.spring.verticle.ServiceRegistry");
    }

    private Parameter<String> getParameterEntity(final MultiMap params) {
        final List<Parameter<String>> parameters = params.entries().stream().map(entry -> new Parameter<String>(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        return new Parameter<>(parameters);
    }
}
