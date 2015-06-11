package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildConfigurationSet} entity.
 */
public interface BuildConfigurationSetRepository extends Repository<BuildConfigurationSet, Integer> {
}
