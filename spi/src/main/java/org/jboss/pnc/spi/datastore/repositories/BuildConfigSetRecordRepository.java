package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildConfigSetRecord} entity.
 */
public interface BuildConfigSetRecordRepository extends Repository<BuildConfigSetRecord, Integer> {
}
