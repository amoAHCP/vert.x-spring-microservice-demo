package org.jacpfx.vertx.spring.repository;

import org.jacpfx.vertx.spring.configuration.MongoRepositoryConfiguration;
import org.jacpfx.vertx.spring.model.Employee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Created by amo on 09.10.14.
 */
@Repository
@Import({MongoRepositoryConfiguration.class})
@Qualifier("EmployeeRepository")
public class EmployeeRepository {
    @Inject
    private MongoTemplate mongoTemplate;

    private final Class<Employee> entityClass = Employee.class;


    public void bulkCreateEmployees(List<Employee> employees) {
        mongoTemplate.insert(employees, entityClass);
    }

    public Collection<Employee> getAllEmployees() {
        try {
            return mongoTemplate.findAll(entityClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Employee> findEmployeeByFirstName(String firstName) {
        return mongoTemplate.find(new Query(Criteria.where("firstName").regex(firstName)), Employee.class);
    }
}
