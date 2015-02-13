package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.BuildRecordSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildRecordSetRepository extends JpaRepository<BuildRecordSet, Integer>, QueryDslPredicateExecutor<BuildRecordSet> {
}
