package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ProductSpringRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductRepositoryImpl extends AbstractRepository<Product, Integer> implements ProductRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public ProductRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public ProductRepositoryImpl(ProductSpringRepository productSpringRepository) {
        super(productSpringRepository, productSpringRepository);
    }
}
