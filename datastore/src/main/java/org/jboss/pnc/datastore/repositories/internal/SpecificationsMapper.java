package org.jboss.pnc.datastore.repositories.internal;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpecificationsMapper {

    public static <T extends GenericEntity<? extends Serializable>> Specification<T> map(Predicate<T>... predicates) {
        return (root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> jpaPredicates = Stream.of(predicates)
                    .map(predicate -> predicate.apply(root, query, cb)).collect(Collectors.toList());
            return cb.and(jpaPredicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
    }

}
