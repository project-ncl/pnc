package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Product} entity.
 */
public interface ProductRepository extends Repository<Product, Integer> {
}
