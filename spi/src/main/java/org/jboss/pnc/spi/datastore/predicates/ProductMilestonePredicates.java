/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
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

    public static Predicate<ProductMilestone> withProductVersionIdAndVersion(Integer productVersionId, String version) {
        return (root, query, cb) -> {
            Join<ProductMilestone, ProductVersion> productVersion = root.join(ProductMilestone_.productVersion);
            return cb.and(
                    cb.equal(productVersion.get(ProductVersion_.id), productVersionId),
                    cb.equal(root.get(ProductMilestone_.version), version));
        };
    }

    public static Predicate<ProductMilestone> withProductAbbreviationAndMilestoneVersion(
            String abbreviation,
            String version) {
        return (root, query, cb) -> {
            Join<ProductMilestone, ProductVersion> productVersion = root.join(ProductMilestone_.productVersion);
            Join<ProductVersion, Product> product = productVersion.join(ProductVersion_.product);
            return cb.and(
                    cb.equal(product.get(Product_.abbreviation), abbreviation),
                    cb.equal(root.get(ProductMilestone_.version), version));
        };
    }
}
