package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.ProductVersionPredicates.withProductId;
import static org.jboss.pnc.datastore.ProductVersionPredicates.withProductVersionId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

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
        Iterable<ProductVersion> product = productVersionRepository.findAll(withProductId(productId));
        return nullableStreamOf(product)
                .map(productVersion -> new ProductVersionRest(productVersion))
                .collect(Collectors.toList());
    }

    public ProductVersionRest getSpecific(Integer productId, Integer productVersionId) {
        ProductVersion productVersion = productVersionRepository.findOne(withProductId(productId).and(withProductVersionId(productVersionId)));
        if(productVersion != null) {
            return new ProductVersionRest(productVersion);
        }
        return null;
    }

}
