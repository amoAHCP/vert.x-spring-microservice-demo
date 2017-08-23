package org.jacpfx.vertx.spring.services;

import com.google.gson.Gson;
import org.jacpfx.model.common.Operation;
import org.jacpfx.model.common.Parameter;
import org.jacpfx.model.common.ServiceInfo;
import org.jacpfx.vertx.spring.repository.EmployeeRepository;
import org.springframework.stereotype.Component;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import javax.inject.Inject;


/**
 * Created by amo on 09.10.14.
 */
@Component(value = "EmployeeService")
public class EmployeeService extends Verticle {

    @Inject
    private EmployeeRepository repository;

    private final Gson gson = new Gson();

    private ServiceInfo info =  new ServiceInfo("service-employee",new Operation("/employeeAll", "POST",new String[1]), new Operation("/employeeByName","POST",new String[1]));

    @Override
    public void start() {
        vertx.eventBus().registerHandler("/employeeAll", this::getAll);
        vertx.eventBus().registerHandler("/employeeByName", this::findByName);
        vertx.eventBus().registerHandler("/service-employee-info", this::info);
        vertx.eventBus().send("services.registry.register",gson.toJson(info));
    }

    private void info(Message m) {
        Logger logger = container.logger();

        m.reply(gson.toJson(info));
        logger.info("reply to: " + m.body());
    }


    private void getAll(Message m) {
        Logger logger = container.logger();

        m.reply(gson.toJson(repository.getAllEmployees()));
        logger.info("reply to: " + m.body());
    }

    private void findByName(Message m) {
        final Parameter<String> params = gson.fromJson(m.body().toString(), Parameter.class);
        Logger logger = container.logger();

        m.reply(gson.toJson(repository.findEmployeeByFirstName(params.getValue("name"))));
        logger.info("reply to: " + m.body());
    }
}
