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

import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.EnumMapUtils;
import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.DeliveredArtifactsStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
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
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
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
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
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

    @Inject
    private UserService userService;

    @Inject
    private EntityManager em;

    @Inject
    public ProductMilestoneProviderImpl(
            ProductMilestoneRepository repository,
            ProductMilestoneMapper mapper,
            ProductMilestoneReleaseManager releaseManager,
            ProductMilestoneCloseResultMapper milestoneReleaseMapper) {

        super(repository, mapper, org.jboss.pnc.model.ProductMilestone.class);

        this.releaseManager = releaseManager;
        this.milestoneReleaseMapper = milestoneReleaseMapper;
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
            org.jboss.pnc.model.ProductMilestone milestoneFromDB = repository.queryByPredicates(
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
        org.jboss.pnc.model.ProductMilestone milestoneInDb = repository.queryById(Integer.valueOf(id));

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

                ProductMilestoneRelease milestoneReleaseDb = releaseManager
                        .startRelease(milestoneInDb, userService.currentUserToken(), milestoneReleaseId);
                ProductMilestoneCloseResult milestoneCloseResult = milestoneReleaseMapper.toDTO(milestoneReleaseDb);
                return milestoneCloseResult;
            }
        }
    }

    @Override
    public void cancelMilestoneCloseProcess(String id) throws RepositoryViolationException, EmptyEntityException {
        org.jboss.pnc.model.ProductMilestone milestoneInDb = repository.queryById(Integer.valueOf(id));
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
                releaseManager.cancel(milestoneInDb, userService.currentUserToken());
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
    public Page<MilestoneInfo> getMilestonesOfArtifact(String artifactId, int pageIndex, int pageSize) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        Optional<Integer> builtIn = getMilestoneIdByBuildRecord(cb, artifactId);
        List<Integer> dependencyOf = getDependentMilestoneIds(cb, artifactId);

        Set<Integer> milestoneIds = new HashSet<>(dependencyOf);
        builtIn.ifPresent(milestoneIds::add);
        milestoneIds.remove(null); // some builds are not in a milestone and so it gives us null
        if (milestoneIds.isEmpty()) {
            return new Page<>();
        }

        CriteriaQuery<Tuple> query = milestoneInfoQuery(cb, milestoneIds);
        int offset = pageIndex * pageSize;
        List<MilestoneInfo> milestones = em.createQuery(query)
                .setMaxResults(pageSize)
                .setFirstResult(offset)
                .getResultList()
                .stream()
                .map(m -> mapTupleToMilestoneInfo(m, builtIn))
                .collect(Collectors.toList());

        return new Page<>(pageIndex, pageSize, milestoneIds.size(), milestones);
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

        org.jboss.pnc.model.ProductMilestone duplicate = repository
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
        CriteriaBuilder cb = em.getCriteriaBuilder();

        return ProductMilestoneStatistics.builder()
                .artifactsInMilestone(getBuiltArtifactsInMilestone(cb, id).size())
                .deliveredArtifactsSource(
                        DeliveredArtifactsStatistics.builder()
                                .thisMilestone(getDeliveredArtifactsBuiltInThisMilestone(cb, id).size())
                                .previousMilestones(getDeliveredArtifactsBuiltInOtherMilestones(cb, id).size())
                                .build())
                .artifactQuality(getArtifactQualities(cb, id))
                .repositoryType(getRepositoryTypes(cb, id))
                .build();
    }

    private CriteriaQuery<Tuple> milestoneInfoQuery(CriteriaBuilder cb, Set<Integer> milestoneIds) {
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> milestone = query.from(org.jboss.pnc.model.ProductMilestone.class);
        Join<org.jboss.pnc.model.ProductMilestone, ProductVersion> version = milestone
                .join(ProductMilestone_.productVersion);
        Join<org.jboss.pnc.model.ProductMilestone, ProductRelease> release = milestone
                .join(ProductMilestone_.productRelease, JoinType.LEFT);
        Join<ProductVersion, Product> product = version.join(ProductVersion_.product);
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
        query.where(milestone.get(ProductMilestone_.id).in(milestoneIds));
        query.orderBy(cb.desc(milestone.get(ProductMilestone_.endDate)), cb.desc(milestone.get(ProductMilestone_.id)));
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

    private List<Artifact> getBuiltArtifactsInMilestone(CriteriaBuilder cb, String id) {
        CriteriaQuery<Artifact> buildQuery = cb.createQuery(Artifact.class);

        Root<Artifact> artifact = buildQuery.from(Artifact.class);
        Join<Artifact, BuildRecord> build = artifact.join(Artifact_.buildRecord);
        Join<BuildRecord, org.jboss.pnc.model.ProductMilestone> productMilestone = build
                .join(BuildRecord_.productMilestone);
        buildQuery.where(cb.equal(productMilestone.get(ProductMilestone_.id), mapper.getIdMapper().toEntity(id)));
        buildQuery.distinct(true);

        return em.createQuery(buildQuery).getResultList();
    }

    private EnumMap<ArtifactQuality, Integer> getArtifactQualities(CriteriaBuilder cb, String id) {
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> productMilestones = query
                .from(org.jboss.pnc.model.ProductMilestone.class);
        SetJoin<org.jboss.pnc.model.ProductMilestone, Artifact> deliveredArtifacts = productMilestones
                .join(ProductMilestone_.deliveredArtifacts);
        query.where(cb.equal(productMilestones.get(ProductMilestone_.id), mapper.getIdMapper().toEntity(id)));

        query.multiselect(
                deliveredArtifacts.get(Artifact_.artifactQuality),
                cb.count(deliveredArtifacts.get(Artifact_.artifactQuality)));
        query.groupBy(deliveredArtifacts.get(Artifact_.artifactQuality));

        List<Tuple> tuples = em.createQuery(query).getResultList();
        return transformListTupleToEnumMap(tuples, ArtifactQuality.class);
    }

    private EnumMap<RepositoryType, Integer> getRepositoryTypes(CriteriaBuilder cb, String id) {
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> productMilestones = query
                .from(org.jboss.pnc.model.ProductMilestone.class);
        SetJoin<org.jboss.pnc.model.ProductMilestone, Artifact> deliveredArtifacts = productMilestones
                .join(ProductMilestone_.deliveredArtifacts);
        Join<Artifact, TargetRepository> targetRepositories = deliveredArtifacts.join(Artifact_.targetRepository);

        query.multiselect(
                targetRepositories.get(TargetRepository_.repositoryType),
                cb.count(targetRepositories.get(TargetRepository_.repositoryType)));
        query.where(cb.equal(productMilestones.get(ProductMilestone_.id), mapper.getIdMapper().toEntity(id)));
        query.groupBy(targetRepositories.get(TargetRepository_.repositoryType));

        List<Tuple> tuples = em.createQuery(query).getResultList();
        return transformListTupleToEnumMap(tuples, RepositoryType.class);
    }

    private List<Artifact> getDeliveredArtifactsBuiltInThisMilestone(CriteriaBuilder cb, String id) {
        CriteriaQuery<Artifact> query = cb.createQuery(Artifact.class);
        Integer productMilestoneId = mapper.getIdMapper().toEntity(id);

        Root<Artifact> artifacts = query.from(Artifact.class);
        SetJoin<Artifact, org.jboss.pnc.model.ProductMilestone> milestones = artifacts
                .join(Artifact_.deliveredInProductMilestones);

        // delivered artifacts, which were *built in this milestone*
        Join<Artifact, BuildRecord> builds = artifacts.join(Artifact_.buildRecord);
        // INNER JOIN guarantees the artifact was built
        query.where(
                cb.equal(builds.get(BuildRecord_.productMilestone).get(ProductMilestone_.id), productMilestoneId),
                cb.equal(milestones.get(ProductMilestone_.id), productMilestoneId));

        return em.createQuery(query).getResultList();
    }

    private List<Artifact> getDeliveredArtifactsBuiltInOtherMilestones(CriteriaBuilder cb, String id) {
        CriteriaQuery<Artifact> query = cb.createQuery(Artifact.class);
        Integer productMilestoneId = mapper.getIdMapper().toEntity(id);

        Root<Artifact> artifacts = query.from(Artifact.class);
        SetJoin<Artifact, org.jboss.pnc.model.ProductMilestone> artifactsMilestone = artifacts
                .join(Artifact_.deliveredInProductMilestones);

        // delivered artifacts, which were *built in other milestones, but are of the same product*
        Join<Artifact, BuildRecord> build = artifacts.join(Artifact_.buildRecord);
        Join<BuildRecord, org.jboss.pnc.model.ProductMilestone> buildsMilestone = build
                .join(BuildRecord_.productMilestone);
        Join<org.jboss.pnc.model.ProductMilestone, ProductVersion> buildsVersion = buildsMilestone
                .join(ProductMilestone_.productVersion);
        Join<ProductVersion, Product> buildsProduct = buildsVersion.join(ProductVersion_.product);
        Integer productId = getProductIdByItsMilestone(cb, productMilestoneId);
        query.where(
                cb.equal(artifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.equal(buildsProduct.get(Product_.id), productId),
                cb.notEqual(buildsMilestone.get(ProductMilestone_.id), productMilestoneId));

        return em.createQuery(query).getResultList();
    }

    private Integer getProductIdByItsMilestone(CriteriaBuilder cb, Integer productMilestoneId) {
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);

        Root<org.jboss.pnc.model.ProductMilestone> milestone = query.from(org.jboss.pnc.model.ProductMilestone.class);
        Join<org.jboss.pnc.model.ProductMilestone, ProductVersion> productVersion = milestone
                .join(ProductMilestone_.productVersion);
        Join<ProductVersion, Product> product = productVersion.join(ProductVersion_.product);
        query.select(product.get(Product_.id));
        query.where(cb.equal(milestone.get(ProductMilestone_.id), productMilestoneId));

        return em.createQuery(query).getResultList().get(0);
    }

    private static <K extends Enum<K>> EnumMap<K, Integer> transformListTupleToEnumMap(
            List<Tuple> tuples,
            Class<K> keyType) {
        EnumMap<K, Integer> enumMap = EnumMapUtils.initEnumMapWithDefaultValue(keyType, () -> 0);

        for (var t : tuples) {
            // Workaround with .intValue() has to be done instead of Integer.class
            enumMap.put(t.get(0, keyType), t.get(1, Long.class).intValue());
        }

        return enumMap;
    }
}
