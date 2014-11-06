package org.jacpfx.vertx.spring.services;

import com.google.gson.Gson;
import org.jacpfx.model.common.OperationType;
import org.jacpfx.model.common.Type;
import org.jacpfx.vertx.spring.SpringVerticle;
import org.jacpfx.vertx.spring.configuration.SpringConfiguration;
import org.jacpfx.vertx.spring.model.Employee;
import org.jacpfx.vertx.spring.repository.EmployeeRepository;
import org.springframework.stereotype.Component;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import javax.inject.Inject;
import javax.ws.rs.*;

/**
 * Created by amo on 29.10.14.
 */
@Component(value = "RESTGetVerticleService")
@SpringVerticle(springConfig = SpringConfiguration.class)
@ApplicationPath("/service-REST-GET")
public class RESTGetVerticleService extends ServiceVerticle {
    @Inject
    private EmployeeRepository repository;

    private final Gson gson = new Gson();


    @Path("/testEmployeeOne")
    @OperationType(Type.REST_GET)
    @Produces("application/json")
    public void getTestEmployeeOne(@QueryParam("name") String name, @QueryParam("lastname") String lastname, Message message) {
        message.reply(name + ":" + lastname);
    }


    @Path("/testEmployeeTwo")
    @OperationType(Type.REST_GET)
    @Produces("application/json")
    public JsonObject getTestEmployeeTwo(@PathParam("id") String id) {
        return new JsonObject().putString("id",id);
    }


    @Path("/testEmployeeThree/:id")
    @OperationType(Type.REST_GET)
    public void getTestEmployeeByPathParameterOne(@PathParam("id") String id, Message message) {
        message.reply(id);
    }


    @Path("/testEmployeeThree/:id/:name")
    @OperationType(Type.REST_GET)
    public void getTestEmployeeByPathParameterTwo(Message message, @PathParam("id") String id, @PathParam("name") String name) {
        message.reply(id + ":" + name);
    }


    @Path("/testEmployeeFour/:id/employee/:name")
    @OperationType(Type.REST_GET)
    public void getTestEmployeeByPathParameterThree(Message message, @PathParam("id") String id, @PathParam("name") String name) {
        message.reply(id + ":" + name);
    }

    @Path("/testEmployeeFive")
    @OperationType(Type.REST_GET)
    @Produces("application/json")
    public Employee getTestEmployeeFive(@PathParam("id") String id) {
        return new Employee("fg","dfg",null,"dfg","fdg","dfg");
    }


}
