package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildConfiguration} entity.
 */
public interface BuildConfigurationRepository extends Repository<BuildConfiguration, Integer> {
}
