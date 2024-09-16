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
package org.jboss.pnc.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.bpm.causeway.BuildPushOperation;
import org.jboss.pnc.bpm.causeway.BuildResultPushManager;
import org.jboss.pnc.bpm.causeway.InProgress;
import org.jboss.pnc.bpm.causeway.Result;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.validation.AlreadyRunningException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.OperationNotAllowedException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildPushResultMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.InconsistentDataException;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jboss.pnc.api.constants.BuildConfigurationParameterKeys.BREW_BUILD_NAME;
import static org.jboss.pnc.api.constants.MDCKeys.BUILD_ID_KEY;
import static org.jboss.pnc.enums.ArtifactQuality.BLACKLISTED;
import static org.jboss.pnc.enums.ArtifactQuality.DELETED;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RequestScoped
@Slf4j
public class BrewPusherImpl implements BrewPusher {

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private ArtifactRepository artifactRepository;

    @Inject
    private BuildRecordPushResultRepository buildRecordPushResultRepository;

    @Inject
    private BuildResultPushManager buildResultPushManager;

    @Inject
    private GlobalModuleGroup globalModuleGroupConfiguration;

    @Inject
    private BuildPushResultMapper buildPushResultMapper;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    private final static EnumSet<ArtifactQuality> ARTIFACT_BAD_QUALITIES = EnumSet.of(DELETED, BLACKLISTED);

    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.brewpush");

    @Override
    public Set<BuildPushResult> pushGroup(String buildGroupId, String tagPrefix) {
        BuildPushParameters buildPushParameters = BuildPushParameters.builder()
                .tagPrefix(tagPrefix)
                .reimport(false)
                .build();
        Base32LongID id = GroupBuildMapper.idMapper.toEntity(buildGroupId);
        List<BuildRecord> buildRecords = buildRecordRepository
                .queryWithPredicates(BuildRecordPredicates.withBuildConfigSetRecordId(id));

        Set<BuildPushResult> results = new HashSet<>();
        for (BuildRecord buildRecord : buildRecords) {
            Long buildPushResultId = Sequence.nextId();
            MDCUtils.addProcessContext(buildPushResultId.toString());
            MDCUtils.addCustomContext(BUILD_ID_KEY, buildRecord.getId().getLongId());
            try {
                results.add(doPushBuild(buildRecord.getId(), buildPushParameters, buildPushResultId));
            } catch (OperationNotAllowedException | AlreadyRunningException e) {
                results.add(
                        BuildPushResult.builder()
                                .status(BuildPushStatus.REJECTED)
                                .id(buildPushResultId.toString())
                                .buildId(BuildMapper.idMapper.toDto(buildRecord.getId()))
                                .message(e.getMessage())
                                .build());
            } catch (InconsistentDataException | ProcessException e) {
                results.add(
                        BuildPushResult.builder()
                                .status(BuildPushStatus.SYSTEM_ERROR)
                                .id(buildPushResultId.toString())
                                .buildId(BuildMapper.idMapper.toDto(buildRecord.getId()))
                                .message(e.getMessage())
                                .build());
            } finally {
                MDCUtils.removeProcessContext();
                MDCUtils.removeCustomContext(BUILD_ID_KEY);
            }
        }
        return results;
    }

    @Override
    public BuildPushResult pushBuild(String buildId, BuildPushParameters buildPushParameters) throws ProcessException {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        BuildRecord build = buildRecordRepository.queryById(BuildMapper.idMapper.toEntity(buildId));
        if (build.getStatus().equals(BuildStatus.NO_REBUILD_REQUIRED)) {
            throw new OperationNotAllowedException(
                    "Build has NO_REBUILD_REQUIRED status, push last successful build or use force-rebuild.");
        }
        Long buildPushResultId = Sequence.nextId();
        MDCUtils.addProcessContext(buildPushResultId.toString());
        MDCUtils.addCustomContext(BUILD_ID_KEY, id.getLongId());
        try {
            return doPushBuild(id, buildPushParameters, buildPushResultId);
        } finally {
            MDCUtils.removeProcessContext();
            MDCUtils.removeCustomContext(BUILD_ID_KEY);
        }
    }

