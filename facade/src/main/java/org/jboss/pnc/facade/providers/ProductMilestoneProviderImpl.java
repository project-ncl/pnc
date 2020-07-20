/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.constants.Patterns;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.MilestoneInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
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
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
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
public class ProductMilestoneProviderImpl
        extends AbstractProvider<Integer, org.jboss.pnc.model.ProductMilestone, ProductMilestone, ProductMilestoneRef>
        implements ProductMilestoneProvider {

    private static final Logger log = LoggerFactory.getLogger(ProductMilestoneProviderImpl.class);

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
    public ProductMilestone update(String id, ProductMilestone restEntity) {
        validateBeforeUpdating(id, restEntity);

        org.jboss.pnc.model.ProductMilestone milestoneInDb = repository.queryById(Integer.valueOf(id));
        org.jboss.pnc.model.ProductMilestone milestoneRestDb = mapper.toEntity(restEntity);

        // we can't modify milestone if it's already released
        if (milestoneInDb.getEndDate() != null) {
            log.info("Milestone is already closed: no more modifications allowed");
            throw new RepositoryViolationException("Milestone is already closed! No more modifications allowed");
        }

        log.debug("Updating milestone for id: {}", id);
        milestoneRestDb.setId(Integer.valueOf(id));

        // make sure that user cannot set the 'endDate' via the REST API
        // this should only be set after the release process is successful
        milestoneRestDb.setEndDate(milestoneInDb.getEndDate());
        // Make sure that user cannot change the product release of the milestone. This is set on release creation.
        milestoneRestDb.setProductRelease(milestoneInDb.getProductRelease());

        validateBeforeUpdating(id, mapper.toDTO(milestoneRestDb));

        return mapper.toDTO(repository.save(milestoneRestDb));
    }

    @Override
    protected void validateBeforeSaving(ProductMilestone restEntity) {
        super.validateBeforeSaving(restEntity);
        validateDoesNotConflict(restEntity);
    }

    @Override
    protected void validateBeforeUpdating(String id, ProductMilestone restEntity) {
        super.validateBeforeUpdating(restEntity.getId(), restEntity);
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
            return doCloseMilestone(id, milestoneReleaseId);
        } finally {
            MDCUtils.removeProcessContext();
        }
    }

    private ProductMilestoneCloseResult doCloseMilestone(String id, Long milestoneReleaseId) {
        org.jboss.pnc.model.ProductMilestone milestoneInDb = repository.queryById(Integer.valueOf(id));

        if (milestoneInDb.getEndDate() != null) {
            log.info("Milestone is already closed: no more modifications allowed");
            throw new RepositoryViolationException("Milestone is already closed! No more modifications allowed");
        } else {
            Optional<ProductMilestoneRelease> inProgress = releaseManager.getInProgress(milestoneInDb);
            if (inProgress.isPresent()) {
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

            log.info("Milestone is already closed.");
            throw new RepositoryViolationException("Milestone is already closed!");

        } else {

            if (releaseManager.noReleaseInProgress(milestoneInDb)) {

                log.debug(
                        "Milestone's 'end date' set and no release in progress! Cannot run cancel process for given id");
                throw new EmptyEntityException("No running cancel process for given id.");

            } else {

                releaseManager.cancel(milestoneInDb);
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
    public Page<MilestoneInfo> getMilestonesOfArtifact(String id, int pageIndex, int pageSize) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        Optional<Integer> builtIn = getBuildInMilestone(cb, id);
        List<Integer> dependencyOf = getDependentMilestoneIds(cb, id);

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

    private CriteriaQuery<Tuple> milestoneInfoQuery(CriteriaBuilder cb, Set<Integer> milestoneIds) {
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> milestone = query.from(org.jboss.pnc.model.ProductMilestone.class);
        Root<ProductRelease> release = query.from(ProductRelease.class);
        Path<ProductVersion> version = milestone.get(ProductMilestone_.productVersion);
        Path<Product> product = version.get(ProductVersion_.product);
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
                cb.and(cb.equal(release.get(ProductRelease_.productMilestone), milestone)),
                milestone.get(ProductMilestone_.id).in(milestoneIds));
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
                .releaseId(tuple.get(7).toString())
                .releaseVersion(tuple.get(8).toString())
                .releaseReleaseDate(toInstant(tuple.get(9)))
                .built(buildIn.map(milestoneId::equals).orElse(false))
                .build();
    }

    private static Instant toInstant(Object object) {
        if (object == null)
            return null;
        return ((Date) object).toInstant();
    }

    private Optional<Integer> getBuildInMilestone(CriteriaBuilder cb, String id) {
        CriteriaQuery<Integer> buildQuery = cb.createQuery(Integer.class);

        Root<Artifact> artifact = buildQuery.from(Artifact.class);
        buildQuery.where(cb.equal(artifact.get(Artifact_.id), Integer.valueOf(id)));
        buildQuery.select(
                artifact.get(Artifact_.buildRecord).get(BuildRecord_.productMilestone).get(ProductMilestone_.id));
        buildQuery.distinct(true);

        List<Integer> resultList = em.createQuery(buildQuery).getResultList();

        return resultList.stream().findFirst();
    }

    private List<Integer> getDependentMilestoneIds(CriteriaBuilder cb, String id) {
        CriteriaQuery<Integer> buildQuery = cb.createQuery(Integer.class);

        Root<Artifact> artifact = buildQuery.from(Artifact.class);
        SetJoin<Artifact, BuildRecord> build = artifact.join(Artifact_.dependantBuildRecords);
        buildQuery.where(cb.equal(artifact.get(Artifact_.id), Integer.valueOf(id)));
        buildQuery.select(build.get(BuildRecord_.productMilestone).get(ProductMilestone_.id));
        buildQuery.distinct(true);

        List<Integer> resultList = em.createQuery(buildQuery).getResultList();
        return resultList;
    }
}
