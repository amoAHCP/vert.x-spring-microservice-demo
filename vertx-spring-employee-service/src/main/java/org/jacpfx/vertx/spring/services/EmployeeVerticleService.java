package org.jacpfx.vertx.spring.services;

import com.google.gson.Gson;
import org.jacpfx.model.common.OperationType;
import org.jacpfx.model.common.Type;
import org.jacpfx.vertx.spring.SpringVerticle;
import org.jacpfx.vertx.spring.configuration.SpringConfiguration;
import org.jacpfx.vertx.spring.repository.EmployeeRepository;
import org.springframework.stereotype.Component;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by amo on 29.10.14.
 */
@Component(value = "EmployeeVerticleService")
@SpringVerticle(springConfig = SpringConfiguration.class)
@ApplicationPath("/service-employee")
public class EmployeeVerticleService extends ServiceVerticle {
    @Inject
    private EmployeeRepository repository;

    private final Gson gson = new Gson();

    @Path("/employeeAll")
    @OperationType(Type.REST_GET)
    //@Produces("text/json")
    public void getAll(Message m) {
        Logger logger = container.logger();
        m.reply(gson.toJson(repository.getAllEmployees()));
        logger.info("reply to: " + m.body());
    }



    @Path("/employeeByName")
   // @Produces("text/json")
    @OperationType(Type.REST_GET)
    public void findByName(@QueryParam("name") String name, @QueryParam("lastname") String lastname, Message message) {
        Logger logger = container.logger();
        logger.info("parameter name: " + name + "  : " + lastname);
        message.reply(gson.toJson(repository.findEmployeeByFirstName(name)));
        logger.info("reply to: " + message.body());
    }

}
