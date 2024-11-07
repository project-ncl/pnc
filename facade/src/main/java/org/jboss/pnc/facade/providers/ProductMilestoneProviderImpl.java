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
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.datastore.repositories.internal.SortInfoConverter;
import org.jboss.pnc.dto.response.ArtifactVersion;
import org.jboss.pnc.dto.response.DeliveredArtifactInMilestones;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneDeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.facade.rsql.mapper.MilestoneInfoRSQLMapper;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.ConflictedEntryValidator;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.ProductMilestoneCloseResultMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductRelease_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private ProductMilestoneReleaseManager releaseManager;
    private final ProductMilestoneCloseResultMapper milestoneReleaseMapper;
    private final MilestoneInfoRSQLMapper milestoneInfoRSQLMapper;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    private final ProductMilestoneRepository milestoneRepository;

    private final DeliverableArtifactRepository deliverableArtifactRepository;

    @Inject
    private EntityManager em;

    @Inject
    private UserService userService;

    @Inject
    public ProductMilestoneProviderImpl(
            ProductMilestoneRepository repository,
            DeliverableArtifactRepository deliverableArtifactRepository,
            ProductMilestoneMapper mapper,
            ProductMilestoneReleaseManager releaseManager,
            ProductMilestoneCloseResultMapper milestoneReleaseMapper,
            MilestoneInfoRSQLMapper milestoneInfoRSQLMapper) {

        super(repository, mapper, org.jboss.pnc.model.ProductMilestone.class);

        this.releaseManager = releaseManager;
        this.milestoneReleaseMapper = milestoneReleaseMapper;
        this.milestoneRepository = repository;
        this.deliverableArtifactRepository = deliverableArtifactRepository;
        this.milestoneInfoRSQLMapper = milestoneInfoRSQLMapper;
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
    public ProductMilestoneCloseResult closeMilestone(String id) {
        try {
            Long milestoneReleaseId = Sequence.nextId();
            MDCUtils.addProcessContext(milestoneReleaseId.toString());
            userLog.info("Processing milestone close request ...");
            ProductMilestoneCloseResult closeResult = doCloseMilestone(id, milestoneReleaseId);
            return closeResult;
        } finally {
            MDCUtils.removeProcessContext();
        }
    }

    private ProductMilestoneCloseResult doCloseMilestone(String id, Long milestoneReleaseId) {
        org.jboss.pnc.model.ProductMilestone milestoneInDb = milestoneRepository.queryById(Integer.valueOf(id));

        if (milestoneInDb.getEndDate() != null) {
            userLog.info("Milestone is already closed: no more modifications allowed");
            throw new RepositoryViolationException("Milestone is already closed! No more modifications allowed");
        }
        if (milestoneInDb.getPerformedBuilds().size() == 0) {
            throw new InvalidEntityException("No builds were performed in milestone!");
        } else {
            Optional<ProductMilestoneRelease> inProgress = releaseManager.getInProgress(milestoneInDb);
            if (inProgress.isPresent()) {
                userLog.warn("Milestone close is already in progress.");
                return milestoneReleaseMapper.toDTO(inProgress.get());
            } else {
                log.debug("Milestone's 'end date' set; no release of the milestone in progress: will start release");

                ProductMilestoneRelease milestoneReleaseDb = releaseManager.startRelease(
                        milestoneInDb,
                        keycloakServiceClient.getAuthToken(),
                        milestoneReleaseId,
                        userService.currentUsername());
                ProductMilestoneCloseResult milestoneCloseResult = milestoneReleaseMapper.toDTO(milestoneReleaseDb);
                return milestoneCloseResult;
            }
        }
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
            if (releaseManager.noReleaseInProgress(milestoneInDb)) {
                userLog.warn(
                        "Milestone's 'end date' set and no release in progress! Cannot run cancel process for given id");
                throw new EmptyEntityException("No running cancel process for given id.");
            } else {
                userLog.info("Cancelling milestone release process ...");
                releaseManager.cancel(milestoneInDb, keycloakServiceClient.getAuthToken());
            }
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
    public Page<DeliveredArtifactInMilestones> getArtifactsDeliveredInMilestonesGroupedByPrefix(
            int pageIndex,
            int pageSize,
            List<String> milestoneIds) {
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);

        List<Integer> milestoneIntIds = milestoneIds.stream().map(Integer::valueOf).collect(Collectors.toList());

        List<Tuple> artifactsDeliveredInMilestonesTuples = milestoneRepository
                .getArtifactsDeliveredInMilestonesGroupedByPrefix(pageInfo, milestoneIntIds);

        List<DeliveredArtifactInMilestones> artifactsDeliveredInMilestones = artifactsDeliveredInMilestonesTuples
                .stream()
                .map(tuple -> {
                    var prefix = tuple.get(0, String.class);

                    Map<String, List<ArtifactVersion>> mMap = milestoneIntIds.stream().map(milestoneId -> {
                        String artifactVersionsWithId = tuple
                                .get(1 + milestoneIntIds.indexOf(milestoneId), String.class);
                        List<ArtifactVersion> artifactVersionsList = Arrays.asList(artifactVersionsWithId.split(","))
                                .stream()
                                .map(artifactVersionWithId -> {
                                    var artifactVersionWithIdSplit = Arrays.asList(artifactVersionWithId.split(":"));
                                    var artifactId = artifactVersionWithIdSplit.get(0);
                                    var artifactVersion = artifactVersionWithIdSplit.get(1);

                                    return ArtifactVersion.builder()
                                            .id(artifactId)
                                            .artifactVersion(artifactVersion)
                                            .build();
                                })
                                .collect(Collectors.toList());

                        return Map.entry(milestoneId.toString(), artifactVersionsList);
                    })
                            .collect(
                                    Collectors.toMap(
                                            entry -> entry.getKey(),
                                            entry -> entry.getValue(),
                                            (existing, replacement) -> ListUtils.union(existing, replacement)));

                    return DeliveredArtifactInMilestones.builder()
                            .artifactIdentifierPrefix(prefix)
                            .productMilestoneArtifacts(mMap)
                            .build();
                })
                .collect(Collectors.toList());

        return new Page<>(
                pageIndex,
                pageSize,
                milestoneRepository.countDeliveredArtifactPrefixesInMilestones(milestoneIntIds),
                artifactsDeliveredInMilestones);
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
