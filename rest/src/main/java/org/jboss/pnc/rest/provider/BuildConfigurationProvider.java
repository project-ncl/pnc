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

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.validation.ConflictedEntryValidator;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.function.Function;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withDependantConfiguration;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withName;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProductId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProductVersionId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;

@Stateless
public class BuildConfigurationProvider extends AbstractProvider<BuildConfiguration, BuildConfigurationRest> {

    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    private ProductVersionRepository productVersionRepository;

    @Inject
    public BuildConfigurationProvider(BuildConfigurationRepository buildConfigurationRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer,
            ProductVersionRepository productVersionRepository) {
        super(buildConfigurationRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.productVersionRepository = productVersionRepository;
    }

    // needed for EJB/CDI
    public BuildConfigurationProvider() {
    }

    public CollectionInfo<BuildConfigurationRest> getAllNonArchived(Integer pageIndex, Integer pageSize, String sortingRsql,
            String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, isNotArchived());
    }

    public CollectionInfo<BuildConfigurationRest> getAllForProject(Integer pageIndex, Integer pageSize, String sortingRsql,
            String query, Integer projectId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProjectId(projectId), isNotArchived());
    }

    public CollectionInfo<BuildConfigurationRest> getAllForProduct(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer productId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductId(productId), isNotArchived());
    }

    public CollectionInfo<BuildConfigurationRest> getAllForProductAndProductVersion(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer productId, Integer versionId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(versionId), isNotArchived());
    }

    public CollectionInfo<BuildConfigurationRest> getAllForBuildConfigurationSet(int pageIndex, int pageSize,
            String sortingRsql, String query, Integer buildConfigurationSetId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query,
                withBuildConfigurationSetId(buildConfigurationSetId), isNotArchived());
    }

    @Override
    protected void validateBeforeSaving(BuildConfigurationRest buildConfigurationRest) throws ValidationException {
        super.validateBeforeSaving(buildConfigurationRest);
        validateIfItsNotConflicted(buildConfigurationRest);
    }

    @Override
    protected void validateBeforeUpdating(Integer id, BuildConfigurationRest buildConfigurationRest)
            throws ValidationException {
        super.validateBeforeUpdating(id, buildConfigurationRest);
        validateIfItsNotConflicted(buildConfigurationRest);
    }

    private void validateIfItsNotConflicted(BuildConfigurationRest buildConfigurationRest)
            throws ConflictedEntryException, InvalidEntityException {
        ValidationBuilder.validateObject(buildConfigurationRest, WhenUpdating.class).validateConflict(() -> {
            BuildConfiguration buildConfigurationFromDB = repository.queryByPredicates(
                    withProjectId(buildConfigurationRest.getProject().getId()), withName(buildConfigurationRest.getName()));

            // don't validate against myself
            if (buildConfigurationFromDB != null && !buildConfigurationFromDB.getId().equals(buildConfigurationRest.getId())) {
                return new ConflictedEntryValidator.ConflictedEntryValidationError(buildConfigurationFromDB.getId(),
                        BuildConfiguration.class, "Build configuration with the same name already exists");
            }
            return null;
        });
    }

    @Override
    protected Function<? super BuildConfiguration, ? extends BuildConfigurationRest> toRESTModel() {
        return buildConfiguration -> new BuildConfigurationRest(buildConfiguration);
    }

    @Override
    protected Function<? super BuildConfigurationRest, ? extends BuildConfiguration> toDBModel() {
        return buildConfigRest -> {

            BuildConfiguration.Builder builder = buildConfigRest.toDBEntityBuilder();

            if (buildConfigRest.getId() == null) {
                return builder.build();
            }

            BuildConfiguration buildConfigDB = repository.queryById(buildConfigRest.getId());
            // If updating an existing record, need to replace several fields from the rest entity with values from DB
            if (buildConfigDB != null) {
                builder.lastModificationTime(buildConfigDB.getLastModificationTime()); // Handled by JPA @Version
                builder.creationTime(buildConfigDB.getCreationTime()); // Immutable after creation
                builder.dependencies(buildConfigDB.getDependencies()); // Update only via add/remove dependencies
            }

            return builder.build();
        };
    }

    public Integer clone(Integer buildConfigurationId) throws ValidationException {
        ValidationBuilder.validateObject(WhenCreatingNew.class).validateAgainstRepository(repository, buildConfigurationId,
                true);

        BuildConfiguration buildConfiguration = repository.queryById(buildConfigurationId);

        BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();

        clonedBuildConfiguration = repository.save(clonedBuildConfiguration);
        return clonedBuildConfiguration.getId();
    }

    public void addDependency(Integer configId, Integer dependencyId) throws ValidationException {
        BuildConfiguration buildConfig = repository.queryById(configId);
        BuildConfiguration dependency = repository.queryById(dependencyId);

        ValidationBuilder.validateObject(buildConfig, WhenCreatingNew.class)
                .validateCondition(!configId.equals(dependencyId), "A build configuration cannot depend on itself")
                .validateCondition(!dependency.getAllDependencies().contains(buildConfig), "Cannot add dependency from : " + configId + " to: " + dependencyId + " because it would introduce a cyclic dependency");

        buildConfig.addDependency(dependency);
        repository.save(buildConfig);
    }

    public void removeDependency(Integer configId, Integer dependencyId) {
        BuildConfiguration buildConfig = repository.queryById(configId);
        BuildConfiguration dependency = repository.queryById(dependencyId);
        buildConfig.removeDependency(dependency);
        repository.save(buildConfig);
    }

    public CollectionInfo<BuildConfigurationRest> getDependencies(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer configId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withDependantConfiguration(configId));
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

    public CollectionInfo<BuildConfigurationAuditedRest> getRevisions(int pageIndex, int pageSize, Integer id) {
        List<BuildConfigurationAudited> auditedBuildConfigs = buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(id);
        return nullableStreamOf(auditedBuildConfigs).map(buildConfigurationAuditedToRestModel()).skip(pageIndex * pageSize)
                .limit(pageSize).collect(new CollectionInfoCollector<>(pageIndex, pageSize, auditedBuildConfigs.size()));
    }

    public BuildConfigurationAuditedRest getRevision(Integer id, Integer rev) {
        IdRev idRev = new IdRev(id, rev);
        BuildConfigurationAudited auditedBuildConfig = buildConfigurationAuditedRepository.queryById(idRev);
        if (auditedBuildConfig == null) {
            return null;
        }
        return new BuildConfigurationAuditedRest(auditedBuildConfig);
    }

    private Function<BuildConfigurationAudited, BuildConfigurationAuditedRest> buildConfigurationAuditedToRestModel() {
        return BuildConfigurationAudited -> new BuildConfigurationAuditedRest(BuildConfigurationAudited);
    }
}
