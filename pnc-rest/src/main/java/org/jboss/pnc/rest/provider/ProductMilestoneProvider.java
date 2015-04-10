package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.ProductMilestonePredicates.withProductVersionId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class ProductMilestoneProvider {

    private ProductMilestoneRepository productMilestoneRepository;

    @Inject
    public ProductMilestoneProvider(ProductMilestoneRepository productMilestoneRepository) {
        this.productMilestoneRepository = productMilestoneRepository;
    }

    // needed for EJB/CDI
    public ProductMilestoneProvider() {
    }

    public List<ProductMilestoneRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(ProductMilestone.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        Iterable<ProductMilestone> productMilestones = productMilestoneRepository.findAll(filteringCriteria.get(), paging);
        return nullableStreamOf(productMilestones)
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProductMilestoneRest getSpecific(Integer productMilestoneId) {
        ProductMilestone productMilestone = productMilestoneRepository.findOne(productMilestoneId);
        if (productMilestone != null) {
            return new ProductMilestoneRest(productMilestone);
        }
        return null;
    }

    public Integer store(ProductMilestoneRest productMilestoneRest) {
        Preconditions.checkArgument(productMilestoneRest.getId() == null, "Id must be null");
        ProductMilestone productMilestone = productMilestoneRepository.save(productMilestoneRest.toProductMilestone());
        return productMilestone.getId();
    }

    public void update(Integer id, ProductMilestoneRest productMilestoneRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productMilestoneRest.getId() == null || productMilestoneRest.getId().equals(id),
                "Entity id does not match the id to update");
        productMilestoneRest.setId(id);
        ProductMilestone productMilestone = productMilestoneRepository.findOne(productMilestoneRest.getId());
        Preconditions.checkArgument(productMilestone != null,
                "Couldn't find Product Milestone with id " + productMilestoneRest.getId());
        productMilestoneRepository.save(productMilestoneRest.toProductMilestone());
    }

    private Function<ProductMilestone, ProductMilestoneRest> toRestModel() {
        return productMilestone -> new ProductMilestoneRest(productMilestone);
    }

}
