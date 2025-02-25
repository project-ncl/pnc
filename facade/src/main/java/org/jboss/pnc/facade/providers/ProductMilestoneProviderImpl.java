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

import org.apache.commons.collections.ListUtils;
import org.jboss.pnc.api.constants.Attributes;
import org.jboss.pnc.api.constants.OperationParameters;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.api.enums.ResultStatus;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.graph.UndirectedGraphBuilder;
import org.jboss.pnc.common.graph.VertexNeighbor;
import org.jboss.pnc.common.util.ArtifactCoordinatesUtils;
import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.datastore.repositories.internal.SortInfoConverter;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.dto.response.ParsedArtifact;
import org.jboss.pnc.dto.response.DeliveredArtifactInMilestones;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneDeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.facade.rsql.mapper.MilestoneInfoRSQLMapper;
import org.jboss.pnc.facade.util.GraphDtoBuilder;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.AlreadyRunningException;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.BuildPushOperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductRelease_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.predicates.OperationPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.events.OperationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.pnc.enums.ValidationErrorType.DUPLICATION;
import static org.jboss.pnc.enums.ValidationErrorType.FORMAT;
import static org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates.withProductVersionId;
import static org.jboss.pnc.spi.datastore.predicates.ProductMilestonePredicates.withProductVersionIdAndVersion;

