package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.BuildConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildConfigurationRepository extends JpaRepository<BuildConfiguration, Integer>, QueryDslPredicateExecutor<BuildConfiguration> {

}
