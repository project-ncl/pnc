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
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.*;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;

@Stateless
public class BuildConfigurationProvider {

    private BuildConfigurationRepository buildConfigurationRepository;

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private BuildRecordRepository buildRecordRepository;

    private ProductVersionRepository productVersionRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    @Inject
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            BuildRecordRepository buildRecordRepository,
            ProductVersionRepository productVersionRepository, 
            RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
        this.productVersionRepository = productVersionRepository;
    }

    // needed for EJB/CDI
    public BuildConfigurationProvider() {
    }

    public Function<? super BuildConfiguration, ? extends BuildConfigurationRest> toRestModel() {
        return projectConfiguration -> new BuildConfigurationRest(projectConfiguration);
    }

    public List<BuildConfigurationRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<BuildConfiguration> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfiguration.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        return nullableStreamOf(buildConfigurationRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationRest> getAllForProject(Integer pageIndex, Integer pageSize, String sortingRsql, String query,
            Integer projectId) {
        Predicate<BuildConfiguration> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfiguration.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        return nullableStreamOf(buildConfigurationRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate,
                withProjectId(projectId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer productId) {
        Predicate<BuildConfiguration> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfiguration.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        return nullableStreamOf(buildConfigurationRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate,
                withProductId(productId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationRest> getAllForProductAndProductVersion(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer productId, Integer versionId) {
        Predicate<BuildConfiguration> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfiguration.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        return nullableStreamOf(buildConfigurationRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate,
                withProductVersionId(versionId), withProductId(productId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigurationRest getSpecific(Integer id) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(id);
        if (buildConfiguration == null) {
            return null;
        }
        return new BuildConfigurationRest(buildConfiguration);
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
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(id);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationRest.getId());
        buildConfiguration = buildConfigurationRepository.save(buildConfigurationRest.toBuildConfiguration(buildConfiguration));
        return buildConfiguration.getId();
  }

    public Integer clone(Integer buildConfigurationId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(buildConfigurationId);
        Preconditions.checkArgument(buildConfiguration != null, "Couldn't find buildConfiguration with id "
                + buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();

        clonedBuildConfiguration = buildConfigurationRepository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void delete(Integer configurationId) {
        buildConfigurationRepository.delete(configurationId);
    }

    public Response addDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        BuildConfiguration dependency = buildConfigurationRepository.queryById(dependencyId);
        // Check that the dependency isn't pointing back to the same config
        if (configId.equals(dependencyId)) {
            return Response.status(Response.Status.BAD_REQUEST).entity("A build configuration cannot depend on itself").build();
        }
        // Check that the new dependency will not create a cycle
        if (dependency.getAllDependencies().contains(buildConfig)) {
            String errorMessage = "Cannot add dependency from : " + configId + " to: " + dependencyId + " because it would introduce a cyclic dependency";
            return Response.status(Response.Status.BAD_REQUEST).entity(errorMessage).build();
        }
        buildConfig.addDependency(dependency);
        buildConfigurationRepository.save(buildConfig);
        return Response.ok().build();
    }

    public void removeDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        BuildConfiguration dependency = buildConfigurationRepository.queryById(dependencyId);
        buildConfig.removeDependency(dependency);
        buildConfigurationRepository.save(buildConfig);
    }

    public Set<BuildConfigurationRest> getDependencies(Integer configId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        Set<BuildConfiguration> buildConfigurations = buildConfig.getDependencies();
        return nullableStreamOf(buildConfigurations)
                .map(toRestModel())
                .collect(Collectors.toSet());
    }

    /**
     * Get the full list of both direct and indirect dependencies
     * @param configId
     * @return
     */
    public Set<BuildConfigurationRest> getAllDependencies(Integer configId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        Set<BuildConfiguration> buildConfigurations = buildConfig.getAllDependencies();
        return nullableStreamOf(buildConfigurations)
                .map(toRestModel())
                .collect(Collectors.toSet());
    }

    public void addProductVersion(Integer configId, Integer productVersionId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        ProductVersion productVersion = productVersionRepository.queryById(productVersionId);
        buildConfig.addProductVersion(productVersion);
        buildConfigurationRepository.save(buildConfig);
    }

    public void removeProductVersion(Integer configId, Integer productVersionId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        ProductVersion productVersion = productVersionRepository.queryById(productVersionId);
        buildConfig.removeProductVersion(productVersion);
        buildConfigurationRepository.save(buildConfig);
    }

    public List<ProductVersionRest> getProductVersions(Integer configId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
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

    public Response getLatestBuildRecord(Integer configId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(configId);
        if (buildConfiguration == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No build configuration exists with id: " + configId).build();
        }
        List<BuildRecord> buildRecords = buildRecordRepository.findAllByLatestBuildConfigurationOrderByEndTimeDesc(buildConfiguration);
        if (buildRecords.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).entity("No build records found for configuration id: " + configId).build();
        }
        BuildRecordRest latestBuildRecord = new BuildRecordRest(buildRecords.get(0));
        return Response.ok(latestBuildRecord).build();
    }

    public Response getBuildRecords(int pageIndex, int pageSize, String sortingRsql, String query, Integer configurationId) {
        BuildConfiguration buildConfiguration = buildConfigurationRepository.queryById(configurationId);
        if (buildConfiguration == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("No build configuration exists with id: " + configurationId).build();
        }
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        List<BuildRecordRest> buildRecords = nullableStreamOf(buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withBuildConfigurationId(configurationId)))
                .map(buildRecordToRestModel())
                .collect(Collectors.toList());
        return Response.ok(buildRecords).build();
    }

    private Function<ProductVersion, ProductVersionRest> productVersionToRestModel() {
        return productVersion -> new ProductVersionRest(productVersion);
    }

    private Function<BuildRecord, BuildRecordRest> buildRecordToRestModel() {
        return buildRecord -> new BuildRecordRest(buildRecord);
    }

    private Function<BuildConfigurationAudited, BuildConfigurationAuditedRest> buildConfigurationAuditedToRestModel() {
        return BuildConfigurationAudited -> new BuildConfigurationAuditedRest(BuildConfigurationAudited);
    }
}
