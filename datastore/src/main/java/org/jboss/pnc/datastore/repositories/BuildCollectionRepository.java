package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.BuildCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface BuildCollectionRepository extends JpaRepository<BuildCollection, Integer>, QueryDslPredicateExecutor<BuildCollection> {
}
