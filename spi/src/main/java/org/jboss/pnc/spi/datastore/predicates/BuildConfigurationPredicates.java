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
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.Project_;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.SetJoin;
import java.util.Set;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildConfiguration} entity.
 */
public class BuildConfigurationPredicates {

    public static Predicate<BuildConfiguration> withProjectId(Integer projectId) {
        return (root, query, cb) -> {
            Join<BuildConfiguration, Project> project = root.join(BuildConfiguration_.project);
            return cb.equal(project.get(Project_.id), projectId);
        };
    }

    public static Predicate<BuildConfiguration> withIds(Set<Integer> ids) {
        if (ids.isEmpty()) {
            return (root, query, cb) -> cb.or();
        }
        return (root, query, cb) -> root.get(BuildConfiguration_.id).in(ids);
    }

    public static Predicate<BuildConfiguration> withDependantConfiguration(Integer parentBuildConfigurationId) {
        return (root, query, cb) -> {
            SetJoin<BuildConfiguration, BuildConfiguration> dependantBuildConfigurationsJoin = root
                    .join(BuildConfiguration_.dependants);
            return cb.equal(dependantBuildConfigurationsJoin.get(BuildConfiguration_.id), parentBuildConfigurationId);
        };
    }

    public static Predicate<BuildConfiguration> withDependencyConfiguration(Integer childBuildConfigurationId) {
        return (root, query, cb) -> {
            SetJoin<BuildConfiguration, BuildConfiguration> dependencyBuildConfigurationsJoin = root
                    .join(BuildConfiguration_.dependencies);
            return cb.equal(dependencyBuildConfigurationsJoin.get(BuildConfiguration_.id), childBuildConfigurationId);
        };
    }

    public static Predicate<BuildConfiguration> withName(String name) {
        return (root, query, cb) -> cb.equal(root.get(BuildConfiguration_.name), name);
    }

    /**
     * Return a predicate which excludes all archived build configurations
     * 
     * @return
     */
    public static Predicate<BuildConfiguration> isNotArchived() {
        return (root, query, cb) -> cb.isTrue(root.get(BuildConfiguration_.active));
    }

    public static Predicate<BuildConfiguration> withProductId(Integer productId) {
        return (root, query, cb) -> {
            Join<BuildConfiguration, ProductVersion> productVersions = root.join(BuildConfiguration_.productVersion);
            Join<ProductVersion, Product> product = productVersions.join(ProductVersion_.product);
            return cb.equal(product.get(Product_.id), productId);
        };
    }

    public static Predicate<BuildConfiguration> withProductVersionId(Integer productVersionId) {
        return (root, query, cb) -> {
            Join<BuildConfiguration, ProductVersion> productVersions = root.join(BuildConfiguration_.productVersion);
            return cb.equal(productVersions.get(ProductVersion_.id), productVersionId);
        };
    }

    public static Predicate<BuildConfiguration> withBuildConfigurationSetId(Integer buildConfigurationSetId) {
        return (root, query, cb) -> {
            SetJoin<BuildConfiguration, BuildConfigurationSet> configurationBuildConfigurationSetSetJoin = root
                    .join(BuildConfiguration_.buildConfigurationSets);
            return cb.equal(
                    configurationBuildConfigurationSetSetJoin.get(BuildConfigurationSet_.id),
                    buildConfigurationSetId);
        };
    }

    public static Predicate<BuildConfiguration> withScmRepositoryId(Integer scmRepositoryId) {
        return (root, query, cb) -> {
            Join<BuildConfiguration, RepositoryConfiguration> repositoryConfigurations = root
                    .join(BuildConfiguration_.repositoryConfiguration);
            return cb.equal(repositoryConfigurations.get(RepositoryConfiguration_.id), scmRepositoryId);
        };
    }
}
