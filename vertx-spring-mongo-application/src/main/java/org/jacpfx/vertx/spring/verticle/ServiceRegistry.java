package org.jacpfx.vertx.spring.verticle;

import com.google.gson.Gson;
import org.jacpfx.model.common.ServiceInfo;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;
import org.vertx.java.platform.Verticle;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by amo on 22.10.14.
 */
public class ServiceRegistry extends Verticle {
    private static final Logger log = LoggerFactory.getLogger(ServiceRegistry.class);

    public static final long DEFAULT_EXPIRATION_AGE = 5000;
    public static final long DEFAULT_PING_TIME = 1000;
    public static final long DEFAULT_SWEEP_TIME = 0;
    // Our own addresses
    public static final String SERVICE_REGISTRY_EXPIRED = "services.registry.expired";
    public static final String SERVICE_REGISTRY_PING = "services.registry.ping";
    public static final String SERVICE_REGISTRY_SEARCH = "services.registry.search";
    public static final String SERVICE_REGISTRY_GET = "services.registry.get";
    public static final String SERVICE_REGISTRY_REGISTER = "services.registry.register";
    public static final String SERVICE_REGISTRY = "services.registry";

    private Map<String, Long> handlers;

    long expiration_age = DEFAULT_EXPIRATION_AGE;
    long ping_time = DEFAULT_PING_TIME;
    long sweep_time = DEFAULT_SWEEP_TIME;

    private final Gson gson = new Gson();

    @Override
    public void start() {
        handlers = vertx.sharedData().getMap(SERVICE_REGISTRY);
        log.info("Service registry started.");

        JsonObject config = container.config();
        expiration_age = config.getLong("expiration", DEFAULT_EXPIRATION_AGE);
        ping_time = config.getLong("ping", DEFAULT_PING_TIME);
        sweep_time = config.getLong("sweep", DEFAULT_SWEEP_TIME);


        vertx.eventBus().registerHandler(SERVICE_REGISTRY_REGISTER, this::serviceRegister);
        vertx.eventBus().registerHandler(SERVICE_REGISTRY_GET, this::getServicesInfo);
        pingService();
    }

    private void getServicesInfo(Message<String> message) {
        message.reply(gson.toJson(handlers.keySet()));
    }


    private void serviceRegister(Message<String> message) {
        if(!handlers.containsKey(message.body().toString())) {
            handlers.put(message.body(), System.currentTimeMillis());
            vertx.eventBus().send("services.register.handler", message.body());
            log.info("EventBus registered address: " + message.body());

        }

    }

    private void pingService() {
        vertx.setPeriodic(ping_time, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
                long expired = System.currentTimeMillis() - expiration_age;

                Iterator<Map.Entry<String, Long>> it = handlers.entrySet()
                        .iterator();

                while (it.hasNext()) {
                    Map.Entry<String, Long> entry = it.next();
                    if ((entry.getValue() == null)
                            || (entry.getValue().longValue() < expired)) {
                        // vertx's SharedMap instances returns a copy internally, so we must remove by hand
                        final ServiceInfo info = gson.fromJson(entry.getKey(), ServiceInfo.class);
                        handlers.remove(entry.getKey());
                        vertx.
                                eventBus().
                                sendWithTimeout(
                                        "/" + info.getServiceName() + "-info",
                                        "ping",
                                        1000,
                                        (Handler<AsyncResult<Message<String>>>) event -> {
                                            if (event.succeeded()) {
                                                log.info("ping: " + info.getServiceName());
                                                handlers.put(event.result().body(), System.currentTimeMillis());
                                            } else {
                                                log.info("ping error: " + info.getServiceName());
                                                // handler.response().end("error");
                                            }
                                        });
                    }
                }
            }

        });
    }
}
