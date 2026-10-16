/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.Join;

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

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

    public static Predicate<ProductMilestone> withProductVersionIdAndVersion(Integer productVersionId, String version) {
        return (root, query, cb) -> {
            Join<ProductMilestone, ProductVersion> productVersion = root.join(ProductMilestone_.productVersion);
            return cb.and(
                    cb.equal(productVersion.get(ProductVersion_.id), productVersionId),
                    cb.equal(root.get(ProductMilestone_.version), version));
        };
    }
}
