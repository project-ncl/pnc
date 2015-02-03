package org.jboss.pnc.rest.repository;

import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * Performs a transformation between RSQL and JPA's Predicates.
 */
@FunctionalInterface
public interface RSQLAdapter<Entity> extends Specification<Entity> {

    /**
     * Creates new Predicate.
     *
     * @param r Root for selection.
     * @param cq For complicated Criteria Queries.
     * @param cb For building queries.
     * @return A predicate for filtering DB results.
     */
    Predicate toPredicate(Root<Entity> r, CriteriaQuery<?> cq, CriteriaBuilder cb);
}
