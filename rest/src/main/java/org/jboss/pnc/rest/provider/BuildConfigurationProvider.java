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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.spi.datastore.repositories.*;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.*;

@Stateless
public class BuildConfigurationProvider extends AbstractProvider<BuildConfiguration, BuildConfigurationRest> {

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private BuildRecordProvider buildRecordProvider;

    private ProductVersionRepository productVersionRepository;

    @Inject
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer, BuildRecordProvider buildRecordProvider,
            ProductVersionRepository productVersionRepository) {
        super(buildConfigurationRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.productVersionRepository = productVersionRepository;
        this.buildRecordProvider = buildRecordProvider;
    }

    // needed for EJB/CDI
    public BuildConfigurationProvider() {
    }

    public List<BuildConfigurationRest> getAllForProject(Integer pageIndex, Integer pageSize, String sortingRsql, String query,
            Integer projectId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProjectId(projectId));
    }

    public List<BuildConfigurationRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer productId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(productId));
    }

    public List<BuildConfigurationRest> getAllForProductAndProductVersion(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer productId, Integer versionId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(versionId), withProductId(productId));
    }

    public List<BuildConfigurationRest> getAllForBuildConfigurationSet(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer buildConfigurationSetId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationSetId(buildConfigurationSetId));
    }

    @Override
    protected void validateBeforeSaving(BuildConfigurationRest buildConfigurationRest) throws ConflictedEntryException, IllegalArgumentException {
        super.validateBeforeSaving(buildConfigurationRest);
        Preconditions.checkArgument(buildConfigurationRest.getName() != null, "Name must not be null");
        Preconditions.checkArgument(buildConfigurationRest.getProjectId() != null, "Project Id must not be null");

        BuildConfiguration buildConfigurationFromDB = repository
                .queryByPredicates(withProjectId(buildConfigurationRest.getProjectId()),
                        withName(buildConfigurationRest.getName()));

        //don't validate against myself
        if(buildConfigurationFromDB != null && !buildConfigurationFromDB.getId().equals(buildConfigurationRest.getId())) {
            throw new  ConflictedEntryException("Configuration with the same name already exists within project", BuildConfiguration.class, buildConfigurationFromDB.getId());
        }
    }

    @Override
    protected Function<? super BuildConfiguration, ? extends BuildConfigurationRest> toRESTModel() {
        return buildConfiguration -> new BuildConfigurationRest(buildConfiguration);
    }

    @Override
    protected Function<? super BuildConfigurationRest, ? extends BuildConfiguration> toDBModelModel() {
        return buildConfiguration -> {
            BuildConfiguration buildConfigurationFromDB = null;
            if(buildConfiguration.getId() != null) {
                buildConfigurationFromDB = repository.queryById(buildConfiguration.getId());
            }
            return buildConfiguration.toBuildConfiguration(buildConfigurationFromDB);
        };
    }

    public Integer clone(Integer buildConfigurationId) {
        BuildConfiguration buildConfiguration = repository.queryById(buildConfigurationId);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();

        clonedBuildConfiguration = repository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void addDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        BuildConfiguration dependency = repository.queryById(dependencyId);
        // Check that the dependency isn't pointing back to the same config
        if (configId.equals(dependencyId)) {
            throw new IllegalArgumentException("A build configuration cannot depend on itself");
        }
        // Check that the new dependency will not create a cycle
        if (dependency.getAllDependencies().contains(buildConfig)) {
            throw new IllegalArgumentException("Cannot add dependency from : " + configId + " to: " + dependencyId + " because it would introduce a cyclic dependency");
        }
        buildConfig.addDependency(dependency);
        repository.save(buildConfig);
    }

    public void removeDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        BuildConfiguration dependency = repository.queryById(dependencyId);
        buildConfig.removeDependency(dependency);
        repository.save(buildConfig);
    }

    public Set<BuildConfigurationRest> getDependencies(Integer configId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        Set<BuildConfiguration> buildConfigurations = buildConfig.getDependencies();
        return nullableStreamOf(buildConfigurations)
                .map(toRESTModel())
                .collect(Collectors.toSet());
    }

    public Set<BuildConfigurationRest> getAllDependencies(Integer configId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        Set<BuildConfiguration> buildConfigurations = buildConfig.getAllDependencies();
        return nullableStreamOf(buildConfigurations)
                .map(toRESTModel())
                .collect(Collectors.toSet());
    }

    public void addProductVersion(Integer configId, Integer productVersionId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        ProductVersion productVersion = productVersionRepository.queryById(productVersionId);
        buildConfig.addProductVersion(productVersion);
        repository.save(buildConfig);
    }

    public void removeProductVersion(Integer configId, Integer productVersionId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        ProductVersion productVersion = productVersionRepository.queryById(productVersionId);
        buildConfig.removeProductVersion(productVersion);
        repository.save(buildConfig);
    }

    public List<ProductVersionRest> getProductVersions(Integer configId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        Set<ProductVersion> productVersions = buildConfig.getProductVersions();
        return nullableStreamOf(productVersions)
                .map(productVersionToRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationAuditedRest> getRevisions(Integer id) {
        List<BuildConfigurationAudited> auditedBuildConfigs = buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(id);
        return nullableStreamOf(auditedBuildConfigs)
                .map(buildConfigurationAuditedToRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigurationAuditedRest getRevision(Integer id, Integer rev) {
        IdRev idRev = new IdRev(id, rev);
        BuildConfigurationAudited auditedBuildConfig = buildConfigurationAuditedRepository.queryById(idRev);
        if (auditedBuildConfig == null) {
            return null;
        }
        return new BuildConfigurationAuditedRest (auditedBuildConfig);
    }

    public BuildRecordRest getLatestBuildRecord(Integer configId) {
        return buildRecordProvider.getLatestBuildRecord(configId);
    }

    public List<BuildRecordRest> getBuildRecords(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer configurationId) {
        return buildRecordProvider.getAllForBuildConfiguration(pageIndex, pageSize, sortingRsql, query, configurationId);
    }

    private Function<ProductVersion, ProductVersionRest> productVersionToRestModel() {
        return productVersion -> new ProductVersionRest(productVersion);
    }

    private Function<BuildConfigurationAudited, BuildConfigurationAuditedRest> buildConfigurationAuditedToRestModel() {
        return BuildConfigurationAudited -> new BuildConfigurationAuditedRest(BuildConfigurationAudited);
    }
}
