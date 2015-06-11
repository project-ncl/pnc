package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ProductVersionSpringRepository;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductVersionRepositoryImpl extends AbstractRepository<ProductVersion, Integer> implements
        ProductVersionRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public ProductVersionRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public ProductVersionRepositoryImpl(ProductVersionSpringRepository productVersionSpringRepository) {
        super(productVersionSpringRepository, productVersionSpringRepository);
    }
}
