package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.ProductMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

public interface ProductMilestoneRepository extends JpaRepository<ProductMilestone, Integer>, QueryDslPredicateExecutor<ProductMilestone> {

}
