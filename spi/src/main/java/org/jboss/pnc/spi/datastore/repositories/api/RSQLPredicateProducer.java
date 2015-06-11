package org.jboss.pnc.spi.datastore.repositories.api;

import org.jboss.pnc.model.GenericEntity;

public interface RSQLPredicateProducer {
    <T extends GenericEntity<? extends Number>> Predicate<T> getPredicate(Class<T> selectingClass, String rsql);
}
