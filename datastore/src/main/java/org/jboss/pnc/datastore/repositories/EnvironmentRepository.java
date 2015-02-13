package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Environment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface EnvironmentRepository extends JpaRepository<Environment, Integer>, QueryDslPredicateExecutor<Environment> {

}
