package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;

/**
 * Predicates for {@link org.jboss.pnc.model.ProductVersion} entity.
 */
public class ProductVersionPredicates {

    public static Predicate<ProductVersion> withProductVersionId(Integer productVersionId) {
        return (root, query, cb) -> cb.equal(root.get(ProductVersion_.id), productVersionId);
    }

    public static Predicate<ProductVersion> withProductId(Integer productId) {
        return (root, query, cb) -> {
            Join<ProductVersion, Product> product = root.join(ProductVersion_.product);
            return cb.equal(product.get(Product_.id), productId);
        };
    }

}
