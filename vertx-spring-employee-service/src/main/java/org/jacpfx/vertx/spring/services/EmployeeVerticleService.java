package org.jacpfx.vertx.spring.services;

import com.google.gson.Gson;
import org.jacpfx.model.common.Path;
import org.jacpfx.model.common.PathParam;
import org.jacpfx.model.common.QueryParam;
import org.jacpfx.model.common.Type;
import org.jacpfx.vertx.spring.SpringVerticle;
import org.jacpfx.vertx.spring.configuration.SpringConfiguration;
import org.jacpfx.vertx.spring.repository.EmployeeRepository;
import org.springframework.stereotype.Component;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import javax.inject.Inject;

/**
 * Created by amo on 29.10.14.
 */
@Component(value = "EmployeeVerticleService")
@SpringVerticle(springConfig = SpringConfiguration.class)
public class EmployeeVerticleService extends ServiceVerticle {
    @Inject
    private EmployeeRepository repository;

    private final Gson gson = new Gson();

    @Path(path = "/employeeAll", type = Type.REST_GET)
    public void getAll(Message m) {
        Logger logger = container.logger();

        m.reply(gson.toJson(repository.getAllEmployees()));
        logger.info("reply to: " + m.body());
    }

    @Path(path = "/testEmployeeOne", type = Type.REST_GET)
    public void getTestEmployeeOne(@QueryParam("name") String name, @QueryParam("lastname") String lastname, Message message) {
        message.reply(name+":"+lastname);
    }

    // TODO implement return objects
    @Path(path = "/testEmployeeTwo", type = Type.REST_GET)
    public JsonObject getTestEmployeeTwo() {
        return null;
    }

    @Path(path = "/testEmployeeThree/:id", type = Type.REST_GET)
    public void getTestEmployeeByPathParameterOne(@PathParam("id") String id, Message message) {
        message.reply(id);
    }

    @Path(path = "/testEmployeeThree/:id/:name", type = Type.REST_GET)
    public void getTestEmployeeByPathParameterTwo(Message message, @PathParam("id") String id,@PathParam("name") String name) {
        message.reply(id+":"+name);
    }

    @Path(path = "/testEmployeeFour/:id/employee/:name", type = Type.REST_GET)
    public void getTestEmployeeByPathParameterThree(Message message, @PathParam("id") String id,@PathParam("name") String name) {
        message.reply(id+":"+name);
    }


    @Path(path = "/employeeByName", type = Type.REST_GET)
    public void findByName(@QueryParam("name") String name, @QueryParam("lastname") String lastname, Message message) {
        Logger logger = container.logger();
        logger.info("parameter name: " + name + "  : " + lastname);
        message.reply(gson.toJson(repository.findEmployeeByFirstName(name)));
        logger.info("reply to: " + message.body());
    }



    @Override
    public String serviceName() {
        return "service-employee";
    }
}
