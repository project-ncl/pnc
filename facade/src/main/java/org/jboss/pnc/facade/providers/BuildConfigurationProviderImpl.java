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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.common.alignment.ranking.AlignmentPredicate;
import org.jboss.pnc.common.alignment.ranking.AlignmentRanking;
import org.jboss.pnc.common.alignment.ranking.exception.ValidationException;
import org.jboss.pnc.common.alignment.ranking.tokenizer.QualifierToken;
import org.jboss.pnc.common.alignment.ranking.tokenizer.Token;
import org.jboss.pnc.common.json.moduleconfig.AlignmentConfig;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.AlignmentStrategy;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildConfigurationWithLatestBuild;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.notification.BuildConfigurationCreation;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.jboss.pnc.dto.response.BuildConfigCreationResponse;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.BuildConfigurationMapper;
import org.jboss.pnc.mapper.api.BuildConfigurationRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates;
import org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates;
import org.jboss.pnc.spi.datastore.predicates.ProductPredicates;
import org.jboss.pnc.spi.datastore.predicates.ProductVersionPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withDependantConfiguration;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withDependencyConfiguration;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withName;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProductVersionId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withScmRepositoryId;

@PermitAll
@Stateless
public class BuildConfigurationProviderImpl extends
        AbstractUpdatableProvider<Integer, org.jboss.pnc.model.BuildConfiguration, BuildConfiguration, BuildConfigurationRef>
        implements BuildConfigurationProvider {

    private final Logger logger = LoggerFactory.getLogger(BuildConfigurationProviderImpl.class);

    @Inject
    private ProductVersionRepository productVersionRepository;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildCoordinator buildCoordinator;

    @Inject
    private BuildConfigurationRevisionMapper buildConfigurationRevisionMapper;

    @Inject
    private SCMRepositoryMapper scmRepositoryMapper;

    @Inject
    private BuildMapper buildMapper;

    @Inject
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    private BuildEnvironmentRepository buildEnvironmentRepository;

    @Inject
    private SequenceHandlerRepository sequenceHandlerRepository;

    @Inject
    private Notifier notifier;

    @Inject
    private SCMRepositoryProvider scmRepositoryProvider;

    @Inject
    private BuildConfigRevisionHelper buildConfigRevisionHelper;

    @Inject
    private ProjectRepository projectRepository;

    @Inject
    private ProductRepository productRepository;

    @Inject
    private ProductMilestoneRepository productMilestoneRepository;

    @Inject
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private UserService userService;

    @Inject
    private UserMapper userMapper;

    @Inject
    private AlignmentConfig alignmentConfig;

    private static final SCMRepository FAKE_REPOSITORY = SCMRepository.builder().id("-1").build();

    @Inject
    public BuildConfigurationProviderImpl(BuildConfigurationRepository repository, BuildConfigurationMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.BuildConfiguration.class);
    }

    @Override
    public Page<BuildConfiguration> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, isNotArchived());
    }

    @Override
    public BuildConfiguration store(BuildConfiguration restEntity) throws DTOValidationException {
        validateBeforeSaving(restEntity);
        Long id = sequenceHandlerRepository.getNextID(org.jboss.pnc.model.BuildConfiguration.SEQUENCE_NAME);
        org.jboss.pnc.model.User currentUser = userService.currentUser();
        User user = userMapper.toDTO(currentUser);
        return super.store(
                restEntity.toBuilder().id(id.toString()).creationUser(user).modificationUser(user).build(),
                false);
    }

    @Override
    public BuildConfiguration getSpecific(String id) {
        org.jboss.pnc.model.BuildConfiguration dbEntity = repository.queryById(Integer.valueOf(id));
        if (dbEntity != null && dbEntity.isArchived()) {
            return null;
        }
        return mapper.toDTO(dbEntity);
    }

    @Override
    protected void preUpdate(org.jboss.pnc.model.BuildConfiguration dbEntity, BuildConfiguration restEntity) {
        if (!BuildConfigRevisionHelper.equalValues(dbEntity, restEntity)) {
            // Changes to audit, set the modificationUser and modificationTime to new values
            org.jboss.pnc.model.User currentUser = userService.currentUser();
            dbEntity.setLastModificationUser(currentUser);
            dbEntity.setLastModificationTime(new Date());
        }

        // NCL-6729: Update the alignment parameters if the build type is changed
        if (restEntity.getBuildType() != dbEntity.getBuildType()) {
            dbEntity.setDefaultAlignmentParams(
                    alignmentConfig.getAlignmentParameters().get(restEntity.getBuildType().toString()));
        }
    }

    @Override
    protected void validateBeforeSaving(BuildConfiguration buildConfigurationRest) {

        super.validateBeforeSaving(buildConfigurationRest);

        validateIfItsNotConflicted(buildConfigurationRest);
        validateEnvironment(buildConfigurationRest);
        validateAlignStrategy(buildConfigurationRest);
    }

    private void validateAlignStrategy(BuildConfiguration newConfiguration) {
        Set<AlignmentStrategy> newStrategies = newConfiguration.getAlignmentStrategies();
        if (newStrategies == null || newStrategies.isEmpty()) {
            return;
        }

        validateRankings(newStrategies);
        validateAllowDenyLists(newStrategies);
    }

    private void validateAlignStrategy(Integer id, BuildConfiguration buildConfigurationRest) {
        Set<AlignmentStrategy> newStrategies = buildConfigurationRest.getAlignmentStrategies();
        if (newStrategies == null || newStrategies.isEmpty()) {
            return;
        }

        Set<AlignmentStrategy> oldStrategies = getSpecific(String.valueOf(id)).getAlignmentStrategies();
        if (oldStrategies == null || oldStrategies.isEmpty()) {
            validateRankings(newStrategies);
            validateAllowDenyLists(newStrategies);
        } else {
            validateRankings(newStrategies, oldStrategies);
            validateAllowDenyLists(newStrategies, oldStrategies);
        }
    }

    private void validateRankings(Set<AlignmentStrategy> strategies) {
        List<List<String>> rankings = strategies.stream().map(AlignmentStrategy::getRanks).collect(Collectors.toList());

        for (List<String> ranks : rankings) {
            AlignmentRanking compiledRanks;
            try {
                compiledRanks = new AlignmentRanking(ranks, null);
            } catch (Exception e) {
                throw new InvalidEntityException(e.getMessage(), "alignmentStrategies", e);
            }

            List<List<Token>> tokenizedRanks = compiledRanks.getRanksAsTokens();

            verifyQualifiersExist(tokenizedRanks);
        }
    }

    private void validateRankings(Set<AlignmentStrategy> newStrats, Set<AlignmentStrategy> oldStrats) {
        try {
            Map<String, List<List<Token>>> newDependencyTokensMap = mapStratToListOfTokensPerRank(newStrats);
            Map<String, List<List<Token>>> oldDependencyTokensMap = mapStratToListOfTokensPerRank(oldStrats);

            Map<String, AlignmentStrategy> mappedByDepOverride = newStrats.stream()
                    .collect(toMap(AlignmentStrategy::getDependencyOverride, Function.identity()));

            Set<AlignmentStrategy> toValidate = newDependencyTokensMap.entrySet()
                    .stream()
                    .filter(
                            entry -> !oldDependencyTokensMap.containsKey(entry.getKey()) // validate new entries
                                    // validate old entries that changed
                                    || !oldDependencyTokensMap.get(entry.getKey()).equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .map(mappedByDepOverride::get)
                    .collect(Collectors.toSet());

            validateRankings(toValidate);
        } catch (ValidationException e) {
            throw new InvalidEntityException(e.getMessage(), "alignmentStrategies", e);
        }

    }

    private void validateAllowDenyLists(Set<AlignmentStrategy> newStrats) {
        validateAList(newStrats, AlignmentStrategy::getDenyList);
        validateAList(newStrats, AlignmentStrategy::getAllowList);
    }

    private void validateAllowDenyLists(Set<AlignmentStrategy> newStrats, Set<AlignmentStrategy> oldStrats) {
        try {
            Map<String, AlignmentStrategy> mappedByDepOverride = newStrats.stream()
                    .collect(toMap(AlignmentStrategy::getDependencyOverride, Function.identity()));

            // DENY LIST SECTION
            Map<String, List<Token>> newDenyListTokenMap = mapStratPredicateToListOfTokens(
                    newStrats,
                    AlignmentStrategy::getDenyList);
            Map<String, List<Token>> oldDenyListTokenMap = mapStratPredicateToListOfTokens(
                    oldStrats,
                    AlignmentStrategy::getDenyList);

            // VALIDATE NEW KEYS AND KEYS THAT HAVE CHANGED QUALIFIERS
            Set<AlignmentStrategy> toValidate = newDenyListTokenMap.entrySet()
                    .stream()
                    .filter(
                            entry -> !oldDenyListTokenMap.containsKey(entry.getKey()) // validate new entries
                                    // validate old entries that changed
                                    || !oldDenyListTokenMap.get(entry.getKey()).equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .map(mappedByDepOverride::get)
                    .collect(Collectors.toSet());

            validateAList(toValidate, AlignmentStrategy::getDenyList);

            // ALLOW LIST SECTION
            Map<String, List<Token>> newAllowListTokenMap = mapStratPredicateToListOfTokens(
                    newStrats,
                    AlignmentStrategy::getAllowList);
            Map<String, List<Token>> oldAllowListTokenMap = mapStratPredicateToListOfTokens(
                    oldStrats,
                    AlignmentStrategy::getAllowList);

            // VALIDATE NEW KEYS AND KEYS THAT HAVE CHANGED QUALIFIERS
            toValidate = newAllowListTokenMap.entrySet()
                    .stream()
                    .filter(
                            entry -> !oldAllowListTokenMap.containsKey(entry.getKey()) // validate new entries
                                    // validate old entries that changed
                                    || !oldAllowListTokenMap.get(entry.getKey()).equals(entry.getValue()))
                    .map(Map.Entry::getKey)
                    .map(mappedByDepOverride::get)
                    .collect(Collectors.toSet());

            validateAList(toValidate, AlignmentStrategy::getAllowList);
        } catch (ValidationException e) {
            throw new InvalidEntityException(e.getMessage(), "alignmentStrategies", e);
        }
    }

    private void validateAList(
            Set<AlignmentStrategy> newStrats,
            Function<AlignmentStrategy, String> allowDenylistFunction) {
        try {
            Map<String, List<Token>> newDenies = mapStratPredicateToListOfTokens(newStrats, allowDenylistFunction);

            newDenies.values()
                    .stream()
                    .flatMap(Collection::stream)
                    .filter(token -> token instanceof QualifierToken)
                    .forEach(token -> verifyQualifier((QualifierToken) token));

        } catch (ValidationException e) {
            throw new InvalidEntityException(e.getMessage(), "alignmentStrategies", e);
        }
    }

    /**
     * AlignStrategy has multiple ranks (List<String>) where each rank (String) consists of multiple Tokens
     * (List<Token>). Therefore, multiple ranks of single strategy map to List<List<Token>>.
     *
     * @param strats Set of AlignmentStrategies
     * @return Map where key is dependencyScope(override) and value is ranks mapped into Tokens
     */
    private static Map<String, List<List<Token>>> mapStratToListOfTokensPerRank(Set<AlignmentStrategy> strats) {
        return strats.stream()
                .collect(
                        toMap(
                                AlignmentStrategy::getDependencyOverride,
                                ac -> new AlignmentRanking(ac.getRanks(), null).getRanksAsTokens()));
    }

    private static Map<String, List<Token>> mapStratPredicateToListOfTokens(
            Set<AlignmentStrategy> strats,
            Function<AlignmentStrategy, String> allowDenylistFunction) {
        return strats.stream()
                .collect(
                        toMap(
                                AlignmentStrategy::getDependencyOverride,
                                ac -> new AlignmentPredicate(allowDenylistFunction.apply(ac)).getTokens()));
    }

    private void verifyQualifiersExist(List<List<Token>> tokenizedRanks) {
        tokenizedRanks.stream()
                .flatMap(Collection::stream)
                .distinct()
                .filter(token -> token instanceof QualifierToken)
                .forEach(token -> verifyQualifier((QualifierToken) token));
    }

    private void verifyQualifier(QualifierToken token) {
        String[] values = token.parts;
        switch (token.qualifier) {
            case PRODUCT_ID:
                validateById(
                        parseQualifierId(values[0], Integer::valueOf, Product.class),
                        productRepository,
                        Product.class);
                break;
            case PRODUCT:
                Product product = productRepository.queryByPredicates(ProductPredicates.withAbbrev(values[0]));
                if (product == null) {
                    throw new InvalidEntityException("Product with abbreviation " + values[0] + " not found.");
                }
                break;
            case VERSION_ID:
                validateById(
                        parseQualifierId(values[0], Integer::valueOf, ProductVersion.class),
                        productVersionRepository,
                        ProductVersion.class);
                break;
            case VERSION:
                ProductVersion version = productVersionRepository.queryByPredicates(
                        ProductVersionPredicates.withProductAbbreviation(values[0]),
                        ProductVersionPredicates.withVersion(values[1]));
                if (version == null) {
                    throw new InvalidEntityException(
                            "ProductVersion of Product " + values[0] + " and Version " + values[1]
                                    + " does not exist.");
                }
                break;
            case MILESTONE_ID:
                validateById(
                        parseQualifierId(values[0], Integer::valueOf, ProductMilestone.class),
                        productMilestoneRepository,
                        ProductMilestone.class);
                break;
            case MILESTONE:
                ProductMilestone milestone = productMilestoneRepository.queryByPredicates(
                        ProductMilestonePredicates.withProductAbbreviationAndMilestoneVersion(values[0], values[1]));
                if (milestone == null) {
                    throw new InvalidEntityException(
                            "ProductMilestone of Product " + values[0] + " and Version " + values[1]
                                    + " does not exist.");
                }
                break;
            case GROUP_BUILD:
                validateById(
                        parseQualifierId(values[0], Long::valueOf, BuildConfigSetRecord.class),
                        buildConfigSetRecordRepository,
                        BuildConfigSetRecord.class);
                break;
            case DEPENDENCY:
            case BUILD:
                validateById(
                        parseQualifierId(values[0], Base32LongID::new, BuildRecord.class),
                        buildRecordRepository,
                        BuildRecord.class);
                break;
            case BUILD_CONFIG_ID:
                validateById(
                        parseQualifierId(values[0], Integer::valueOf, org.jboss.pnc.model.BuildConfiguration.class),
                        repository,
                        org.jboss.pnc.model.BuildConfiguration.class);
                break;
            case BUILD_CONFIG:
                org.jboss.pnc.model.BuildConfiguration configuration = repository
                        .queryByPredicates(withName(values[0]));
                if (configuration == null) {
                    throw new InvalidEntityException("BuildConfiguration with name " + values[0] + " not found");
                }
                break;
            case GROUP_CONFIG_ID:
                validateById(
                        parseQualifierId(values[0], Integer::valueOf, BuildConfigurationSet.class),
                        buildConfigurationSetRepository,
                        BuildConfigurationSet.class);
                break;
            case GROUP_CONFIG:
                org.jboss.pnc.model.BuildConfigurationSet bcs = buildConfigurationSetRepository
                        .queryByPredicates(BuildConfigurationSetPredicates.withName(values[0]));
                if (bcs == null) {
                    throw new InvalidEntityException("BuildConfiguration with name " + values[0] + " not found");
                }
                break;
            case QUALITY:
                try {
                    ArtifactQuality.valueOf(values[0]);
                } catch (IllegalArgumentException e) {
                    throw new InvalidEntityException("Unknown Quality " + values[0]);
                }
                break;
        }
    }

    private <ID extends Serializable> ID parseQualifierId(
            String value,
            Function<String, ID> parser,
            Class<? extends GenericEntity<ID>> clazz) {
        try {
            return parser.apply(value);
        } catch (Exception e) {
            throw new InvalidEntityException(
                    "Could not parse " + clazz.getCanonicalName() + " ID " + value,
                    "alignmentStrategies",
                    e);
        }
    }

    private <E extends GenericEntity<ID>, ID extends Serializable> void validateById(
            ID parsedId,
            Repository<E, ID> repository,
            Class<E> clazz) {
        E entity = repository.queryById(parsedId);
        if (entity == null) {
            throw new InvalidEntityException(
                    clazz.getSimpleName() + " with ID " + parsedId.toString() + " does not exist.");
        }
    }

    @Override
    protected void validateBeforeUpdating(Integer id, BuildConfiguration buildConfigurationRest) {
        super.validateBeforeUpdating(id, buildConfigurationRest);

        org.jboss.pnc.model.BuildConfiguration dbEntity = findInDB(id);
        if (dbEntity.isArchived()) {
            throw new RepositoryViolationException("The Build Config " + id + " is already deleted.");
        }
        validateIfItsNotConflicted(buildConfigurationRest);
        validateDependencies(id, buildConfigurationRest.getDependencies());
        validateEnvironment(buildConfigurationRest);
        validateAlignStrategy(id, buildConfigurationRest);
    }

    private void validateDependencies(Integer buildConfigId, Map<String, BuildConfigurationRef> dependencies)
            throws InvalidEntityException {

        if (dependencies == null || dependencies.isEmpty()) {
            return;
        }

        org.jboss.pnc.model.BuildConfiguration buildConfig = repository.queryById(buildConfigId);

        for (String id : dependencies.keySet()) {

            Integer dependencyId = Integer.valueOf(id);

            ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                    .validateCondition(
                            !buildConfig.getId().equals(dependencyId),
                            "A build configuration cannot depend on itself");

            org.jboss.pnc.model.BuildConfiguration dependency = repository.queryById(dependencyId);

            ValidationBuilder.validateObject(buildConfig, WhenUpdating.class)
                    .validateCondition(
                            !dependency.getAllDependencies().contains(buildConfig),
                            "Cannot add dependency from : " + buildConfig.getId() + " to: " + dependencyId
                                    + " because it would introduce a cyclic dependency");
        }
    }

    private void validateIfItsNotConflicted(BuildConfiguration buildConfigurationRest)
            throws ConflictedEntryException, InvalidEntityException {

        ValidationBuilder.validateObject(buildConfigurationRest, WhenUpdating.class).validateConflict(() -> {

            org.jboss.pnc.model.BuildConfiguration buildConfigurationFromDB = repository
                    .queryByPredicates(withName(buildConfigurationRest.getName()), isNotArchived());

            // don't validate against myself
            if (buildConfigurationFromDB != null && (buildConfigurationRest.getId() == null
                    || !buildConfigurationFromDB.getId().equals(Integer.valueOf(buildConfigurationRest.getId())))) {

                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        buildConfigurationFromDB.getId(),
                        org.jboss.pnc.model.BuildConfiguration.class,
                        "Build configuration with the same name already exists");
            }
            return null;
        });
    }

    private void validateEnvironment(BuildConfiguration buildConfigurationRest) {
        String envId = buildConfigurationRest.getEnvironment().getId();
        BuildEnvironment env = buildEnvironmentRepository.queryById(Integer.valueOf(envId));
        if (env == null) {
            throw new EmptyEntityException("Build environment " + envId + " does not exist.");
        }
    }

    @Override
    public BuildConfigurationRevision createRevision(String id, BuildConfiguration buildConfiguration) {
        buildConfigRevisionHelper.updateBuildConfiguration(id, buildConfiguration);
        return buildConfigRevisionHelper.findRevision(parseId(id), buildConfiguration);
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId) {
        ValidationBuilder.validateObject(null)
                .validateAgainstRepository(productVersionRepository, Integer.valueOf(productVersionId), true);
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withProductVersionId(Integer.valueOf(productVersionId)),
                isNotArchived());
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForProject(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String projectId) {
        ValidationBuilder.validateObject(null)
                .validateAgainstRepository(projectRepository, Integer.valueOf(projectId), true);
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withProjectId(Integer.valueOf(projectId)),
                isNotArchived());

    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForScmRepository(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String scmRepositoryId) {
        ValidationBuilder.validateObject(null)
                .validateAgainstRepository(repositoryConfigurationRepository, Integer.valueOf(scmRepositoryId), true);
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withScmRepositoryId(Integer.valueOf(scmRepositoryId)),
                isNotArchived());
    }

    @Override
    public Page<BuildConfigurationWithLatestBuild> getBuildConfigurationIncludeLatestBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query) {
        Page<BuildConfiguration> buildConfigs = queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                isNotArchived());
        List<Integer> configIds = buildConfigs.getContent()
                .stream()
                .map(bc -> mapper.getIdMapper().toEntity(bc.getId()))
                .collect(Collectors.toList());
        List<BuildRecord> latestBuilds = buildRecordRepository.getLatestBuildsForBuildConfigs(configIds);
        List<BuildTask> runningBuilds;
        try {
            runningBuilds = buildCoordinator.getSubmittedBuildTasks();
        } catch (RemoteRequestException | MissingDataException e) {
            throw new RuntimeException(e);
        }
        List<BuildConfigurationWithLatestBuild> bcsWithLatest = new ArrayList<>();
        buildConfigs.getContent()
                .forEach(bc -> bcsWithLatest.add(populateBuildConfigWithLatestBuild(bc, latestBuilds, runningBuilds)));
        return new Page<>(
                pageIndex,
                pageSize,
                buildConfigs.getTotalPages(),
                buildConfigs.getTotalHits(),
                bcsWithLatest);
    }

    private BuildConfigurationWithLatestBuild populateBuildConfigWithLatestBuild(
            BuildConfiguration buildConfig,
            List<BuildRecord> latestBuilds,
            List<BuildTask> runningBuilds) {
        Integer bcId = mapper.getIdMapper().toEntity(buildConfig.getId());
        Optional<BuildTask> latestBuildTask = runningBuilds.stream()
                .filter(Objects::nonNull)
                .filter(bt -> bt.getBuildConfigurationAudited().getId().equals(bcId))
                .max(Comparator.comparing(BuildTask::getSubmitTime));
        Optional<BuildRecord> latestBuildRecord = latestBuilds.stream()
                .filter(br -> br.getBuildConfigurationId().equals(bcId))
                .findFirst();
        BuildRef latestBuild = latestBuildTask.map((bt -> (BuildRef) buildMapper.fromBuildTask(bt)))
                .orElse(latestBuildRecord.map(buildMapper::toRef).orElse(null));
        String latestBuildUsername = latestBuildTask.map(bt -> bt.getUser().getUsername())
                .orElse(latestBuildRecord.map(br -> br.getUser().getUsername()).orElse(null));
        return BuildConfigurationWithLatestBuild.builderWithLatestBuild()
                .buildConfig(buildConfig)
                .latestBuild(latestBuild)
                .latestBuildUsername(latestBuildUsername)
                .build();
    }

    @Override
    public BuildConfiguration clone(String buildConfigurationId) {
        ValidationBuilder.validateObject(WhenCreatingNew.class)
                .validateAgainstRepository(repository, Integer.valueOf(buildConfigurationId), true);

        org.jboss.pnc.model.BuildConfiguration buildConfiguration = repository
                .queryById(Integer.valueOf(buildConfigurationId));
        org.jboss.pnc.model.User user = userService.currentUser();

        org.jboss.pnc.model.BuildConfiguration clonedBuildConfiguration = buildConfiguration.clone();
        Long id = sequenceHandlerRepository.getNextID(org.jboss.pnc.model.BuildConfiguration.SEQUENCE_NAME);
        clonedBuildConfiguration.setId(id.intValue());
        clonedBuildConfiguration.setCreationUser(user);
        clonedBuildConfiguration.setLastModificationUser(user);

        clonedBuildConfiguration = repository.save(clonedBuildConfiguration);
        repository.flushAndRefresh(clonedBuildConfiguration);

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
                .validateCondition(
                        !dependency.getAllDependencies().contains(buildConfig),
                        "Cannot add dependency from : " + configId + " to: " + dependencyId
                                + " because it would introduce a cyclic dependency");

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
    public Page<BuildConfiguration> getDependencies(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String configId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withDependantConfiguration(Integer.valueOf(configId)),
                isNotArchived());
    }

    @Override
    public Page<BuildConfiguration> getDependants(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String configId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withDependencyConfiguration(Integer.valueOf(configId)),
                isNotArchived());
    }

    @Override
    public Page<BuildConfigurationRevision> getRevisions(int pageIndex, int pageSize, String id) {

        List<BuildConfigurationAudited> auditedBuildConfigs = buildConfigurationAuditedRepository
                .findAllByIdOrderByRevDesc(Integer.valueOf(id));

        List<BuildConfigurationRevision> toReturn = nullableStreamOf(auditedBuildConfigs)
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

    @Override
    public Page<BuildConfiguration> getBuildConfigurationsForGroup(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String groupConfigId) {
        ValidationBuilder.validateObject(null)
                .validateAgainstRepository(buildConfigurationSetRepository, Integer.valueOf(groupConfigId), true);

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withBuildConfigurationSetId(Integer.valueOf(groupConfigId)),
                isNotArchived());
    }

    @Override
    public BuildConfigCreationResponse createWithScm(BuildConfigWithSCMRequest request) {
        ValidationBuilder.validateObject(request, WhenCreatingNew.class)
                .validateNotEmptyArgument()
                .validateAnnotations();
        BuildConfiguration buildConfiguration = request.getBuildConfig();
        validateBeforeSaving(buildConfiguration.toBuilder().scmRepository(FAKE_REPOSITORY).build());
        Long buildConfigurationId = sequenceHandlerRepository
                .getNextID(org.jboss.pnc.model.BuildConfiguration.SEQUENCE_NAME);
        MDCUtils.addProcessContext(buildConfigurationId.toString());
        BuildConfiguration newBuildConfigurationWithId = buildConfiguration.toBuilder()
                .id(buildConfigurationId.toString())
                .build();
        RepositoryCreationResponse rcResponse = scmRepositoryProvider.createSCMRepository(
                request.getScmUrl(),
                request.getBuildConfig().getScmRevision(),
                request.getPreBuildSyncEnabled(),
                JobNotificationType.BUILD_CONFIG_CREATION,
                Optional.of(newBuildConfigurationWithId));

        BuildConfigCreationResponse response;
        if (rcResponse.getTaskId() == null) {
            // scm is internal, not running a RepositoryCreationTask.
            createBuildConfigurationWithRepository(
                    null,
                    Integer.parseInt(rcResponse.getRepository().getId()),
                    newBuildConfigurationWithId);

            org.jboss.pnc.model.BuildConfiguration buildConfigurationFromDB = repository
                    .queryByPredicates(withName(newBuildConfigurationWithId.getName()), isNotArchived());
            response = new BuildConfigCreationResponse(mapper.toDTO(buildConfigurationFromDB));
        } else {
            response = new BuildConfigCreationResponse(rcResponse.getTaskId().toString());
        }
        MDCUtils.removeProcessContext();
        return response;
    }

    @Override
    public Optional<BuildConfiguration> restoreRevision(String id, int rev) {
        IdRev idRev = new IdRev(Integer.valueOf(id), rev);
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository.queryById(idRev);
        org.jboss.pnc.model.BuildConfiguration originalBC = repository.queryById(Integer.valueOf(id));
        org.jboss.pnc.model.User user = userService.currentUser();

        if (buildConfigurationAudited == null || originalBC == null) {
            return Optional.empty();
        }

        originalBC.setName(buildConfigurationAudited.getName());
        originalBC.setBuildScript(buildConfigurationAudited.getBuildScript());
        originalBC.setRepositoryConfiguration(buildConfigurationAudited.getRepositoryConfiguration());
        originalBC.setScmRevision(buildConfigurationAudited.getScmRevision());
        originalBC.setBuildType(buildConfigurationAudited.getBuildType());
        originalBC.setBuildEnvironment(buildConfigurationAudited.getBuildEnvironment());
        originalBC.setGenericParameters(buildConfigurationAudited.getGenericParameters());
        originalBC.setLastModificationUser(user);

        org.jboss.pnc.model.BuildConfiguration newBc = repository.save(originalBC);

        newBc.getBuildConfigurationSets().forEach(BuildConfigurationSet::getId);
        newBc.getDependencies().forEach(org.jboss.pnc.model.BuildConfiguration::getId);
        repository.flushAndRefresh(newBc);

        return Optional.of(mapper.toDTO(newBc));
    }

    public void createBuildConfigurationWithRepository(
            String taskId,
            int scmRepositoryId,
            BuildConfiguration configuration) {
        RepositoryConfiguration repositoryConfiguration = repositoryConfigurationRepository.queryById(scmRepositoryId);
        final boolean sendMessage = taskId != null;
        if (repositoryConfiguration == null) {
            String errorMessage = "Repository Configuration was not found in database.";
            logger.error(errorMessage);
            if (sendMessage) {
                sendErrorMessage(
                        SCMRepository.builder().id(Integer.toString(scmRepositoryId)).build(),
                        null,
                        errorMessage,
                        taskId);
                return;
            }
            throw new RepositoryViolationException("Repository Configuration was not found in database.");
        }

        org.jboss.pnc.model.BuildConfiguration buildConfiguration = mapper.toEntity(configuration);
        buildConfiguration.setRepositoryConfiguration(repositoryConfiguration);
        org.jboss.pnc.model.BuildConfiguration buildConfigurationSaved = repository.save(buildConfiguration);

        Set<Integer> bcSetIds;
        if (configuration.getGroupConfigs() == null) {
            bcSetIds = Collections.emptySet();
        } else {
            bcSetIds = configuration.getGroupConfigs()
                    .keySet()
                    .stream()
                    .map(Integer::valueOf)
                    .collect(Collectors.toSet());
        }

        SCMRepository scmRepository = scmRepositoryMapper.toDTO(repositoryConfiguration);
        BuildConfiguration buildConfig = mapper.toDTO(buildConfigurationSaved);
        try {
            addBuildConfigurationToSet(buildConfigurationSaved, bcSetIds);
        } catch (Exception e) {
            logger.error(e.getMessage());
            if (sendMessage) {
                sendErrorMessage(scmRepository, buildConfig, e.getMessage(), taskId);
                return;
            }
            throw new RepositoryViolationException("Failed to add BuildConfig to BuildConfigSets.");
        }

        logger.info("Created Build Configuration with Repository: {}.", buildConfig);
        if (sendMessage) {
            BuildConfigurationCreation successMessage = BuildConfigurationCreation
                    .success(scmRepository, buildConfig, taskId);
            notifier.sendMessage(successMessage);
        }
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

    private void sendErrorMessage(
            SCMRepository scmRepository,
            BuildConfigurationRef buildConfig,
            String message,
            String taskId) {
        BuildConfigurationCreation errorMessage = BuildConfigurationCreation
                .error(scmRepository, buildConfig, message, taskId);
        notifier.sendMessage(errorMessage);
    }
}
