package org.jboss.pnc.rest.provider;

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.ProductRepository;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.rest.restmodel.ProductRest;

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

    public List<ProductRest> getAll() {
        return productRepository.findAll().stream().map(product -> new ProductRest(product)).collect(Collectors.toList());
    }

    public ProductRest getSpecific(Integer id) {
        Product product = productRepository.findOne(id);
        if (product != null) {
            return new ProductRest(product);
        }
        return null;
    }
}
