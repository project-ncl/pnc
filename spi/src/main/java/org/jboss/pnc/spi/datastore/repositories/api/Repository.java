package org.jboss.pnc.spi.datastore.repositories.api;

import org.jboss.pnc.model.GenericEntity;

import java.io.Serializable;

public interface Repository<T extends GenericEntity<ID>, ID extends Serializable> extends ReadOnlyRepository<T, ID> {
    T save(T entity);
    void delete(ID id);
}
