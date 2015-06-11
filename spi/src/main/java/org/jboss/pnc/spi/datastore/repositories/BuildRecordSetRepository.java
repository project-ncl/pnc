package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildRecordSet} entity.
 */
public interface BuildRecordSetRepository extends Repository<BuildRecordSet, Integer> {
}
