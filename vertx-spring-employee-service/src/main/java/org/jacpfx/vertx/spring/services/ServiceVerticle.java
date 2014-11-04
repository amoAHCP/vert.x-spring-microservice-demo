package org.jacpfx.vertx.spring.services;

import com.google.gson.Gson;
import org.jacpfx.model.common.*;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import javax.ws.rs.*;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by amo on 28.10.14.
 */
public abstract class ServiceVerticle extends Verticle {

    private final Gson gson = new Gson();
    private ServiceInfo info;
    private static final String HOST_PREFIX="";

    @Override
    public final void start() {
        // collest all service operation for descriptor
        final List<Operation> operations = getAllOperationsInService(this.getClass().getDeclaredMethods());
        info = new ServiceInfo(serviceName(), operations.toArray(new Operation[operations.size()]));
        // register service at service registry
        vertx.eventBus().send("services.registry.register", gson.toJson(info));
        // register info handler
        vertx.eventBus().registerHandler(serviceName() + "-info", this::info);
    }

    /**
     * Scans all method in ServiceVerticle, checks method signature, registers each path and create for each method a operation objects for service information.
     * @param allMethods methods in serviceVerticle
     * @return a list of all operation in service
     */
    private List<Operation> getAllOperationsInService(final Method[] allMethods) {
           return Stream.of(allMethods).
                   filter(m -> m.isAnnotationPresent(Path.class)).
                   map(method -> {
                       final Path path = method.getDeclaredAnnotation(Path.class);
                       final Produces mime = method.getDeclaredAnnotation(Produces.class);
                       final OperationType opType = method.getDeclaredAnnotation(OperationType.class);
                       if (opType == null)
                           throw new MissingResourceException("missing OperationType ", this.getClass().getName(), "");
                       final String[] mimeTypes = mime != null ? mime.value() : new String[]{""};
                       final String url = serviceName().concat(path.value());
                       final List<String> parameters = getQueryParametersInMethod(method.getParameterAnnotations());
                       parameters.addAll(getPathParametersInMethod(method.getParameterAnnotations()));
                       switch (opType.value()) {
                           case REST_POST:
                               vertx.eventBus().registerHandler(url, handler -> genericRESTHandler(handler, method));
                               break;
                           case REST_GET:
                               vertx.eventBus().registerHandler(url, handler -> genericRESTHandler(handler, method));
                               break;
                           case WEBSOCKET:
                               break;
                           case EVENTBUS:
                               break;
                       }

                       return new Operation(url, opType.value().name(), mimeTypes, parameters.toArray(new String[parameters.size()]));
                   }).collect(Collectors.toList());
    }

    /**
     * Returns all query parameters in a method, this is only for REST methods
     * @param parameterAnnotations
     * @return a list of QueryParameters in a method
     */
    private List<String> getQueryParametersInMethod(final Annotation[][] parameterAnnotations) {
        final List<String> parameters = new ArrayList<>();
        for (final Annotation[] parameterAnnotation : parameterAnnotations) {
            parameters.addAll(Stream.of(parameterAnnotation).
                    filter(pa -> QueryParam.class.isAssignableFrom(pa.getClass())).
                    map(parameter -> QueryParam.class.cast(parameter).value()).
                    collect(Collectors.toList()));
        }
        return parameters;
    }

    /**
     * Returns all path parameters in a method, this is only for REST methods
     * @param parameterAnnotations
     * @return a list of PathParameters in a method
     */
    private List<String> getPathParametersInMethod(final Annotation[][] parameterAnnotations) {
        final List<String> parameters = new ArrayList<>();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            parameters.addAll(Stream.of(parameterAnnotation).
                    filter(pa -> PathParam.class.isAssignableFrom(pa.getClass())).
                    map(parameter -> PathParam.class.cast(parameter).value()).
                    collect(Collectors.toList()));
        }
        return parameters;
    }

    /**
     * executes a requested Service Method in ServiceVerticle
     * @param m
     * @param method
     */
    private void genericRESTHandler(Message m, Method method) {
        try {
            // TODO allow method return values an do atomatic reply ... be aware of type limitation!!
            Object replyValue = method.invoke(this, invokePatameters(m, method));
            if(replyValue!=null) {
                if(TypeTool.isCompatibleRESTReturnType(replyValue)) {
                   m.reply(replyValue);
                } else {
                    // TODO try to serialize to REST
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            m.fail(200,e.getMessage());
        } catch (InvocationTargetException e) {
            m.fail(200, e.getMessage());
        }
    }



    /**
     * checks method parameters an request parameters for method invocation
     * @param m the message
     * @param method the service method
     * @return an array with all valid method parameters
     */
    private Object[] invokePatameters(Message m, Method method) {
        final Parameter<String> params = gson.fromJson(m.body().toString(), Parameter.class);
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        final Class[] parameterTypes = method.getParameterTypes();
        final Object[] parameters = new Object[parameterAnnotations.length];

        int i = 0;
        for (final Annotation[] parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation.length > 0) {
                // check only first parameter annotation as only one is allowed
                final Annotation annotation = parameterAnnotation[0];
                putQueryParameter(parameters, i, annotation, params);
                putPathParameter(parameters, i, annotation, params);


            } else {
                final Class typeClass = parameterTypes[i];
                if (typeClass.isAssignableFrom(m.getClass())) {
                    parameters[i] = m;
                }
            }
            i++;
        }
        return parameters;
    }

    private void putQueryParameter(Object[] parameters, int counter, Annotation annotation, final Parameter<String> params) {
        if (QueryParam.class.isAssignableFrom(annotation.getClass())) {
            parameters[counter] = (params.getValue(QueryParam.class.cast(annotation).value()));
        }
    }

    private void putPathParameter(Object[] parameters, int counter, Annotation annotation, final Parameter<String> params) {
        if (PathParam.class.isAssignableFrom(annotation.getClass())) {
            parameters[counter] = (params.getValue(PathParam.class.cast(annotation).value()));
        }
    }


    private void info(Message m) {
        Logger logger = container.logger();

        m.reply(gson.toJson(info));
        logger.info("reply to: " + m.body());
    }

    protected String serviceName(){
         if(this.getClass().isAnnotationPresent(ApplicationPath.class)) {
             final JsonObject config = container.config();
             final String host = config.getString("host-prefix", HOST_PREFIX);
             final ApplicationPath path = this.getClass().getAnnotation(ApplicationPath.class);
             return host.length()>1?"/".concat(host).concat("-").concat(path.value()):path.value();
         }
        return null;
    }
}
