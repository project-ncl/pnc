package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.License;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.License} entity.
 */
public interface LicenseRepository extends Repository<License, Integer> {
}
