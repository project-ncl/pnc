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

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildConfigurationSet_;
import org.jboss.pnc.model.BuildConfiguration_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.SetJoin;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildConfigurationSet} entity.
 */
public class BuildConfigurationSetPredicates {

    public static Predicate<BuildConfigurationSet> withBuildConfigurationSetId(Integer configurationSetId) {
        return (root, query, cb) -> cb.equal(root.get(BuildConfigurationSet_.id), configurationSetId);
    }

    public static Predicate<BuildConfigurationSet> withProductVersionId(Integer productVersionId) {
        return (root, query, cb) -> {
            Join<BuildConfigurationSet, ProductVersion> productVersionJoin = root
                    .join(BuildConfigurationSet_.productVersion);
            return cb.equal(productVersionJoin.get(ProductVersion_.id), productVersionId);
        };
    }

    public static Predicate<BuildConfigurationSet> isNotArchived() {
        return (root, query, cb) -> cb.isTrue(root.get(BuildConfigurationSet_.active));
    }

    public static Predicate<BuildConfigurationSet> withName(String name) {
        return (root, query, cb) -> cb.equal(root.get(BuildConfigurationSet_.name), name);
    }

    public static Predicate<BuildConfigurationSet> withBuildConfigurationId(Integer buildConfigurationId) {
        return (root, query, cb) -> {
            SetJoin<BuildConfigurationSet, BuildConfiguration> buildConfigsJoin = root
                    .join(BuildConfigurationSet_.buildConfigurations);
            return cb.equal(buildConfigsJoin.get(BuildConfiguration_.id), buildConfigurationId);
        };
    }
}
