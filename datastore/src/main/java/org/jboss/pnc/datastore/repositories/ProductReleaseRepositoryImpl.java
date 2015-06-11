package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ProductReleaseSpringRepository;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.spi.datastore.repositories.ProductReleaseRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductReleaseRepositoryImpl extends AbstractRepository<ProductRelease, Integer> implements ProductReleaseRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public ProductReleaseRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public ProductReleaseRepositoryImpl(ProductReleaseSpringRepository productReleaseSpringRepository) {
        super(productReleaseSpringRepository, productReleaseSpringRepository);
    }
}
