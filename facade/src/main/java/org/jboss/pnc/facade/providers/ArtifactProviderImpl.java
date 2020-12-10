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

import java.util.Date;
import org.jboss.pnc.common.maven.Gav;
import org.jboss.pnc.coordinator.maintenance.BlacklistAsyncInvoker;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.ArtifactRevisionMapper;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactAudited;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.ArtifactAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withMd5;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha1;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha256;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@PermitAll
@Stateless
public class ArtifactProviderImpl
        extends AbstractUpdatableProvider<Integer, Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>
        implements ArtifactProvider {

    private static Logger logger = LoggerFactory.getLogger(ArtifactProviderImpl.class);

    private static final EnumSet<ArtifactQuality> USER_ALLOWED_ARTIFACT_QUALITIES = EnumSet
            .of(ArtifactQuality.NEW, ArtifactQuality.VERIFIED, ArtifactQuality.TESTED, ArtifactQuality.DEPRECATED);

    private static final EnumSet<ArtifactQuality> ADMIN_ALLOWED_ARTIFACT_QUALITIES = EnumSet.of(
            ArtifactQuality.NEW,
            ArtifactQuality.VERIFIED,
            ArtifactQuality.TESTED,
            ArtifactQuality.DEPRECATED,
            ArtifactQuality.BLACKLISTED,
            ArtifactQuality.DELETED);

    private static final EnumSet<ArtifactQuality> NOT_MODIFIABLE_ARTIFACT_QUALITIES = EnumSet
            .of(ArtifactQuality.DELETED, ArtifactQuality.BLACKLISTED);

    private static final EnumSet<ArtifactQuality> DA_SYNCRONIZED_ARTIFACT_QUALITIES = EnumSet
            .of(ArtifactQuality.DELETED, ArtifactQuality.BLACKLISTED);

    private static final String MVN_BLACKLIST_JSON_PAYLOAD = "{\"groupId\":\"%s\",\"artifactId\":\"%s\",\"version\":\"%s\"}";

    private BuildRecordRepository buildRecordRepository;

    private final TargetRepositoryRepository targetRepositoryRepository;

    private ArtifactRevisionMapper artifactRevisionMapper;

    private ArtifactAuditedRepository artifactAuditedRepository;

    private BlacklistAsyncInvoker blacklistAsyncInvoker;

    private UserService userService;

    private UserMapper userMapper;

    @Inject
    private EntityManager em;

    @Inject
    public ArtifactProviderImpl(
            ArtifactRepository repository,
            ArtifactMapper mapper,
            ArtifactRevisionMapper artifactRevisionMapper,
            BuildRecordRepository buildRecordRepository,
            TargetRepositoryRepository targetRepositoryRepository,
            ArtifactAuditedRepository artifactAuditedRepository,
            BlacklistAsyncInvoker blacklistAsyncInvoker,
            UserService userService,
            UserMapper userMapper) {
        super(repository, mapper, Artifact.class);
        this.buildRecordRepository = buildRecordRepository;
        this.targetRepositoryRepository = targetRepositoryRepository;
        this.artifactRevisionMapper = artifactRevisionMapper;
        this.artifactAuditedRepository = artifactAuditedRepository;
        this.blacklistAsyncInvoker = blacklistAsyncInvoker;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getAll(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Optional<String> sha256,
            Optional<String> md5,
            Optional<String> sha1) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withSha256(sha256),
                withMd5(md5),
                withSha1(sha1));
    }

    @Override
    public Page<ArtifactInfo> getAllFiltered(
            int pageIndex,
            int pageSize,
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = artifactInfoQuery(cb, identifierPattern, qualities, repoType);
        int offset = pageIndex * pageSize;
        List<ArtifactInfo> artifacts = em.createQuery(query)
                .setMaxResults(pageSize)
                .setFirstResult(offset)
                .getResultList()
                .stream()
                .map(this::mapTupleToArtifactInfo)
                .collect(Collectors.toList());

        return new Page<>(pageIndex, pageSize, artifacts.size(), artifacts);
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public org.jboss.pnc.dto.Artifact store(org.jboss.pnc.dto.Artifact restEntity) throws DTOValidationException {
        org.jboss.pnc.model.User currentUser = userService.currentUser();
        User user = userMapper.toDTO(currentUser);
        Instant now = Instant.now();
        return super.store(
                restEntity.toBuilder()
                        .creationUser(user)
                        .modificationUser(user)
                        .creationTime(now)
                        .modificationTime(now)
                        .build());
    }

    @Override
    protected void preUpdate(Artifact dbEntity, org.jboss.pnc.dto.Artifact restEntity) {
        if (!equalAuditedValues(dbEntity, restEntity)) {
            // Changes to audit, set the modificationUser and modificationTime to new values
            org.jboss.pnc.model.User currentUser = userService.currentUser();
            dbEntity.setModificationUser(currentUser);
            dbEntity.setModificationTime(new Date());
        }
    }

    private boolean equalAuditedValues(Artifact persisted, org.jboss.pnc.dto.Artifact toUpdate) {
        return Objects.equals(persisted.getArtifactQuality(), toUpdate.getArtifactQuality())
                && Objects.equals(persisted.getQualityLevelReason(), toUpdate.getQualityLevelReason());
    }

    @Override
    public ArtifactRevision createQualityLevelRevision(String id, String quality, String reason)
            throws DTOValidationException {

        boolean isLoggedInUserSystemUser = userService.hasLoggedInUserRole(SYSTEM_USER);

        ArtifactQuality newQuality = validateProvidedArtifactQuality(quality, isLoggedInUserSystemUser);

        org.jboss.pnc.dto.Artifact artifact = getSpecific(id);
        if (artifact == null) {
            throw new InvalidEntityException("Artifact with id: " + id + " does not exist.");
        }

        validateIfArtifactQualityIsModifiable(artifact, newQuality);

        update(id, artifact.toBuilder().artifactQuality(newQuality).qualityLevelReason(reason).build());

        ArtifactAudited latestRevision = artifactAuditedRepository.findLatestById(Integer.parseInt(id));
        if (latestRevision == null) {
            throw new RepositoryViolationException("Entity should exist in the DB");
        }

        if (DA_SYNCRONIZED_ARTIFACT_QUALITIES.contains(newQuality)) {
            String jsonPayload = createBlacklistJSONPayload(artifact);
            blacklistAsyncInvoker.notifyBlacklistToDA(jsonPayload);
        }

        return artifactRevisionMapper.toDTO(latestRevision);
    }

    private String createBlacklistJSONPayload(org.jboss.pnc.dto.Artifact artifact) {
        switch (artifact.getTargetRepository().getRepositoryType()) {
            case MAVEN: {
                Gav gav = Gav.parse(artifact.getIdentifier());
                return String
                        .format(MVN_BLACKLIST_JSON_PAYLOAD, gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
            }
            default:
                return null;
        }
    }

    @Override
    @DenyAll
    public void delete(String id) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getBuiltArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getDependantArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withDependantBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }

    @Override
    public Page<ArtifactRevision> getRevisions(int pageIndex, int pageSize, String id) {
        List<ArtifactAudited> auditedBuildConfigs = artifactAuditedRepository
                .findAllByIdOrderByRevDesc(Integer.valueOf(id));

        List<ArtifactRevision> toReturn = nullableStreamOf(auditedBuildConfigs).map(artifactRevisionMapper::toDTO)
                .skip(pageIndex * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        int totalHits = auditedBuildConfigs.size();
        int totalPages = (totalHits + pageSize - 1) / pageSize;

        return new Page<>(pageIndex, pageSize, totalPages, totalHits, toReturn);
    }

    @Override
    public ArtifactRevision getRevision(String id, Integer rev) {
        IdRev idRev = new IdRev(Integer.valueOf(id), rev);
        ArtifactAudited auditedArtifact = artifactAuditedRepository.queryById(idRev);

        return artifactRevisionMapper.toDTO(auditedArtifact);
    }

    private CriteriaQuery<Tuple> artifactInfoQuery(
            CriteriaBuilder cb,
            Optional<String> identifierPattern,
            Set<ArtifactQuality> qualities,
            Optional<RepositoryType> repoType) {

        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<Artifact> artifact = query.from(org.jboss.pnc.model.Artifact.class);
        Path<TargetRepository> repository = artifact.get(Artifact_.targetRepository);
        query.multiselect(
                artifact.get(Artifact_.id),
                artifact.get(Artifact_.identifier),
                artifact.get(Artifact_.artifactQuality),
                repository.get(TargetRepository_.REPOSITORY_TYPE));
        Predicate withIdentifierLike = identifierPattern.isPresent()
                ? cb.like(artifact.get(Artifact_.identifier), identifierPattern.get().replace("*", "%"))
                : cb.and();
        Predicate withQualityIn = !qualities.isEmpty() ? artifact.get(Artifact_.ARTIFACT_QUALITY).in(qualities)
                : cb.and();
        Predicate withRepoType = repoType.isPresent() ? cb
                .equal(artifact.join(Artifact_.targetRepository).get(TargetRepository_.REPOSITORY_TYPE), repoType.get())
                : cb.and();
        query.where(cb.and(withIdentifierLike, withQualityIn, withRepoType));
        return query;
    }

    private ArtifactInfo mapTupleToArtifactInfo(Tuple tuple) {
        return ArtifactInfo.builder()
                .id(tuple.get(0).toString())
                .identifier(tuple.get(1).toString())
                .artifactQuality((ArtifactQuality) tuple.get(2))
                .repositoryType((RepositoryType) tuple.get(3))
                .build();
    }

    private ArtifactQuality validateProvidedArtifactQuality(String quality, boolean isLoggedInUserSystemUser) {

        ArtifactQuality newQuality;
        try {
            newQuality = ArtifactQuality.valueOf(quality.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEntityException("Artifact quality: " + quality + " does not exist.");
        }

        // User can specify NEW, TESTED, VERIFIED, DEPRECATED quality levels; admins can also specify DELETED and
        // BLACKLISTED
        EnumSet<ArtifactQuality> allowedQualities = isLoggedInUserSystemUser ? ADMIN_ALLOWED_ARTIFACT_QUALITIES
                : USER_ALLOWED_ARTIFACT_QUALITIES;

        if (!allowedQualities.contains(newQuality)) {
            throw new InvalidEntityException("Artifact quality level can be changed only to " + allowedQualities);
        }

        return newQuality;
    }

    private void validateIfArtifactQualityIsModifiable(
            org.jboss.pnc.dto.Artifact artifact,
            ArtifactQuality newQuality) {

        // If the quality level is not changing (e.g. in a bulk update after a single update), ignore the checks below
        if (artifact.getArtifactQuality().equals(newQuality)) {
            return;
        }

        // Artifacts with DELETED, BLACKLISTED quality levels cannot be changed
        if (NOT_MODIFIABLE_ARTIFACT_QUALITIES.contains(artifact.getArtifactQuality())) {
            throw new ConflictedEntryException(
                    "Artifact " + artifact.getId() + " with quality " + artifact.getArtifactQuality()
                            + " cannot be changed to another quality level.",
                    Artifact.class,
                    artifact.getId().toString());
        }

        // If the artifact is TEMPORARY, quality level can only change to DELETED
        if (ArtifactQuality.TEMPORARY.equals(artifact.getArtifactQuality())
                && !newQuality.equals(ArtifactQuality.DELETED)) {

            throw new ConflictedEntryException(
                    "Temporary artifact " + artifact.getId() + " can only be changed to DELETED quality level.",
                    Artifact.class,
                    artifact.getId().toString());
        }
    }
}
