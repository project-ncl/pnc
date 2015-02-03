package org.jboss.pnc.rest.repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.List;

@FunctionalInterface
interface Transformer<Entity> {
    Predicate transform(Root<Entity> r, CriteriaBuilder cb, String operand, List<String> arguments);
}
