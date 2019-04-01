/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.facade.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withDependantConfiguration;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withName;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProductVersionId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationProviderImpl
        extends AbstractProvider<org.jboss.pnc.model.BuildConfiguration, BuildConfiguration, BuildConfigurationRef> implements BuildConfigurationProvider {

    private final Logger logger = LoggerFactory.getLogger(BuildConfigurationProviderImpl.class);

    private ProductVersionRepository productVersionRepository;
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    public BuildConfigurationProviderImpl(BuildConfigurationRepository repository,
                                          BuildConfigurationMapper mapper,
                                          ProductVersionRepository productVersionRepository,
                                          BuildConfigurationRevisionMapper buildConfigRevisionMapper,
                                          BuildConfigurationAuditedRepository buildConfigurationAuditedRepository) {
        super(repository, mapper, org.jboss.pnc.model.BuildConfiguration.class);

        this.productVersionRepository = productVersionRepository;
        this.buildConfigurationRevisionMapper = buildConfigRevisionMapper;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
    }


    @Override
    protected void validateBeforeSaving(BuildConfiguration buildConfigurationRest) {

        super.validateBeforeSaving(buildConfigurationRest);

        validateIfItsNotConflicted(buildConfigurationRest);
    }

    @Override
    protected void validateBeforeUpdating(Integer id, BuildConfiguration buildConfigurationRest) {

        super.validateBeforeUpdating(id, buildConfigurationRest);

        validateIfItsNotConflicted(buildConfigurationRest);
        validateDependencies(buildConfigurationRest.getId(), buildConfigurationRest.getDependencyIds());
    }

    private void validateDependencies(Integer buildConfigId, Set<Integer> dependenciesIds) throws InvalidEntityException {

        if (dependenciesIds == null || dependenciesIds.isEmpty()) {
            return;
        }

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(buildConfigId);

        for (Integer dependencyId : dependenciesIds) {

            ValidationBuilder.validateObject(buildConfig, WhenUpdating.class).validateCondition(
                    !buildConfig.getId().equals(dependencyId),
                    "A build configuration cannot depend on itself");

            org.jboss.pnc.model.BuildConfiguration dependency = repository.queryById(dependencyId);

            ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                    .validateCondition(!dependency.getAllDependencies().contains(buildConfig),
                                "Cannot add dependency from : " +
                                        buildConfig.getId() + " to: " + dependencyId +
                                        " because it would introduce a cyclic dependency");
        }
    }

    private void validateIfItsNotConflicted(BuildConfiguration buildConfigurationRest)
            throws ConflictedEntryException, InvalidEntityException {

        ValidationBuilder.validateObject(buildConfigurationRest, WhenUpdating.class).validateConflict(() -> {

            org.jboss.pnc.model.BuildConfiguration buildConfigurationFromDB =
                    repository.queryByPredicates(withName(buildConfigurationRest.getName()), isNotArchived());

            // don't validate against myself
            if (buildConfigurationFromDB != null &&
                !buildConfigurationFromDB.getId().equals(buildConfigurationRest.getId())) {

                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        buildConfigurationFromDB.getId(),
                        org.jboss.pnc.model.BuildConfiguration.class,
                        "Build configuration with the same name already exists");
            }
            return null;
        });
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForProductVersion(int pageIndex,
                                                                            int pageSize,
                                                                            String sortingRsql,
                                                                            String query,
                                                                            Integer productVersionId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(productVersionId));
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForProject(int pageIndex,
                                                                     int pageSize,
                                                                     String sortingRsql,
                                                                     String query,
                                                                     Integer projectId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProjectId(projectId));

    }

    @Override
    public BuildConfiguration clone(Integer buildConfigurationId) {

        ValidationBuilder
                .validateObject(WhenCreatingNew.class)
                .validateAgainstRepository(repository, buildConfigurationId, true);

        org.jboss.pnc.model.BuildConfiguration buildConfiguration = repository.queryById(buildConfigurationId);

        org.jboss.pnc.model.BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();
        clonedBuildConfiguration = repository.save(clonedBuildConfiguration);

        logger.debug("Cloned saved BuildConfiguration: {}", clonedBuildConfiguration);

        return mapper.toDTO(clonedBuildConfiguration);
    }

    @Override
    public void addDependency(Integer configId, Integer dependencyId) {

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(configId);
        org.jboss.pnc.model.BuildConfiguration dependency = repository.queryById(dependencyId);

        ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                .validateCondition(buildConfig != null, "No build config exists with id: " + configId)
                .validateCondition(dependency != null, "No dependency build config exists with id: " + dependencyId)
                .validateCondition(!configId.equals(dependencyId), "A build configuration cannot depend on itself")
                .validateCondition(!dependency.getAllDependencies().contains(buildConfig), "Cannot add dependency from : "
                        + configId + " to: " + dependencyId + " because it would introduce a cyclic dependency");

        logger.debug("Didn't throw any validation errors");
        buildConfig.addDependency(dependency);
        repository.save(buildConfig);
    }

    @Override
    public void removeDependency(Integer configId, Integer dependencyId) {

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(configId);
        org.jboss.pnc.model.BuildConfiguration dependency = repository.queryById(dependencyId);

        ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                .validateCondition(buildConfig != null, "No build config exists with id: " + configId)
                .validateCondition(dependency != null, "No dependency build config exists with id: " + dependencyId);

        buildConfig.removeDependency(dependency);
        repository.save(buildConfig);
    }

    @Override
    public Page<BuildConfiguration> getDependencies(int pageIndex,
                                                    int pageSize,
                                                    String sortingRsql,
                                                    String query,
                                                    Integer configId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withDependantConfiguration(configId));
    }

    @Override
    public Page<BuildConfigurationRevision> getRevisions(int pageIndex, int pageSize, Integer id) {

        List<BuildConfigurationAudited> auditedBuildConfigs =
                buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(id);

        List<BuildConfigurationRevision> toReturn =  nullableStreamOf(auditedBuildConfigs)
                .map(buildConfigurationRevisionMapper::toDTO)
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        int totalHits = auditedBuildConfigs.size();
        int totalPages = (totalHits + pageSize - 1) / pageSize;

        return new Page<>(pageIndex, pageSize, totalPages, totalHits, toReturn);
    }

    @Override
    public BuildConfigurationRevision getRevision(Integer id, Integer rev) {

        IdRev idRev = new IdRev(id, rev);

        BuildConfigurationAudited auditedBuildConfig = buildConfigurationAuditedRepository.queryById(idRev);

        return buildConfigurationRevisionMapper.toDTO(auditedBuildConfig);
    }

    @Override
    public Optional<BuildConfigurationRevision> getLatestAuditedMatchingBCRest(BuildConfiguration buildConfigurationRest) {

        return buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(buildConfigurationRest.getId())
                .stream()
                .filter(bca -> equalValues(bca, buildConfigurationRest))
                .findFirst()
                .map(buildConfigurationRevisionMapper::toDTO);
    }

    private boolean equalValues(BuildConfigurationAudited audited, BuildConfiguration rest) {

        return audited.getName().equals(rest.getName()) &&
                Objects.equals(audited.getBuildScript(), rest.getBuildScript()) &&
                equalsId(audited.getRepositoryConfiguration(), rest.getRepository()) &&
                Objects.equals(audited.getScmRevision(), rest.getScmRevision()) &&
                Objects.equals(audited.getDescription(), rest.getDescription()) &&
                equalsId(audited.getProject(), rest.getProject()) &&
                equalsId(audited.getBuildEnvironment(), rest.getEnvironment()) &&
                audited.getGenericParameters().equals(rest.getGenericParameters());
    }

    private boolean equalsId(GenericEntity<Integer> dbEntity, DTOEntity restEntity) {

        if(dbEntity == null || restEntity == null){
            return dbEntity == restEntity;
        }

        return dbEntity.getId().equals(restEntity.getId());
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForGroup(int pageIndex, int pageSize, String sortingRsql, String query, int groupConfigId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationSetId(groupConfigId));
    }
}
