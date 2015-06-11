package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;

/**
 * Predicates for {@link org.jboss.pnc.model.ProductMilestone} entity.
 */
public class ProductMilestonePredicates {

    public static Predicate<ProductMilestone> withProductVersionId(Integer productVersionId) {
        return (root, query, cb) -> {
            Join<ProductMilestone, ProductVersion> productVersion = root.join(ProductMilestone_.productVersion);
            return cb.equal(productVersion.get(ProductVersion_.id), productVersionId);
        };
    }
}
