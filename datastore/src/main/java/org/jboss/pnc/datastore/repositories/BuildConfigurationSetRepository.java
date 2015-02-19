package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildConfigurationSetRepository extends JpaRepository<BuildConfigurationSet, Integer>, QueryDslPredicateExecutor<BuildConfigurationSet> {

}
