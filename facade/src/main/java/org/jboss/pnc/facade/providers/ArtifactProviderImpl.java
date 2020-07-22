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

import static org.jboss.pnc.common.util.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withMd5;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha1;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha256;

import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.ArtifactRevision;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.mapper.api.ArtifactRevisionMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactAudited;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.ArtifactAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@PermitAll
@Stateless
public class ArtifactProviderImpl extends AbstractProvider<Integer, Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>
        implements ArtifactProvider {

    private static Logger logger = LoggerFactory.getLogger(ArtifactProviderImpl.class);

    private BuildRecordRepository buildRecordRepository;

    private final TargetRepositoryRepository targetRepositoryRepository;

    private ArtifactRevisionMapper artifactRevisionMapper;

    private ArtifactAuditedRepository artifactAuditedRepository;

    private UserService userService;

    private UserMapper userMapper;

    @Inject
    public ArtifactProviderImpl(
            ArtifactRepository repository,
            ArtifactMapper mapper,
            ArtifactRevisionMapper artifactRevisionMapper,
            BuildRecordRepository buildRecordRepository,
            TargetRepositoryRepository targetRepositoryRepository,
            ArtifactAuditedRepository artifactAuditedRepository,
            UserService userService,
            UserMapper userMapper) {
        super(repository, mapper, Artifact.class);
        this.buildRecordRepository = buildRecordRepository;
        this.targetRepositoryRepository = targetRepositoryRepository;
        this.artifactRevisionMapper = artifactRevisionMapper;
        this.artifactAuditedRepository = artifactAuditedRepository;
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
    @RolesAllowed(SYSTEM_USER)
    public org.jboss.pnc.dto.Artifact update(String id, org.jboss.pnc.dto.Artifact restEntity)
            throws DTOValidationException {
        org.jboss.pnc.model.User currentUser = userService.currentUser();
        User user = userMapper.toDTO(currentUser);
        Instant now = Instant.now();
        return super.update(id, restEntity.toBuilder().modificationUser(user).modificationTime(now).build());
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

        validateIfArtifactQualityIsModifiable(artifact);

        org.jboss.pnc.model.User currentUser = userService.currentUser();
        User user = userMapper.toDTO(currentUser);
        Instant now = Instant.now();
        super.update(
                id,
                artifact.toBuilder()
                        .modificationUser(user)
                        .modificationTime(now)
                        .artifactQuality(newQuality)
                        .qualityLevelReason(reason)
                        .build());

        ArtifactAudited latestRevision = artifactAuditedRepository.findLatestById(Integer.parseInt(id));
        if (latestRevision == null) {
            throw new RepositoryViolationException("Entity should exist in the DB");
        }
        return artifactRevisionMapper.toDTO(latestRevision);
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

    private ArtifactQuality validateProvidedArtifactQuality(String quality, boolean isLoggedInUserSystemUser) {

        ArtifactQuality newQuality;
        try {
            newQuality = ArtifactQuality.valueOf(quality.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEntityException("Artifact quality: " + quality + " does not exist.");
        }

        EnumSet<ArtifactQuality> allowedQualities = EnumSet
                .of(ArtifactQuality.NEW, ArtifactQuality.VERIFIED, ArtifactQuality.TESTED, ArtifactQuality.DEPRECATED);

        if (isLoggedInUserSystemUser) {
            allowedQualities.add(ArtifactQuality.BLACKLISTED);
            allowedQualities.add(ArtifactQuality.DELETED);
        }

        if (!allowedQualities.contains(newQuality)) {
            throw new InvalidEntityException("Artifact quality level can be changed only to " + allowedQualities);
        }

        return newQuality;
    }

    private void validateIfArtifactQualityIsModifiable(org.jboss.pnc.dto.Artifact artifact) {
        // Artifacts which have TEMPORARY, DELETED, BLACKLISTED, DEPRECATED quality levels cannot be changed
        EnumSet<ArtifactQuality> forbiddenQualities = EnumSet.of(
                ArtifactQuality.TEMPORARY,
                ArtifactQuality.DELETED,
                ArtifactQuality.BLACKLISTED,
                ArtifactQuality.DEPRECATED);

        if (forbiddenQualities.contains(artifact.getArtifactQuality())) {
            throw new ConflictedEntryException(
                    "Artifact " + artifact.getId() + " with quality " + artifact.getArtifactQuality()
                            + " cannot be changed to another quality level.",
                    Artifact.class,
                    artifact.getId().toString());
        }
    }
}
