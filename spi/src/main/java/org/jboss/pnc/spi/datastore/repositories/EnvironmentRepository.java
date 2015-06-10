package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Environment} entity.
 */
public interface EnvironmentRepository extends Repository<Environment, Integer> {
}
