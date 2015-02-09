package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface LicenseRepository extends JpaRepository<License, Integer>, QueryDslPredicateExecutor<License> {

}
