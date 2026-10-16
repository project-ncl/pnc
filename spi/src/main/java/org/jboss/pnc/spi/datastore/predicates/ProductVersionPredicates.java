/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.predicates;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.SetJoin;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfiguration_;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Predicates for {@link org.jboss.pnc.model.ProductVersion} entity.
 */
public class ProductVersionPredicates {

    public static Predicate<ProductVersion> withProductId(Integer productId) {
        return (root, query, cb) -> {
            Join<ProductVersion, Product> product = root.join(ProductVersion_.product);
            return cb.equal(product.get(Product_.id), productId);
        };
    }

    // THIS PREDICATE GIVES ALL THE PRODUCTVERSIONS LINKED TO THE BUILDCONFIGURATIONSETS THAT CONTAIN A CERTAIN
    // BUILDCONFIGURATION
    /*
     * public static Predicate<ProductVersion> withBuildConfigurationId(Integer buildConfigurationId) { return (root,
     * query, cb) -> { SetJoin<ProductVersion, BuildConfigurationSet> buildConfigurationSetSetJoin =
     * root.join(ProductVersion_.buildConfigurationSets); SetJoin<BuildConfigurationSet, BuildConfiguration>
     * buildConfigurationJoin = buildConfigurationSetSetJoin.join( BuildConfigurationSet_.buildConfigurations); return
     * cb.equal(buildConfigurationJoin.get(BuildConfiguration_.id), buildConfigurationId); }; }
     */

    /**
     * This predicate returns all the ProductVersions linked to a specified BuildConfiguration
     */
    public static Predicate<ProductVersion> withBuildConfigurationId(Integer buildConfigurationId) {
        return (root, query, cb) -> {
            SetJoin<ProductVersion, BuildConfiguration> buildConfigurationJoin = root
                    .join(ProductVersion_.buildConfigurations);
            return cb.equal(buildConfigurationJoin.get(BuildConfiguration_.id), buildConfigurationId);
        };
    }

}