@PermitAll
@Stateless
public class ProductMilestoneProviderImpl extends
        AbstractUpdatableProvider<Integer, org.jboss.pnc.model.ProductMilestone, ProductMilestone, ProductMilestoneRef>
        implements ProductMilestoneProvider {

    private static final Logger log = LoggerFactory.getLogger(ProductMilestoneProviderImpl.class);
    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.milestone");

    private final MilestoneInfoRSQLMapper milestoneInfoRSQLMapper;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    private final ProductMilestoneRepository milestoneRepository;

    private final DeliverableArtifactRepository deliverableArtifactRepository;

    private final BrewPusher brewPusher;

    private final BuildPushOperationRepository buildPushOperationRepository;

    @Inject
    private ArtifactProvider artifactProvider;

    private BuildPushOperationMapper buildPushOperationMapper;

    @Inject
    private EntityManager em;

    @Inject
    private UserService userService;

    @Inject
    public ProductMilestoneProviderImpl(
            ProductMilestoneRepository repository,
            DeliverableArtifactRepository deliverableArtifactRepository,
            ProductMilestoneMapper mapper,
            MilestoneInfoRSQLMapper milestoneInfoRSQLMapper,
            BrewPusher brewPusher,
            BuildPushOperationRepository buildPushOperationRepository,
            BuildPushOperationMapper buildPushOperationMapper) {

        super(repository, mapper, org.jboss.pnc.model.ProductMilestone.class);

        this.milestoneRepository = repository;
        this.deliverableArtifactRepository = deliverableArtifactRepository;
        this.milestoneInfoRSQLMapper = milestoneInfoRSQLMapper;
        this.brewPusher = brewPusher;
        this.buildPushOperationRepository = buildPushOperationRepository;
        this.buildPushOperationMapper = buildPushOperationMapper;
    }

    @Override
    protected void validateBeforeSaving(ProductMilestone restEntity) {
        super.validateBeforeSaving(restEntity);
        validateDoesNotConflict(restEntity);
    }

    @Override
    protected void validateBeforeUpdating(Integer id, ProductMilestone restEntity) {
        super.validateBeforeUpdating(id, restEntity);
        org.jboss.pnc.model.ProductMilestone milestoneInDb = findInDB(id);
        // we can't modify milestone if it's already released
        if (milestoneInDb.getEndDate() != null) {
            log.info("Milestone is already closed: no more modifications allowed");
            throw new RepositoryViolationException("Milestone is already closed! No more modifications allowed");
        }
        validateDoesNotConflict(restEntity);
    }

    private void validateDoesNotConflict(ProductMilestone restEntity)
            throws ConflictedEntryException, InvalidEntityException {
        ValidationBuilder.validateObject(restEntity, WhenUpdating.class).validateConflict(() -> {
            org.jboss.pnc.model.ProductMilestone milestoneFromDB = milestoneRepository.queryByPredicates(
                    withProductVersionIdAndVersion(
                            Integer.valueOf(restEntity.getProductVersion().getId()),
                            restEntity.getVersion()));

            Integer restEntityId = null;

            if (restEntity.getId() != null) {
                restEntityId = Integer.valueOf(restEntity.getId());
            }

            // don't validate against myself
            if (milestoneFromDB != null && !milestoneFromDB.getId().equals(restEntityId)) {
                return new ConflictedEntryValidator.ConflictedEntryValidationError(
                        milestoneFromDB.getId(),
                        org.jboss.pnc.model.ProductMilestone.class,
                        "Product milestone with the same product version and version already exists");
            }
            return null;
        });
    }

    @Override
    public void closeMilestone(String id, boolean skipPush) {
        org.jboss.pnc.model.ProductMilestone milestoneInDb = milestoneRepository.queryById(Integer.valueOf(id));

        if (milestoneInDb.getEndDate() != null) {
            throw new RepositoryViolationException("Milestone is already closed! No more modifications allowed");
        }
        if (milestoneInDb.getPerformedBuilds().isEmpty()) {
            throw new InvalidEntityException("No builds were performed in milestone!");
        }
        if (skipPush) {
            close(milestoneInDb);
            return;
        }
        Set<Base32LongID> buildIds = buildsToPush(milestoneInDb);
        List<org.jboss.pnc.model.BuildPushOperation> runningOperations = runningBuildPushes(buildIds);
        if (!runningOperations.isEmpty()) {
            List<BuildPushOperation> runningOperationsDTO = runningOperations.stream()
                    .map(buildPushOperationMapper::toDTO)
                    .collect(Collectors.toList());
            throw new AlreadyRunningException(
                    "Build push for builds in milestone already in progress.",
                    runningOperationsDTO);
        }
        doCloseMilestone(milestoneInDb, buildIds);
    }

    private static Set<Base32LongID> buildsToPush(org.jboss.pnc.model.ProductMilestone milestoneInDb) {
        return milestoneInDb.getPerformedBuilds()
                .stream()
                .filter(build -> build.getStatus() == BuildStatus.SUCCESS)
                .map(BuildRecord::getId)
                .collect(Collectors.toSet());
    }

    private void close(org.jboss.pnc.model.ProductMilestone milestone) {
        // set milestone end date to now when the release process is successful
        milestone.setEndDate(new Date());
        milestoneRepository.save(milestone);
        removeCurrentFlagFromMilestone(milestone);
    }

    /**
     * [NCL-3112] Mark the milestone provided as not current
     *
     * @param milestone ProductMilestone to not be current anymore
     */
    private void removeCurrentFlagFromMilestone(org.jboss.pnc.model.ProductMilestone milestone) {
        ProductVersion productVersion = milestone.getProductVersion();

        if (productVersion.getCurrentProductMilestone() != null
                && productVersion.getCurrentProductMilestone().getId().equals(milestone.getId())) {

            productVersion.setCurrentProductMilestone(null);
        }
    }

    private void doCloseMilestone(org.jboss.pnc.model.ProductMilestone milestone, Set<Base32LongID> buildIds) {
        String tagPrefix = milestone.getProductVersion().getAttributes().get(Attributes.BREW_TAG_PREFIX);
        if (tagPrefix == null) {
            throw new InvalidEntityException(
                    "Product version for this milestone is missing attribute " + Attributes.BREW_TAG_PREFIX);
        }

        BuildPushParameters buildPushParameters = BuildPushParameters.builder()
                .tagPrefix(tagPrefix)
                .reimport(false)
                .build();

        String milestoneId = mapper.getIdMapper().toDto(milestone.getId());

        for (Base32LongID buildId : buildIds) {
            brewPusher.pushBuild(buildId, buildPushParameters, milestoneId);
        }
    }

    @Override
    public void observeEvent(@ObservesAsync OperationChangedEvent event) {
        if (event.getOperationClass() != org.jboss.pnc.model.BuildPushOperation.class) {
            return;
        }
        if (event.getStatus() != ProgressStatus.FINISHED || event.getPreviousStatus() == ProgressStatus.FINISHED) {
            return;
        }
        org.jboss.pnc.model.BuildPushOperation operation = buildPushOperationRepository.queryById(event.getId());
        String milestoneId = operation.getOperationParameters().get(OperationParameters.BUILD_PUSH_MILESTONE_CLOSE);
        if (milestoneId == null) {
            return;
        }
        log.debug(
                "Observed build push operation status (with linked milestone {}) changed event {}.",
                milestoneId,
                event);
        onBuildPushOperationFinished(milestoneId, operation);
    }

    private void onBuildPushOperationFinished(String milestoneId, org.jboss.pnc.model.BuildPushOperation operation) {
        if (operation.getResult() != OperationResult.SUCCESSFUL) {
            return;
        }
        Integer id = mapper.getIdMapper().toEntity(milestoneId);
        org.jboss.pnc.model.ProductMilestone milestone = repository.queryById(id);
        if (milestone == null) {
            throw new IllegalStateException("Product milestone with id " + id + " not found.");
        }

        Set<Base32LongID> buildIds = buildsToPush(milestone);
        List<org.jboss.pnc.model.BuildPushOperation> runningOperations = runningBuildPushes(buildIds).stream()
                .filter(
                        o -> milestoneId
                                .equals(o.getOperationParameters().get(OperationParameters.BUILD_PUSH_MILESTONE_CLOSE)))
                .collect(Collectors.toList());
        if (!runningOperations.isEmpty()) {
            log.debug(
                    "Wanting to close milestone {}, but waiting for {} other build pushes to finish.",
                    milestoneId,
                    runningOperations.size());
            return;
        }

        Set<Base32LongID> successfullyPushed = buildPushOperationRepository
                .queryWithPredicates(
                        BuildPushPredicates.withBuilds(buildIds),
                        OperationPredicates.withResult(ResultStatus.SUCCESS))
                .stream()
                .map(o -> o.getBuild().getId())
                .collect(Collectors.toSet());

        if (successfullyPushed.containsAll(buildIds)) {
            log.debug("Last build push {} finished, closing milestone.", operation.getId());
            close(milestone);
        }
    }

    public List<org.jboss.pnc.model.BuildPushOperation> runningBuildPushes(Set<Base32LongID> buildIds) {
        return buildPushOperationRepository
                .queryWithPredicates(BuildPushPredicates.withBuilds(buildIds), OperationPredicates.inProgress());
    }

    @Override
    public void cancelMilestoneCloseProcess(String id) throws RepositoryViolationException, EmptyEntityException {
        org.jboss.pnc.model.ProductMilestone milestoneInDb = milestoneRepository.queryById(Integer.valueOf(id));
        // If we want to close a milestone, make sure it's not already released (by checking end date)
        // and there are no release in progress
        if (milestoneInDb.getEndDate() != null) {
            userLog.info("Milestone is already closed.");
            throw new RepositoryViolationException("Milestone is already closed!");
        } else {
            brewPusher.cancelPushOfMilestone(id);
        }
    }

    @Override
    public Page<ProductMilestone> getProductMilestonesForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withProductVersionId(Integer.valueOf(productVersionId)));
    }

    @Override
    public Page<MilestoneInfo> getMilestonesOfArtifact(
            String artifactId,
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String queryRsql) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        Optional<Integer> builtIn = getMilestoneIdByBuildRecord(cb, artifactId);
        List<Integer> dependencyOf = getDependentMilestoneIds(cb, artifactId);

        Set<Integer> milestoneIds = new HashSet<>(dependencyOf);
        builtIn.ifPresent(milestoneIds::add);
        milestoneIds.remove(null); // some builds are not in a milestone and so it gives us null
        if (milestoneIds.isEmpty()) {
            return new Page<>();
        }

        Predicate<org.jboss.pnc.model.ProductMilestone> rsqlPredicate = rsqlPredicateProducer
                .getCriteriaPredicate(milestoneInfoRSQLMapper, queryRsql);
        SortInfo<org.jboss.pnc.model.ProductMilestone> sortInfo = rsqlPredicateProducer
                .getSortInfo(milestoneInfoRSQLMapper, sortingRsql);
        CriteriaQuery<Tuple> query = milestoneInfoQuery(cb, milestoneIds, rsqlPredicate, sortInfo);
        int offset = pageIndex * pageSize;
        List<MilestoneInfo> milestones = em.createQuery(query)
                .setMaxResults(pageSize)
                .setFirstResult(offset)
                .getResultList()
                .stream()
                .map(m -> mapTupleToMilestoneInfo(m, builtIn))
                .collect(Collectors.toList());

        int totalHits = getMatchingArtifactMilestonesCount(milestoneIds, rsqlPredicate);
        return new Page<>(pageIndex, pageSize, totalHits, milestones);
    }

    @Override
    public ValidationResponse validateVersion(String productVersionId, String version) {
        boolean matches = Pattern.matches(Patterns.PRODUCT_MILESTONE_VERSION, version);
        ValidationResponse.Builder builder = ValidationResponse.builder().isValid(matches);
        if (!matches) {
            return builder.errorType(FORMAT)
                    .hints(
                            Collections.singletonList(
                                    "Allowed format consists of 2 or 3 numeric components (separated by a dot) followed by a string "
                                            + "qualifier starting with a character, eg. 3.0.0.GA, 1.0.11.CR2.ER1, 3.0.CR2"))
                    .build();
        }

        org.jboss.pnc.model.ProductMilestone duplicate = milestoneRepository
                .queryByPredicates(withProductVersionIdAndVersion(Integer.parseInt(productVersionId), version));

        if (duplicate != null) {
            return builder.isValid(false)
                    .errorType(DUPLICATION)
                    .hints(Collections.singletonList("Product Milestone version already exists"))
                    .build();
        }

        return builder.isValid(matches).build();
    }

    @Override
    public ProductMilestoneStatistics getStatistics(String id) {
        Integer milestoneId = mapper.getIdMapper().toEntity(id);

        return ProductMilestoneStatistics.builder()
                .artifactsInMilestone(milestoneRepository.countBuiltArtifactsInMilestone(milestoneId))
                .deliveredArtifactsSource(
                        ProductMilestoneDeliveredArtifactsStatistics.builder()
                                .thisMilestone(
                                        deliverableArtifactRepository
                                                .countMilestoneDeliveredArtifactsBuiltInThisMilestone(milestoneId))
                                .otherMilestones(
                                        deliverableArtifactRepository
                                                .countMilestoneDeliveredArtifactsBuiltInOtherMilestones(milestoneId))
                                .otherProducts(
                                        deliverableArtifactRepository
                                                .countMilestoneDeliveredArtifactsBuiltByOtherProducts(milestoneId))
                                .noMilestone(
                                        deliverableArtifactRepository
                                                .countMilestoneDeliveredArtifactsBuiltInNoMilestone(milestoneId))
                                .noBuild(
                                        deliverableArtifactRepository
                                                .countMilestoneDeliveredArtifactsNotBuilt(milestoneId))
                                .build())
                .artifactQuality(deliverableArtifactRepository.getArtifactQualitiesCounts(milestoneId))
                .repositoryType(deliverableArtifactRepository.getRepositoryTypesCounts(milestoneId))
                .build();
    }

    @Override
    public List<DeliveredArtifactInMilestones> getArtifactsDeliveredInMilestonesGroupedByPrefix(
            List<String> milestoneIds) {
        List<Integer> milestoneIntIds = milestoneIds.stream().map(Integer::valueOf).collect(Collectors.toList());

        List<Tuple> artifactsDeliveredInMilestonesTuples = milestoneRepository
                .getArtifactsDeliveredInMilestones(milestoneIntIds);

        return parseDeliveredArtifactsInMilestoneTuples(artifactsDeliveredInMilestonesTuples, milestoneIntIds);
    }

    private List<DeliveredArtifactInMilestones> parseDeliveredArtifactsInMilestoneTuples(
            List<Tuple> tuples,
            List<Integer> milestoneId) {
        Map<String, Map<String, List<ParsedArtifact>>> artifactsDeliveredInMilestonesMap = tuples.stream()
                .collect(
                        Collectors.groupingBy(
                                this::parseArtifactNameFromTuple,
                                Collectors.flatMapping(
                                        tuple -> parseMilestonePresencesFromTuple(tuple, milestoneId),
                                        Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (existing, replacement) -> ListUtils.union(existing, replacement)))));

        final int ARTIFACT_PREFIX_SHARED_IN_MIN_MILESTONES_COUNT = 2;

        return artifactsDeliveredInMilestonesMap.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() >= ARTIFACT_PREFIX_SHARED_IN_MIN_MILESTONES_COUNT)
                .map(
                        entry -> DeliveredArtifactInMilestones.builder()
                                .artifactIdentifierPrefix(entry.getKey())
                                .productMilestoneArtifacts(entry.getValue())
                                .build())
                .collect(Collectors.toList());
    }

    private String parseArtifactNameFromTuple(Tuple tuple) {
        String deployPath = tuple.get(1, String.class);
        RepositoryType repositoryType = tuple.get(2, RepositoryType.class);

        return parseArtifactNameFromDeployPath(deployPath, repositoryType);
    }

    private String parseArtifactNameFromDeployPath(String deployPath, RepositoryType repositoryType) {
        switch (repositoryType) {
            case MAVEN: {
                var mavenCoordinates = ArtifactCoordinatesUtils.parseMavenCoordinates(deployPath);

                if (mavenCoordinates != null) {
                    return mavenCoordinates.getGroupId() + ":" + mavenCoordinates.getArtifactId();
                }
                break;
            }
            case NPM: {
                var npmCoordinates = ArtifactCoordinatesUtils.parseNPMCoordinates(deployPath);

                if (npmCoordinates != null) {
                    return npmCoordinates.getName();
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported repository type: " + repositoryType);
        }

        return "";
    }

    private Stream<Map.Entry<String, List<ParsedArtifact>>> parseMilestonePresencesFromTuple(
            Tuple tuple,
            List<Integer> milestoneIds) {
        String id = tuple.get(0, Integer.class).toString();
        String deployPath = tuple.get(1, String.class);
        RepositoryType repositoryType = tuple.get(2, RepositoryType.class);

        String version = parseArtifactVersionFromDeployPath(deployPath, repositoryType);
        String type = parseArtifactTypeFromDeployPath(deployPath, repositoryType);
        String classifier = parseArtifactClassifierFromDeployPath(deployPath, repositoryType);

        ParsedArtifact parsedArtifact = ParsedArtifact.builder()
                .id(id)
                .artifactVersion(version)
                .type(type)
                .classifier(classifier)
                .build();

        return milestoneIds.stream().filter(milestoneId -> {
            Boolean milestonePresence = tuple.get(3 + milestoneIds.indexOf(milestoneId), Boolean.class);
            return milestonePresence.booleanValue();
        }).map(milestoneId -> Map.entry(milestoneId.toString(), List.of(parsedArtifact)));
    }

    private String parseArtifactVersionFromDeployPath(String deployPath, RepositoryType repositoryType) {
        switch (repositoryType) {
            case MAVEN: {
                var mavenCoordinates = ArtifactCoordinatesUtils.parseMavenCoordinates(deployPath);

                if (mavenCoordinates != null) {
                    return mavenCoordinates.getVersionString();
                }
                break;
            }
            case NPM: {
                var npmCoordinates = ArtifactCoordinatesUtils.parseNPMCoordinates(deployPath);

                if (npmCoordinates != null) {
                    return npmCoordinates.getVersionString();
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported repository type: " + repositoryType);
        }

        return "";
    }

    private String parseArtifactTypeFromDeployPath(String deployPath, RepositoryType repositoryType) {
        switch (repositoryType) {
            case MAVEN: {
                var mavenCoordinates = ArtifactCoordinatesUtils.parseMavenCoordinates(deployPath);

                if (mavenCoordinates != null) {
                    return mavenCoordinates.getType();
                }
                break;
            }
            case NPM:
                return "tgz";
            default:
                throw new IllegalArgumentException("Unsupported repository type: " + repositoryType);

        }

        return "";
    }

    private String parseArtifactClassifierFromDeployPath(String deployPath, RepositoryType repositoryType) {
        switch (repositoryType) {
            case MAVEN: {
                var mavenCoordinates = ArtifactCoordinatesUtils.parseMavenCoordinates(deployPath);

                if (mavenCoordinates != null) {
                    return mavenCoordinates.getClassifier();
                }
                break;
            }
            case NPM:
                return null;
            default:
                throw new IllegalArgumentException("Unsupported repository type: " + repositoryType);

        }

        return null;
    }

    @Override
    public Graph<ProductMilestone> getMilestonesSharingDeliveredArtifactsGraph(String milestoneId, Integer depthLimit) {
        org.jboss.util.graph.Graph<ProductMilestone> milestoneInterconnectionGraph = createMilestoneInterconnectionGraph(
                milestoneId,
                depthLimit);

        return GraphDtoBuilder.from(milestoneInterconnectionGraph, ProductMilestone.class, vertex -> vertex.getData());
    }

    private org.jboss.util.graph.Graph<ProductMilestone> createMilestoneInterconnectionGraph(
            String milestoneId,
            Integer depthLimit) {
        var graph = new org.jboss.util.graph.Graph<ProductMilestone>();
        var graphBuilder = new UndirectedGraphBuilder<ProductMilestone, String>(id -> getSpecific(id), node -> {
            var tuples = milestoneRepository.getMilestonesSharingDeliveredArtifacts(Integer.valueOf(node.getId()));
            return parseVertexNeighborsFromTuples(tuples);
        });

        graphBuilder.buildGraph(graph, milestoneId, depthLimit);
        return graph;
    }

    private List<VertexNeighbor<String>> parseVertexNeighborsFromTuples(List<Tuple> tuples) {
        return tuples.stream()
                .map(
                        tuple -> VertexNeighbor.<String> builder()
                                .neighborId(tuple.get(0, Integer.class).toString())
                                .cost(tuple.get(1, Integer.class))
                                .build())
                .collect(Collectors.toList());
    }

    private int getMatchingArtifactMilestonesCount(
            Set<Integer> milestoneIds,
            Predicate<org.jboss.pnc.model.ProductMilestone> rsqlPredicate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<org.jboss.pnc.model.ProductMilestone> milestone = query.from(org.jboss.pnc.model.ProductMilestone.class);

        query.select(cb.count(milestone.get(ProductMilestone_.id)));
        query.where(
                cb.and(
                        milestone.get(ProductMilestone_.id).in(milestoneIds),
                        rsqlPredicate.apply(milestone, query, cb)));
        return em.createQuery(query).getSingleResult().intValue();
    }

    private CriteriaQuery<Tuple> milestoneInfoQuery(
            CriteriaBuilder cb,
            Set<Integer> milestoneIds,
            Predicate<org.jboss.pnc.model.ProductMilestone> rsqlPredicate,
            SortInfo<org.jboss.pnc.model.ProductMilestone> sortInfo) {
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> milestone = query.from(org.jboss.pnc.model.ProductMilestone.class);
        Join<org.jboss.pnc.model.ProductMilestone, ProductVersion> version = milestone
                .join(ProductMilestone_.productVersion);
        Join<org.jboss.pnc.model.ProductMilestone, ProductRelease> release = milestone
                .join(ProductMilestone_.productRelease, JoinType.LEFT);
        Join<ProductVersion, Product> product = version.join(ProductVersion_.product);
        List<Order> orders = SortInfoConverter.toOrders(sortInfo, milestone, cb);

        query.multiselect(
                product.get(Product_.id),
                product.get(Product_.name),
                version.get(ProductVersion_.id),
                version.get(ProductVersion_.version),
                milestone.get(ProductMilestone_.id),
                milestone.get(ProductMilestone_.version),
                milestone.get(ProductMilestone_.endDate),
                release.get(ProductRelease_.id),
                release.get(ProductRelease_.version),
                release.get(ProductRelease_.releaseDate));
        query.where(
                cb.and(
                        milestone.get(ProductMilestone_.id).in(milestoneIds),
                        rsqlPredicate.apply(milestone, query, cb)));
        query.orderBy(orders);
        return query;
    }

    private MilestoneInfo mapTupleToMilestoneInfo(Tuple tuple, Optional<Integer> buildIn) {
        final Integer milestoneId = (Integer) tuple.get(4);
        return MilestoneInfo.builder()
                .productId(tuple.get(0).toString())
                .productName(tuple.get(1).toString())
                .productVersionId(tuple.get(2).toString())
                .productVersionVersion(tuple.get(3).toString())
                .milestoneId(milestoneId.toString())
                .milestoneVersion(tuple.get(5).toString())
                .milestoneEndDate(toInstant(tuple.get(6)))
                .releaseId(tuple.get(7) == null ? null : tuple.get(7).toString())
                .releaseVersion(tuple.get(8) == null ? null : tuple.get(8).toString())
                .releaseReleaseDate(toInstant(tuple.get(9)))
                .built(buildIn.map(milestoneId::equals).orElse(false))
                .build();
    }

    private static Instant toInstant(Object object) {
        if (object == null)
            return null;
        return ((Date) object).toInstant();
    }

    private Optional<Integer> getMilestoneIdByBuildRecord(CriteriaBuilder cb, String id) {
        CriteriaQuery<Integer> buildQuery = cb.createQuery(Integer.class);

        Root<Artifact> artifact = buildQuery.from(Artifact.class);
        Join<Artifact, BuildRecord> buildRecords = artifact.join(Artifact_.buildRecord);
        buildQuery.select(buildRecords.get(BuildRecord_.productMilestone).get(ProductMilestone_.id));
        buildQuery.where(cb.equal(artifact.get(Artifact_.id), Integer.valueOf(id)));
        buildQuery.distinct(true);

        Optional<Integer> singleResult;
        try {
            singleResult = Optional.ofNullable(em.createQuery(buildQuery).getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
        return singleResult;
    }

    private List<Integer> getDependentMilestoneIds(CriteriaBuilder cb, String id) {
        CriteriaQuery<Integer> buildQuery = cb.createQuery(Integer.class);

        Root<Artifact> artifact = buildQuery.from(Artifact.class);
        SetJoin<Artifact, BuildRecord> build = artifact.join(Artifact_.dependantBuildRecords);
        buildQuery.where(cb.equal(artifact.get(Artifact_.id), Integer.valueOf(id)));
        buildQuery.select(build.get(BuildRecord_.productMilestone).get(ProductMilestone_.id));
        buildQuery.distinct(true);

        return em.createQuery(buildQuery).getResultList();
    }
}
