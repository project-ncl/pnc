package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.rest.restmodel.ProductRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class ProductProvider {

    private ProductRepository productRepository;

    @Inject
    public ProductProvider(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // needed for EJB/CDI
    public ProductProvider() {
    }

    public List<ProductRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(Product.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(productRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public ProductRest getSpecific(Integer id) {
        Product product = productRepository.findOne(id);
        if (product != null) {
            return new ProductRest(product);
        }
        return null;
    }

    public Integer store(ProductRest productRest) {
        Preconditions.checkArgument(productRest.getId() == null, "Id must be null");
        Product product = productRepository.save(productRest.toProduct());
        return product.getId();
    }

    public Integer update(Integer id, ProductRest productRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(productRest.getId() == null || productRest.getId().equals(id),
                "Entity id does not match the id to update");
        productRest.setId(id);
        Product product = productRepository.findOne(productRest.getId());
        Preconditions.checkArgument(product != null, "Couldn't find product with id " + productRest.getId());

        product = productRepository.saveAndFlush(productRest.toProduct());
        return product.getId();
    }

    public Function<? super Product, ? extends ProductRest> toRestModel() {
        return product -> new ProductRest(product);
    }

}
