package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildRecord} entity.
 */
public interface BuildRecordRepository extends Repository<BuildRecord, Integer> {
}
