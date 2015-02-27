package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.ProductVersionProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface ProductVersionProjectRepository extends JpaRepository<ProductVersionProject, Integer>, QueryDslPredicateExecutor<ProductVersionProject> {

}
