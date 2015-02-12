package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.rest.restmodel.ProductRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class ProductProvider extends BasePaginationProvider<ProductRest, Product> {

    private ProductRepository productRepository;

    @Inject
    public ProductProvider(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // needed for EJB/CDI
    public ProductProvider() {
    }

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super Product, ? extends ProductRest> toRestModel() {
        return product -> new ProductRest(product);
    }

    @Override
    public String getDefaultSortingField() {
        return Product.DEFAULT_SORTING_FIELD;
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting, String rsql) {
        RSQLPredicate rsqlPredicate = RSQLPredicateProducer.fromRSQL(Product.class, rsql);
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return nullableStreamOf(productRepository.findAll(rsqlPredicate.get())).map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(productRepository.findAll(rsqlPredicate.get(), buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public ProductRest getSpecific(Integer id) {
        Product product = productRepository.findOne(id);
        if (product != null) {
            return new ProductRest(product);
        }
        return null;
    }

    public Integer store(ProductRest productRest) {
        Product product = productRepository.save(productRest.toProduct());
        return product.getId();
    }

    public Integer update(ProductRest productRest) {
        Product product = productRepository.findOne(productRest.getId());
        Preconditions.checkArgument(product != null, "Couldn't find product with id " + productRest.getId());

        product = productRepository.saveAndFlush(productRest.toProduct());
        return product.getId();
    }

}
