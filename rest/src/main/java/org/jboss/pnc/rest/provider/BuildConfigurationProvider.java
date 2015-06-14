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
import org.jboss.pnc.rest.restmodel.ProductVersionRest;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.*;

@Stateless
public class BuildConfigurationProvider {

    private BuildConfigurationRepository buildConfigurationRepository;

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private ProductVersionRepository productVersionRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    @Inject
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            ProductVersionRepository productVersionRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
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
        BuildConfiguration projectConfiguration = buildConfigurationRepository.queryByPredicates(withConfigurationId(id));
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

    public void addDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        BuildConfiguration dependency = buildConfigurationRepository.queryById(dependencyId);
        buildConfig.addDependency(dependency);
        buildConfigurationRepository.save(buildConfig);
    }

    public void removeDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        BuildConfiguration dependency = buildConfigurationRepository.queryById(dependencyId);
        buildConfig.removeDependency(dependency);
        buildConfigurationRepository.save(buildConfig);
    }

    public List<BuildConfigurationRest> getDependencies(Integer configId) {
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        Set<BuildConfiguration> buildConfigurations = buildConfig.getDependencies();
        return nullableStreamOf(buildConfigurations)
                .map(toRestModel())
                .collect(Collectors.toList());
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
        return new BuildConfigurationAuditedRest (auditedBuildConfig);
    }

    private Function<ProductVersion, ProductVersionRest> productVersionToRestModel() {
        return productVersion -> new ProductVersionRest(productVersion);
    }

    private Function<BuildConfigurationAudited, BuildConfigurationAuditedRest> buildConfigurationAuditedToRestModel() {
        return BuildConfigurationAudited -> new BuildConfigurationAuditedRest(BuildConfigurationAudited);
    }
}
