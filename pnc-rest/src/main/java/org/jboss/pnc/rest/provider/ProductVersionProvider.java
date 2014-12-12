package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.provider.StreamHelper.nullableStreamOf;

@Stateless
public class ProductVersionProvider {

    private ProductVersionRepository productVersionRepository;

    @Inject
    public ProductVersionProvider(ProductVersionRepository productVersionRepository) {
        this.productVersionRepository = productVersionRepository;
    }

    //needed for EJB/CDI
    public ProductVersionProvider() {
    }

    public List<ProductVersionRest> getAll(Integer productId) {
        List<ProductVersion> product = productVersionRepository.findByProductId(productId);
        return nullableStreamOf(product)
                .map(productVersion -> new ProductVersionRest(productVersion))
                .collect(Collectors.toList());
    }

    public ProductVersionRest getSpecific(Integer productId, Integer productVersionId) {
        ProductVersion productVersion = productVersionRepository.findByProductIdAndProductVersionId(productId, productVersionId);
        if(productVersion != null) {
            return new ProductVersionRest(productVersion);
        }
        return null;
    }

}
