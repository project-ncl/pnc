package org.jboss.pnc.rest.repository;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Empty implementation of a RSQL adapter
 *
 * <p>
 *     Converts RSQL query into Spring Data's {@link org.springframework.data.jpa.domain.Specification}, which in turn
 *     might be used for selecting records.
 * </p>
 * @param <Entity> An entity type.
 */
class EmptyRSQLAdapter<Entity> implements RSQLAdapter<Entity> {

   @Override
   public Predicate toPredicate(Root<Entity> r, CriteriaQuery<?> cq, CriteriaBuilder cb) {
      return cb.conjunction();
   }

}
