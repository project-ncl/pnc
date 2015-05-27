/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildConfigurationPredicates.*;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationProvider {

    private BuildConfigurationRepository buildConfigurationRepository;
    private ProductVersionRepository productVersionRepository;

    @Inject
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository, 
            ProductVersionRepository productVersionRepository) {
        this.buildConfigurationRepository = buildConfigurationRepository;
    }

    // needed for EJB/CDI
    public BuildConfigurationProvider() {
    }

    public Function<? super BuildConfiguration, ? extends BuildConfigurationRest> toRestModel() {
        return projectConfiguration -> new BuildConfigurationRest(projectConfiguration);
    }

    public List<BuildConfigurationRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildConfigurationRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationRest> getAllForProject(Integer pageIndex, Integer pageSize, String sortingRsql, String query,
            Integer projectId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return mapToListOfBuildConfigurationRest(
                buildConfigurationRepository.findAll(
                        withProjectId(projectId)
                                .and(filteringCriteria.get()), paging));
    }

    public List<BuildConfigurationRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer productId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);
        return mapToListOfBuildConfigurationRest(
                buildConfigurationRepository.findAll(
                        withProductId(productId)
                                .and(filteringCriteria.get()), paging));
    }

    public List<BuildConfigurationRest> getAllForProductAndProductVersion(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer productId, Integer versionId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return mapToListOfBuildConfigurationRest(buildConfigurationRepository.findAll(
                withProductId(productId)
                        .and(withProductVersionId(versionId))
                        .and(filteringCriteria.get()), paging));
    }

    public BuildConfigurationRest getSpecific(Integer id) {
        BuildConfiguration projectConfiguration = buildConfigurationRepository.findOne(withConfigurationId(id));
        return toRestModel().apply(projectConfiguration);
    }

    public Integer store(BuildConfigurationRest buildConfigurationRest) {
        Preconditions.checkArgument(buildConfigurationRest.getId() == null, "Id must be null");
        BuildConfiguration buildConfiguration = buildConfigurationRest.toBuildConfiguration(null);
        buildConfiguration = buildConfigurationRepository.save(buildConfiguration);
        return buildConfiguration.getId();
    }

    public Integer update(Integer id, BuildConfigurationRest buildConfigurationRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(buildConfigurationRest.getId() == null || buildConfigurationRest.getId().equals(id),
                "Entity id does not match the id to update");
        buildConfigurationRest.setId(id);
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(id);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationRest.getId());
        buildConfiguration = buildConfigurationRepository.save(buildConfigurationRest.toBuildConfiguration(buildConfiguration));
        return buildConfiguration.getId();
  }

    public Integer clone(Integer buildConfigurationId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.findOne(buildConfigurationId);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();

        clonedBuildConfiguration = buildConfigurationRepository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        buildConfigurationRepository.delete(configurationId);
    }

    private List<BuildConfigurationRest> mapToListOfBuildConfigurationRest(Iterable<BuildConfiguration> entries) {
        return nullableStreamOf(entries).map(projectConfiguration -> new BuildConfigurationRest(projectConfiguration)).collect(
                Collectors.toList());
    }

    public void addDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.findOne(configId);
        BuildConfiguration dependency = buildConfigurationRepository.findOne(dependencyId);
        buildConfig.addDependency(dependency);
        buildConfigurationRepository.save(buildConfig);
    }

    public void removeDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.findOne(configId);
        BuildConfiguration dependency = buildConfigurationRepository.findOne(dependencyId);
        buildConfig.removeDependency(dependency);
        buildConfigurationRepository.save(buildConfig);
    }

    public void addProductVersion(Integer configId, Integer productVersionId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.findOne(configId);
        ProductVersion productVersion = productVersionRepository.findOne(productVersionId);
        buildConfig.addProductVersion(productVersion);
        buildConfigurationRepository.save(buildConfig);
    }

    public void removeProductVersion(Integer configId, Integer productVersionId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.findOne(configId);
        ProductVersion productVersion = productVersionRepository.findOne(productVersionId);
        buildConfig.removeProductVersion(productVersion);
        buildConfigurationRepository.save(buildConfig);
    }

}