    private BuildPushResult doPushBuild(
            Base32LongID buildId,
            BuildPushParameters buildPushParameters,
            Long buildPushResultId) throws ProcessException {

        userLog.info("Push started."); // TODO START timing event
        // collect and validate input data
        BuildRecord buildRecord = getLatestSuccessfullyExecutedBuildRecord(buildId);
        if (buildRecord.getExecutionRootName() == null && !buildRecord.getBuildConfigurationAudited()
                .getGenericParameters()
                .containsKey(BREW_BUILD_NAME.name())) {
            throw new InvalidEntityException(
                    "Build " + buildId + " cannot be pushed to brew, because it is missing "
                            + Attributes.BUILD_BREW_NAME + " attribute with brew name.");
        }
        List<Artifact> artifacts = artifactRepository
                .queryWithPredicates(ArtifactPredicates.withBuildRecordId(buildRecord.getId()));
        if (hasBadArtifactQuality(artifacts)) {
            String message = "Build contains artifacts of insufficient quality: BLACKLISTED/DELETED.";
            log.debug(message);
            BuildPushResult pushResult = BuildPushResult.builder()
                    .buildId(BuildMapper.idMapper.toDto(buildId))
                    .status(BuildPushStatus.REJECTED)
                    .id(buildPushResultId.toString())
                    .logContext(buildPushResultId.toString())
                    .message(message)
                    .build();
            throw new OperationNotAllowedException(message, pushResult);
        }

        log.debug("Pushing Build.id {}.", buildRecord.getId());

        BuildPushOperation buildPushOperation = new BuildPushOperation(
                buildRecord,
                buildPushResultId,
                buildPushParameters.getTagPrefix(),
                buildPushParameters.isReimport(),
                getCompleteCallbackUrlTemplate());

        Result pushResult = buildResultPushManager.push(buildPushOperation, keycloakServiceClient.getAuthToken());
        log.info("Push Result {}.", pushResult);

        BuildPushResult result = BuildPushResult.builder()
                .id(pushResult.getId())
                .buildId(pushResult.getBuildId())
                .status(pushResult.getStatus())
                .logContext(pushResult.getId())
                .message(pushResult.getMessage())
                .build();

        // verify operation status
        switch (pushResult.getStatus()) {
            case ACCEPTED:
                userLog.info("Push ACCEPTED.");
                return result;
            case REJECTED:
                userLog.warn("Push REJECTED.");
                throw new AlreadyRunningException(pushResult.getMessage(), result);
            case SYSTEM_ERROR:
                userLog.error("Brew push failed: " + pushResult.getMessage());
                throw new ProcessException(pushResult.getMessage());
            default:
                userLog.error("Invalid push result status.");
                throw new ProcessException("Invalid push result status.");
        }
    }

    private boolean hasBadArtifactQuality(Collection<Artifact> builtArtifacts) {
        return builtArtifacts.stream().map(Artifact::getArtifactQuality).anyMatch(ARTIFACT_BAD_QUALITIES::contains);
    }

    /**
     *
     * @param buildRecordId
     * @return Latest build record with status success or null if the build record does not exist.
     * @throws InconsistentDataException when there is no SUCCESS status before NO_REBUILD_REQUIRED
     * @throws InvalidEntityException when the status is not SUCCESS or NO_REBUILD_REQUIRED
     */
    private BuildRecord getLatestSuccessfullyExecutedBuildRecord(Base32LongID buildRecordId) {
        BuildRecord buildRecord = buildRecordRepository.findByIdFetchProperties(buildRecordId);
        if (buildRecord == null) {
            throw new EmptyEntityException("Build record not found.");
        }

        switch (buildRecord.getStatus()) {
            case SUCCESS:
                return buildRecord;
            case NO_REBUILD_REQUIRED:
                // if status is NO_REBUILD_REQUIRED, find the associated BuildRecord which was linked as the no rebuild
                // cause
                BuildRecord noRebuildCause = buildRecord.getNoRebuildCause();
                if (noRebuildCause != null) {
                    return noRebuildCause;
                } else {
                    String message = "There is no SUCCESS build before NO_REBUILD_REQUIRED.";
                    log.error(message);
                    throw new InconsistentDataException(message);
                }
            default:
                // Build status is not SUCCESS or NO_REBUILD_REQUIRED.
                throw new OperationNotAllowedException("Not allowed to push failed build.");
        }
    }

    @Override
    public boolean brewPushCancel(String buildId) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        Optional<InProgress.Context> pushContext = buildResultPushManager.getContext(id);
        if (pushContext.isPresent()) {
            MDCUtils.addProcessContext(pushContext.get().getPushResultId());
            MDCUtils.addCustomContext(BUILD_ID_KEY, id.getLongId());
            userLog.info("Build push cancel requested.");
            try {
                return buildResultPushManager.cancelInProgressPush(id);
            } finally {
                MDCUtils.removeProcessContext();
                MDCUtils.removeCustomContext(BUILD_ID_KEY);
            }
        } else {
            throw new EmptyEntityException("There is no running push operation for build id: " + buildId);
        }
    }

    @Override
    public BuildPushResult brewPushComplete(String buildId, BuildPushResult buildPushResult) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        MDCUtils.addProcessContext(buildPushResult.getId());
        MDCUtils.addCustomContext(BUILD_ID_KEY, id.getLongId());
        try {
            log.info(
                    "Received completion notification for BuildRecord.id: {}. Object received: {}.",
                    buildId,
                    buildPushResult);

            buildResultPushManager.complete(id, buildPushResultMapper.toEntity(buildPushResult));
            userLog.info("Brew push completed."); // TODO END timing event
            return buildPushResult;
        } finally {
            MDCUtils.removeProcessContext();
            MDCUtils.removeCustomContext(BUILD_ID_KEY);
        }
    }

    @Override
    public BuildPushResult getBrewPushResult(String buildId) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        BuildPushResult result = null;
        Optional<InProgress.Context> pushContext = buildResultPushManager.getContext(id);
        if (pushContext.isPresent()) {
            result = BuildPushResult.builder()
                    .buildId(buildId)
                    .status(BuildPushStatus.ACCEPTED)
                    .logContext(pushContext.get().getPushResultId())
                    .build();
        } else {
            BuildRecordPushResult latestForBuildRecord = buildRecordPushResultRepository.getLatestForBuildRecord(id);
            if (latestForBuildRecord != null) {
                return buildPushResultMapper.toDTO(latestForBuildRecord);
            }
        }
        return result;
    }

    private String getCompleteCallbackUrlTemplate() {
        String pncBaseUrl = StringUtils.stripEndingSlash(globalModuleGroupConfiguration.getPncUrl());
        return pncBaseUrl + "/builds/%s/brew-push/complete";
    }

}
