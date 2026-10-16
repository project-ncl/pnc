/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.Join;

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductRelease_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.ProductRelease} entity.
 */
public class ProductReleasePredicates {

    public static Predicate<ProductRelease> withProductVersionId(Integer productVersionId) {
        return (root, query, cb) -> {
            Join<ProductMilestone, ProductVersion> productVersion = root.join(ProductRelease_.productMilestone)
                    .join(ProductMilestone_.productVersion);
            return cb.equal(productVersion.get(ProductVersion_.id), productVersionId);
        };
    }

}
