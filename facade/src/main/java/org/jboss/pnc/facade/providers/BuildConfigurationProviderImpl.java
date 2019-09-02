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
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.notification.BuildConfigurationCreation;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withDependantConfiguration;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withName;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProductVersionId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withScmRepositoryId;

@PermitAll
@Stateless
public class BuildConfigurationProviderImpl
        extends AbstractProvider<org.jboss.pnc.model.BuildConfiguration, BuildConfiguration, BuildConfigurationRef> implements BuildConfigurationProvider {

    private final Logger logger = LoggerFactory.getLogger(BuildConfigurationProviderImpl.class);

    @Inject
    private ProductVersionRepository productVersionRepository;

    @Inject
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;

    @Inject
    private SCMRepositoryMapper scmRepositoryMapper;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private Notifier notifier;

    @Inject
    private SCMRepositoryProvider scmRepositoryProvider;

    @Inject
    private BuildConfigRevisionHelper buildConfigRevisionHelper;

    private static final SCMRepository FAKE_REPOSITORY = SCMRepository.builder().id("-1").build();

    @Inject
    public BuildConfigurationProviderImpl(BuildConfigurationRepository repository,
                                          BuildConfigurationMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.BuildConfiguration.class);
    }


    @Override
    protected void validateBeforeSaving(BuildConfiguration buildConfigurationRest) {

        super.validateBeforeSaving(buildConfigurationRest);

        validateIfItsNotConflicted(buildConfigurationRest);
    }

    @Override
    protected void validateBeforeUpdating(String id, BuildConfiguration buildConfigurationRest) {

        super.validateBeforeUpdating(id, buildConfigurationRest);

        validateIfItsNotConflicted(buildConfigurationRest);
        validateDependencies(id, buildConfigurationRest.getDependencies());
    }

    private void validateDependencies(String buildConfigId, Set<BuildConfigurationRef> dependencies) throws InvalidEntityException {

        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(Integer.valueOf(buildConfigId));

        for (BuildConfigurationRef buildConfigurationRef : dependencies) {

            Integer dependencyId = Integer.valueOf(buildConfigurationRef.getId());

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
                !buildConfigurationFromDB.getId().equals(Integer.valueOf(buildConfigurationRest.getId()))) {

                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        buildConfigurationFromDB.getId(),
                        org.jboss.pnc.model.BuildConfiguration.class,
                        "Build configuration with the same name already exists");
            }
            return null;
        });
    }

    @Override
    public BuildConfigurationRevision createRevision(String id, BuildConfiguration buildConfiguration) {
        super.validateBeforeSaving(buildConfiguration.toBuilder().id(null).build());
        validateIfItsNotConflicted(buildConfiguration.toBuilder().id(id).build());
        validateDependencies(id, buildConfiguration.getDependencies());
        BuildConfigurationAudited latestRevision = buildConfigurationAuditedRepository.findLatestById(Integer.parseInt(id));
        if (latestRevision == null) {
            throw new RepositoryViolationException("Entity should exist in the DB");
        }

        org.jboss.pnc.model.BuildConfiguration bcEntity = mapper.toEntity(buildConfiguration);
        if (equalValues(latestRevision, bcEntity)) {
            return buildConfigurationRevisionMapper.toDTO(latestRevision);
        }
        bcEntity.setCreationTime(latestRevision.getCreationTime());

        buildConfigRevisionHelper.updateBuildConfiguration(bcEntity);
        return buildConfigRevisionHelper.findRevision(id, bcEntity);
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForProductVersion(int pageIndex,
                                                                            int pageSize,
                                                                            String sortingRsql,
                                                                            String query,
                                                                            String productVersionId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(Integer.valueOf(productVersionId)));
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForProject(int pageIndex,
                                                                     int pageSize,
                                                                     String sortingRsql,
                                                                     String query,
                                                                     String projectId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProjectId(Integer.valueOf(projectId)));

    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForScmRepository(int pageIndex,
                                                                           int pageSize,
                                                                           String sortingRsql,
                                                                           String query,
                                                                           String scmRepositoryId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withScmRepositoryId(Integer.valueOf(scmRepositoryId)));
    }

    @Override
    public BuildConfiguration clone(String buildConfigurationId) {

        ValidationBuilder
                .validateObject(WhenCreatingNew.class)
                .validateAgainstRepository(repository, Integer.valueOf(buildConfigurationId), true);

        org.jboss.pnc.model.BuildConfiguration buildConfiguration = repository.queryById(Integer.valueOf(buildConfigurationId));

        org.jboss.pnc.model.BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();
        clonedBuildConfiguration = repository.save(clonedBuildConfiguration);

        logger.debug("Cloned saved BuildConfiguration: {}", clonedBuildConfiguration);

        return mapper.toDTO(clonedBuildConfiguration);
    }

    @Override
    public void addDependency(String configId, String dependencyId) {

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(Integer.valueOf(configId));
        org.jboss.pnc.model.BuildConfiguration dependency = repository.queryById(Integer.valueOf(dependencyId));

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
    public void removeDependency(String configId, String dependencyId) {

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(Integer.valueOf(configId));
        org.jboss.pnc.model.BuildConfiguration dependency = repository.queryById(Integer.valueOf(dependencyId));

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
                                                    String configId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withDependantConfiguration(Integer.valueOf(configId)));
    }

    @Override
    public Page<BuildConfigurationRevision> getRevisions(int pageIndex, int pageSize, String id) {

        List<BuildConfigurationAudited> auditedBuildConfigs =
                buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(Integer.valueOf(id));

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
    public BuildConfigurationRevision getRevision(String id, Integer rev) {

        IdRev idRev = new IdRev(Integer.valueOf(id), rev);

        BuildConfigurationAudited auditedBuildConfig = buildConfigurationAuditedRepository.queryById(idRev);

        return buildConfigurationRevisionMapper.toDTO(auditedBuildConfig);
    }

    private boolean equalValues(BuildConfigurationAudited audited, org.jboss.pnc.model.BuildConfiguration query) {

        return audited.getName().equals(query.getName()) &&
                Objects.equals(audited.getBuildScript(), query.getBuildScript()) &&
                equalsId(audited.getRepositoryConfiguration(), query.getRepositoryConfiguration()) &&
                Objects.equals(audited.getScmRevision(), query.getScmRevision()) &&
                Objects.equals(audited.getDescription(), query.getDescription()) &&
                equalsId(audited.getProject(), query.getProject()) &&
                equalsId(audited.getBuildEnvironment(), query.getBuildEnvironment()) &&
                audited.getGenericParameters().equals(query.getGenericParameters());
    }

    private boolean equalsId(GenericEntity<Integer> dbEntity, GenericEntity<Integer> query) {

        if(dbEntity == null || query == null){
            return dbEntity == query;
        }

        return dbEntity.getId().equals(query.getId());
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForGroup(int pageIndex, int pageSize, String sortingRsql, String query, String groupConfigId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationSetId(Integer.valueOf(groupConfigId)));
    }

    @Override
    public BuildConfigCreationResponse createWithScm(BuildConfigWithSCMRequest request) {
        ValidationBuilder.validateObject(request, WhenCreatingNew.class)
                .validateNotEmptyArgument().validateAnnotations();
        BuildConfiguration buildConfiguration = request.getBuildConfiguration();
        validateBeforeSaving(buildConfiguration.toBuilder().scmRepository(FAKE_REPOSITORY).build());

        RepositoryCreationResponse rcResponse = scmRepositoryProvider.createSCMRepository(
                request.getScmUrl(),
                request.getPreBuildSyncEnabled(),
                JobNotificationType.BUILD_CONFIG_CREATION,
                id -> onRCCreationSuccess(id, buildConfiguration));

        if(rcResponse.getTaskId() == null){
            org.jboss.pnc.model.BuildConfiguration buildConfigurationFromDB =
                    repository.queryByPredicates(withName(buildConfiguration.getName()), isNotArchived());
            return new BuildConfigCreationResponse(mapper.toDTO(buildConfigurationFromDB));
        }else{
            return new BuildConfigCreationResponse(rcResponse.getTaskId());
        }
    }

    @Override
    public Optional<BuildConfiguration> restoreRevision(String id, int rev) {
        IdRev idRev = new IdRev(Integer.valueOf(id), rev);
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(idRev);
        org.jboss.pnc.model.BuildConfiguration originalBC = repository.queryById(Integer.valueOf(id));

        if (buildConfigurationAudited == null || originalBC == null) {
            return Optional.empty();
        }

        originalBC.setName(buildConfigurationAudited.getName());
        originalBC.setBuildScript(buildConfigurationAudited.getBuildScript());
        originalBC.setRepositoryConfiguration(buildConfigurationAudited.getRepositoryConfiguration());
        originalBC.setScmRevision(buildConfigurationAudited.getScmRevision());
        originalBC.setDescription(buildConfigurationAudited.getDescription());
        originalBC.setBuildType(buildConfigurationAudited.getBuildType());
        originalBC.setBuildEnvironment(buildConfigurationAudited.getBuildEnvironment());
        originalBC.setGenericParameters(buildConfigurationAudited.getGenericParameters());

        org.jboss.pnc.model.BuildConfiguration newBc = repository.save(originalBC);

        return Optional.of(mapper.toDTO(newBc));
    }

    private void onRCCreationSuccess(int repositoryConfigurationId, BuildConfiguration configuration) {
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryById(repositoryConfigurationId);
        if (repositoryConfiguration == null) {
            String errorMessage = "Repository Configuration was not found in database.";
            logger.error(errorMessage);
            sendErrorMessage(SCMRepository.builder().id(Integer.toString(repositoryConfigurationId)).build(), null, errorMessage);
            return;
        }

        org.jboss.pnc.model.BuildConfiguration buildConfiguration = mapper.toEntity(configuration);
        buildConfiguration.setRepositoryConfiguration(repositoryConfiguration);
        org.jboss.pnc.model.BuildConfiguration buildConfigurationSaved = repository.save(buildConfiguration);

        Set<Integer> bcSetIds = configuration.getGroupConfigs()
                .stream()
                .map(c -> Integer.valueOf(c.getId()))
                .collect(Collectors.toSet());

        SCMRepository scmRepository = scmRepositoryMapper.toDTO(repositoryConfiguration);
        BuildConfiguration buildConfig = mapper.toDTO(buildConfigurationSaved);
        try {
            if (bcSetIds != null) {
                addBuildConfigurationToSet(buildConfigurationSaved, bcSetIds);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            sendErrorMessage(scmRepository, buildConfig, e.getMessage());
            return;
        }

        BuildConfigurationCreation successMessage
                = BuildConfigurationCreation.success(scmRepository, buildConfig);

        notifier.sendMessage(successMessage);
    }

    private void addBuildConfigurationToSet(org.jboss.pnc.model.BuildConfiguration buildConfig, Set<Integer> bcSetIds) {
        Set<String> notFoundSets = null;
        for (Integer setId : bcSetIds) {
            BuildConfigurationSet bcSet = buildConfigurationSetRepository.queryById(setId);
            if (bcSet == null) {
                if (notFoundSets == null) {
                    notFoundSets = new HashSet<>();
                }
                notFoundSets.add(setId.toString());
            } else {
                if (!bcSet.getBuildConfigurations().contains(buildConfig)) {
                    bcSet.addBuildConfiguration(buildConfig);
                    buildConfigurationSetRepository.save(bcSet);
                }
            }
        }
        if (notFoundSets != null) {
            String ids = String.join(", ", notFoundSets);
            throw new IllegalArgumentException("No group configuration exists for ids: " + ids);
        }
    }

    private void sendErrorMessage(SCMRepository scmRepository, BuildConfigurationRef buildConfig,
            String message) {
        BuildConfigurationCreation errorMessage
                = BuildConfigurationCreation.error(
                        scmRepository,
                        buildConfig,
                        message);
        notifier.sendMessage(errorMessage);
    }
}
