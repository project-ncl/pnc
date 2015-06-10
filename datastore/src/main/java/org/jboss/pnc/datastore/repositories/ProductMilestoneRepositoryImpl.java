package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ProductMilestoneSpringRepository;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductMilestoneRepositoryImpl extends AbstractRepository<ProductMilestone, Integer> implements ProductMilestoneRepository {

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public ProductMilestoneRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public ProductMilestoneRepositoryImpl(ProductMilestoneSpringRepository productMilestoneSpringRepository) {
        super(productMilestoneSpringRepository, productMilestoneSpringRepository);
    }
}
