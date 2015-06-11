package org.jboss.pnc.spi.datastore.repositories.api;

import org.jboss.pnc.model.GenericEntity;

import java.io.Serializable;
import java.util.List;

public interface ReadOnlyRepository<T extends GenericEntity<ID>, ID extends Serializable> {
    List<T> queryAll();
    List<T> queryAll(PageInfo pageInfo, SortInfo sortInfo);
    T queryById(ID id);
    T queryByPredicates(Predicate<T>... predicates);
    int count(Predicate<T>... predicates);
    List<T> queryWithPredicates(Predicate<T>... predicates);
    List<T> queryWithPredicates(PageInfo pageInfo, SortInfo sortInfo, Predicate<T>... predicates);
}
