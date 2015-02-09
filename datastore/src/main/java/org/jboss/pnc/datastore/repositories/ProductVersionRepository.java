package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.ProductVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface ProductVersionRepository extends JpaRepository<ProductVersion, Integer>, QueryDslPredicateExecutor<ProductVersion> {

}
