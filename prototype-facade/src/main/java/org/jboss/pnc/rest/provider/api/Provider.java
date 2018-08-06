package org.jboss.pnc.rest.provider.api;

import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface Provider<DB, Rest> {

    void delete(Integer id) throws RestValidationException;

    CollectionInfo<Rest> getAll(int pageIndex, int pageSize, String sortingRsql, String query);

    Rest getSpecific(Integer id);

    CollectionInfo<Rest> queryForCollection(int pageIndex, int pageSize, String sortingRsql, String query, Predicate<DB>... predicates);

    Integer store(Rest restEntity) throws RestValidationException;

    void update(Integer id, Rest restEntity) throws RestValidationException;

}
