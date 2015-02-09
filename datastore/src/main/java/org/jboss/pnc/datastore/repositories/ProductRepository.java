package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;

/*
 * How to use Spring Data: https://speakerdeck.com/olivergierke/spring-data-repositories-a-deep-dive-2
 */
public interface ProductRepository extends JpaRepository<Product, Integer>, QueryDslPredicateExecutor<Product> {
}
