package org.jacpfx.vertx.spring.services;

import com.google.gson.Gson;
import org.jacpfx.model.common.*;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by amo on 28.10.14.
 */
public abstract class ServiceVerticle extends Verticle {

    private final Gson gson = new Gson();
    private ServiceInfo info;

    @Override
    public final void start() {
        // collest all service operation for descriptor
        final List<Operation> operations = getAllOperationsInService(this.getClass().getDeclaredMethods());
        info = new ServiceInfo(serviceName(), operations.toArray(new Operation[operations.size()]));
        // register service at service registry
        vertx.eventBus().send("services.registry.register", gson.toJson(info));
        // register info handler
        vertx.eventBus().registerHandler("/" + serviceName() + "-info", this::info);
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
                       final List<String> parameters = getQueryParametersInMethod(method.getParameterAnnotations());
                       parameters.addAll(getPathParametersInMethod(method.getParameterAnnotations()));
                       vertx.eventBus().registerHandler(path.path(), handler -> genericHandler(handler, method));
                       return new Operation(path.path(), path.type().name(), parameters.toArray(new String[parameters.size()]));
                   }).collect(Collectors.toList());
    }

    /**
     * Returns all query parameters in a method, this is only for REST methods
     * @param parameterAnnotations
     * @return a list of QueryParameters in a method
     */
    private List<String> getQueryParametersInMethod(final Annotation[][] parameterAnnotations) {
        final List<String> parameters = new ArrayList<>();
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
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
    private void genericHandler(Message m, Method method) {
        try {
            // TODO allow method return values an do atomatic reply ... be aware of type limitation!!
            method.invoke(this, invokePatameters(m, method));
            //if(replyValue!=null)
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
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
        for (Annotation[] parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation.length > 0) {
                // check only first parameter annotation as only one is allowed
                final Annotation annotation = parameterAnnotation[0];

                getQueryParameter(parameters, i, annotation, params);


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

    private void getQueryParameter(Object[] parameters, int counter, Annotation annotation, final Parameter<String> params) {
        if (QueryParam.class.isAssignableFrom(annotation.getClass())) {
            parameters[counter] = (params.getValue(QueryParam.class.cast(annotation).value()));
        }
    }


    private void info(Message m) {
        Logger logger = container.logger();

        m.reply(gson.toJson(info));
        logger.info("reply to: " + m.body());
    }

    protected abstract String serviceName();
}
