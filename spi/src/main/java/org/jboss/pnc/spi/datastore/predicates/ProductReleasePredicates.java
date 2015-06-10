package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductRelease_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;

/**
 * Predicates for {@link org.jboss.pnc.model.ProductRelease} entity.
 */
public class ProductReleasePredicates {

    public static Predicate<ProductRelease> withProductVersionId(Integer productVersionId) {
        return (root, query, cb) -> {
            Join<ProductRelease, ProductVersion> productVersion = root.join(ProductRelease_.productVersion);
            return cb.equal(productVersion.get(ProductVersion_.id), productVersionId);
        };
    }

}
