package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.ProductVersion} entity.
 */
public interface ProductVersionRepository extends Repository<ProductVersion, Integer> {
}
