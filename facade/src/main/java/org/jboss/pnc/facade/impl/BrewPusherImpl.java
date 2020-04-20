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
package org.jboss.pnc.facade.impl;

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.bpm.causeway.BuildPushOperation;
import org.jboss.pnc.bpm.causeway.BuildResultPushManager;
import org.jboss.pnc.bpm.causeway.Result;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.AlreadyRunningException;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.OperationNotAllowedException;
import org.jboss.pnc.mapper.api.BuildPushResultMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.InconsistentDataException;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.jboss.pnc.constants.MDCKeys.BUILD_ID_KEY;
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
    private UserService userService;

    private final static EnumSet<ArtifactQuality> ARTIFACT_BAD_QUALITIES = EnumSet.of(DELETED, BLACKLISTED);

    @Override
    public Set<BuildPushResult> pushGroup(int buildGroupId, String tagPrefix) {
        BuildPushParameters buildPushParameters = BuildPushParameters.builder()
                .tagPrefix(tagPrefix)
                .reimport(false)
                .build();
        List<BuildRecord> buildRecords = buildRecordRepository
                .queryWithPredicates(BuildRecordPredicates.withBuildConfigSetRecordId(buildGroupId));

        Set<BuildPushResult> results = new HashSet<>();
        for (BuildRecord buildRecord : buildRecords) {
            UUID buildPushResultId = UUID.randomUUID();
            MDCUtils.addProcessContext(buildPushResultId.toString());
            MDCUtils.addCustomContext(BUILD_ID_KEY, buildRecord.getId().toString());
            try {
                results.add(doPushBuild(buildRecord.getId(), buildPushParameters, buildPushResultId));
            } catch (OperationNotAllowedException e) {
                results.add(
                        BuildPushResult.builder()
                                .status(BuildPushStatus.REJECTED)
                                .id(buildPushResultId.toString())
                                .buildId(buildRecord.getId().toString())
                                .message(e.getMessage())
                                .build());
            } catch (InconsistentDataException e) {
                results.add(
                        BuildPushResult.builder()
                                .status(BuildPushStatus.SYSTEM_ERROR)
                                .id(buildPushResultId.toString())
                                .buildId(buildRecord.getId().toString())
                                .message(e.getMessage())
                                .build());
            } catch (AlreadyRunningException e) {
                results.add(
                        BuildPushResult.builder()
                                .status(BuildPushStatus.REJECTED)
                                .id(buildPushResultId.toString())
                                .buildId(buildRecord.getId().toString())
                                .message(e.getMessage())
                                .build());
            } catch (ProcessException e) {
                results.add(
                        BuildPushResult.builder()
                                .status(BuildPushStatus.SYSTEM_ERROR)
                                .id(buildPushResultId.toString())
                                .buildId(buildRecord.getId().toString())
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
        UUID buildPushResultId = UUID.randomUUID();
        MDCUtils.addProcessContext(buildPushResultId.toString());
        MDCUtils.addCustomContext(BUILD_ID_KEY, buildId);
        try {
            return doPushBuild(Integer.parseInt(buildId), buildPushParameters, buildPushResultId);
        } finally {
            MDCUtils.removeProcessContext();
            MDCUtils.removeCustomContext(BUILD_ID_KEY);
        }
    }

    private BuildPushResult doPushBuild(
            Integer buildId,
            BuildPushParameters buildPushParameters,
            UUID buildPushResultId) throws ProcessException {

        // collect and validate input data
        BuildRecord buildRecord = getLatestSuccessfullyExecutedBuildRecord(buildId);
        List<Artifact> artifacts = artifactRepository
                .queryWithPredicates(ArtifactPredicates.withBuildRecordId(buildRecord.getId()));
        if (hasBadArtifactQuality(artifacts)) {
            String message = "Build contains artifacts of insufficient quality: BLACKLISTED/DELETED.";
            log.debug(message);
            BuildPushResult pushResult = BuildPushResult.builder()
                    .buildId(buildId.toString())
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

        Result pushResult = buildResultPushManager.push(buildPushOperation, userService.currentUserToken());
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
            case SUCCESS:
                return result;
            case REJECTED:
                throw new AlreadyRunningException(pushResult.getMessage(), result);
            case SYSTEM_ERROR:
                log.error("Brew push failed: " + pushResult.getMessage());
                throw new ProcessException(pushResult.getMessage());
            default:
                log.error("Invalid push result status.");
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
    private BuildRecord getLatestSuccessfullyExecutedBuildRecord(Integer buildRecordId) {
        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);
        if (buildRecord == null) {
            throw new EmptyEntityException("Build record not found.");
        }
        if (BuildStatus.SUCCESS.equals(buildRecord.getStatus())) {
            return buildRecord;
        } else if (BuildStatus.NO_REBUILD_REQUIRED.equals(buildRecord.getStatus())) {
            // if status is NO_REBUILD_REQUIRED, find the last BuildRecord with status SUCCESS for the same idRev.
            IdRev idRev = buildRecord.getBuildConfigurationAuditedIdRev();
            BuildRecord latestSuccessfulBuildRecord = buildRecordRepository
                    .getLatestSuccessfulBuildRecord(idRev, buildRecord.isTemporaryBuild());
            if (latestSuccessfulBuildRecord != null) {
                return latestSuccessfulBuildRecord;
            } else {
                String message = "There is no SUCCESS build before NO_REBUILD_REQUIRED.";
                log.error(message);
                // In case of temporary builds it can happen. The SUCCESS build might be already garbage-collected.
                throw new InconsistentDataException(message);
            }
        } else {
            // Build status is not SUCCESS or NO_REBUILD_REQUIRED.
            throw new OperationNotAllowedException("Not allowed to push failed build.");
        }
    }

    @Override
    public boolean brewPushCancel(int buildId) {
        return buildResultPushManager.cancelInProgressPush(buildId);
    }

    @Override
    public BuildPushResult brewPushComplete(int buildId, BuildPushResult buildPushResult) {
        MDCUtils.addProcessContext(buildPushResult.getId());
        MDCUtils.addCustomContext(BUILD_ID_KEY, Integer.toString(buildId));
        try {
            log.info(
                    "Received completion notification for BuildRecord.id: {}. Object received: {}.",
                    buildId,
                    buildPushResult);

            buildResultPushManager.complete(buildId, buildPushResultMapper.toEntity(buildPushResult));
            return buildPushResult;
        } finally {
            MDCUtils.removeProcessContext();
            MDCUtils.removeCustomContext(BUILD_ID_KEY);
        }
    }

    @Override
    public BuildPushResult getBrewPushResult(int buildId) {
        BuildPushResult result = null;
        if (buildResultPushManager.getInProgress().contains(buildId)) {
            result = BuildPushResult.builder()
                    .buildId(String.valueOf(buildId))
                    .status(BuildPushStatus.ACCEPTED)
                    .logContext(Integer.toString(buildId))
                    .build();
        } else {
            BuildRecordPushResult latestForBuildRecord = buildRecordPushResultRepository
                    .getLatestForBuildRecord(buildId);
            if (latestForBuildRecord != null) {
                ProductMilestoneRelease productMilestoneRelease = latestForBuildRecord.getProductMilestoneRelease();
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
