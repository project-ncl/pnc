package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.BuildRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildRecordRepository extends JpaRepository<BuildRecord, Integer>, QueryDslPredicateExecutor<BuildRecord> {

}
