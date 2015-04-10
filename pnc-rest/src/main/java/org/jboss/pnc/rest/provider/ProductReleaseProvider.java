package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.rest.restmodel.ProductReleaseRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.ProductReleasePredicates.withProductVersionId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class ProductReleaseProvider {

    private ProductReleaseRepository productReleaseRepository;

    @Inject
    public ProductReleaseProvider(ProductReleaseRepository productReleaseRepository) {
        this.productReleaseRepository = productReleaseRepository;
    }

    // needed for EJB/CDI
    public ProductReleaseProvider() {
    }

    public List<ProductReleaseRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(ProductRelease.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        Iterable<ProductRelease> productReleases = productReleaseRepository.findAll(filteringCriteria.get(), paging);
        return nullableStreamOf(productReleases)
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProductReleaseRest getSpecific(Integer productReleaseId) {
        ProductRelease productRelease = productReleaseRepository.findOne(productReleaseId);
        if (productRelease != null) {
            return new ProductReleaseRest(productRelease);
        }
        return null;
    }

    public Integer store(ProductReleaseRest productReleaseRest) {
        Preconditions.checkArgument(productReleaseRest.getId() == null, "Id must be null");
        ProductRelease productRelease = productReleaseRepository.save(productReleaseRest.toProductRelease());
        return productRelease.getId();
    }

    public void update(Integer id, ProductReleaseRest productReleaseRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productReleaseRest.getId() == null || productReleaseRest.getId().equals(id),
                "Entity id does not match the id to update");
        productReleaseRest.setId(id);
        ProductRelease productRelease = productReleaseRepository.findOne(productReleaseRest.getId());
        Preconditions.checkArgument(productRelease != null,
                "Couldn't find Product Release with id " + productReleaseRest.getId());
        productReleaseRepository.save(productReleaseRest.toProductRelease());
    }

    private Function<ProductRelease, ProductReleaseRest> toRestModel() {
        return productRelease -> new ProductReleaseRest(productRelease);
    }

}
