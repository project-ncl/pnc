package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.ProductRelease;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface ProductReleaseRepository extends JpaRepository<ProductRelease, Integer>, QueryDslPredicateExecutor<ProductRelease> {

}
