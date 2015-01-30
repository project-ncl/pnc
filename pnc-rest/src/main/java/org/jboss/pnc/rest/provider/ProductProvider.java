package org.jboss.pnc.rest.provider;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.rest.restmodel.ProductRest;

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

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting) {

        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return productRepository.findAll().stream().map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(productRepository.findAll(buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public ProductRest getSpecific(Integer id) {
        Product product = productRepository.findOne(id);
        if (product != null) {
            return new ProductRest(product);
        }
        return null;
    }

}
