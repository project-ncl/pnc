package org.jboss.pnc.spi.datastore.repositories.api;

import org.jboss.pnc.model.GenericEntity;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;

public interface Predicate<T extends GenericEntity<? extends Serializable>> {
    javax.persistence.criteria.Predicate apply(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb);
}
