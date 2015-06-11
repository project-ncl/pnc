package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.ProductRelease} entity.
 */
public interface ProductReleaseRepository extends Repository<ProductRelease, Integer> {
}
