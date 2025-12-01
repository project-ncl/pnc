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
import org.jboss.pnc.api.constants.OperationParameters;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dingroguclient.DingroguBuildPushDTO;
import org.jboss.pnc.dingroguclient.DingroguClient;
import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.OperationsManager;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.facade.validation.OperationNotAllowedException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildPushOperationMapper;
import org.jboss.pnc.mapper.api.BuildPushReportMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushReport;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.InconsistentDataException;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.predicates.OperationPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildPushReportRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.api.constants.MDCKeys.BUILD_ID_KEY;

/**
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Slf4j
@ApplicationScoped
public class BrewPusherImpl implements BrewPusher {

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private OperationsManager operationsManager;

    @Inject
    private BuildPushOperationMapper buildPushOperationMapper;

    @Inject
    private BuildPushReportMapper buildPushReportMapper;

    @Inject
    private BuildPushOperationRepository buildPushOperationRepository;

    @Inject
    private BuildPushReportRepository buildPushReportRepository;

    @Inject
    private DingroguClient dingroguClient;

    @Inject
    private GlobalModuleGroup globalConfig;

    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.brewpush");

    @Override
    public Set<org.jboss.pnc.dto.BuildPushOperation> pushGroup(String buildGroupId, String tagPrefix) {
        BuildPushParameters buildPushParameters = BuildPushParameters.builder()
                .tagPrefix(tagPrefix)
                .reimport(false)
                .build();
        Base32LongID id = GroupBuildMapper.idMapper.toEntity(buildGroupId);
        List<Base32LongID> buildRecords = buildRecordRepository
                .queryIdsWithPredicates(BuildRecordPredicates.withBuildConfigSetRecordId(id));

        Set<org.jboss.pnc.dto.BuildPushOperation> results = new HashSet<>();
        for (Base32LongID buildId : buildRecords) {
            BuildRecord build = getLatestSuccessfullyExecutedBuildRecord(buildId);
            org.jboss.pnc.dto.BuildPushOperation buildPushOperation = doPushBuild(build, buildPushParameters, null);
            results.add(buildPushOperation);
            MDCUtils.removeProcessContext();
        }
        return results;
    }

    @Override
    public org.jboss.pnc.dto.BuildPushOperation pushBuild(String buildId, BuildPushParameters buildPushParameters) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        return pushBuild(id, buildPushParameters, null);
    }

    @Override
    public org.jboss.pnc.dto.BuildPushOperation pushBuild(
            Base32LongID id,
            BuildPushParameters buildPushParameters,
            String milestoneId) {
        BuildRecord build = buildRecordRepository.queryById(id);
        if (build == null) {
            throw new EmptyEntityException("Build with id: " + id + " does not exist!");
        }
        if (build.getStatus().equals(BuildStatus.NO_REBUILD_REQUIRED)) {
            throw new OperationNotAllowedException(
                    "Build has NO_REBUILD_REQUIRED status, push last successful build or use force-rebuild.");
        }
        if (!build.getStatus().equals(BuildStatus.SUCCESS)) {
            throw new OperationNotAllowedException("You can push only successful builds.");
        }
        return doPushBuild(build, buildPushParameters, milestoneId);
    }

    private org.jboss.pnc.dto.BuildPushOperation doPushBuild(
            BuildRecord build,
            BuildPushParameters buildPushParameters,
            String milestoneId) {
        Map<String, String> inputParams = new HashMap<>();
        inputParams.put(OperationParameters.BUILD_PUSH_TAG_PREFIX, buildPushParameters.getTagPrefix());
        inputParams.put(OperationParameters.BUILD_PUSH_REIMPORT, buildPushParameters.isReimport() ? "true" : "false");
        if (milestoneId != null) {
            inputParams.put(OperationParameters.BUILD_PUSH_MILESTONE_CLOSE, milestoneId);
        }

        BuildPushOperation operation = operationsManager.newBuildPushOperation(build, inputParams);

        try {
            log.info("Starting brew push of build {}.", build.getId());
            startPush(operation, buildPushParameters.getTagPrefix(), buildPushParameters.isReimport());
            return buildPushOperationMapper.toDTO(
                    (BuildPushOperation) operationsManager
                            .updateProgress(operation.getId(), ProgressStatus.IN_PROGRESS));
        } catch (RuntimeException ex) {
            operationsManager.setResult(operation.getId(), OperationResult.SYSTEM_ERROR);
            throw ex;
        }
    }

    private void startPush(BuildPushOperation operation, String tagPrefix, boolean reimport) {
        try {

            MDCUtils.addProcessContext(operation.getId().getId());
            MDCUtils.addCustomContext(BUILD_ID_KEY, operation.getBuild().getId().getId());

            dingroguClient.submitBuildPush(
                    DingroguBuildPushDTO.builder()
                            .operationId(operation.getId().getId())
                            .buildId(operation.getBuild().getId().getId())
                            .tagPrefix(tagPrefix)
                            .reimport(reimport)
                            .orchUrl(globalConfig.getPncUrl())
                            .causewayUrl(globalConfig.getExternalCausewayUrl())
                            .username(operation.getUser().getUsername())
                            .build());
        } finally {
            MDCUtils.removeProcessContext();
            MDCUtils.removeCustomContext(BUILD_ID_KEY);
        }
    }

    /**
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
    public void cancelPushOfBuild(String buildId) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        List<BuildPushOperation> buildPushOperations = buildPushOperationRepository
                .queryWithPredicates(OperationPredicates.inProgress(), BuildPushPredicates.withBuild(id));
        if (buildPushOperations.isEmpty()) {
            throw new EmptyEntityException("There is no running push operation for build id: " + buildId);
        }
        buildPushOperations
                .forEach(operation -> dingroguClient.submitCancelProcessInstance(operation.getId().getId()));
    }

    @Override
    public void cancelPushOfMilestone(String milestoneId) {
        List<BuildPushOperation> buildPushOperations = buildPushOperationRepository
                .queryWithPredicates(OperationPredicates.inProgress())
                .stream()
                .filter(
                        o -> milestoneId
                                .equals(o.getOperationParameters().get(OperationParameters.BUILD_PUSH_MILESTONE_CLOSE)))
                .collect(Collectors.toList());

        if (buildPushOperations.isEmpty()) {
            throw new EmptyEntityException("There is no running push operation for milestone: " + milestoneId);
        }
        buildPushOperations
                .forEach(operation -> dingroguClient.cancelProcessInstance(List.of(), operation.getId().getId()));
    }

    @Override
    public void brewPushComplete(String buildId, BuildPushCompleted buildPushCompletion) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        MDCUtils.addProcessContext(buildPushCompletion.getOperationId());
        Base32LongID operationId = buildPushOperationMapper.getIdMapper()
                .toEntity(buildPushCompletion.getOperationId());
        BuildPushOperation buildPushOperation = buildPushOperationRepository.queryById(operationId);
        if (buildPushOperation == null) {
            throw new EmptyEntityException("Build push operation with id " + operationId + " not found.");
        }
        MDCUtils.addCustomContext(BUILD_ID_KEY, id.getLongId());
        try {
            log.info(
                    "Received completion notification for BuildRecord.id: {}. Object received: {}.",
                    buildId,
                    buildPushCompletion);

            var report = BuildPushReport.builder()
                    .id(id)
                    .operation(buildPushOperation)
                    .brewBuildId(buildPushCompletion.getBrewBuildId())
                    .brewBuildUrl(buildPushCompletion.getBrewBuildUrl())
                    .build();
            buildPushReportRepository.save(report);

            userLog.info("Brew push completed."); // TODO END timing event
        } finally {
            MDCUtils.removeProcessContext();
            MDCUtils.removeCustomContext(BUILD_ID_KEY);
        }
    }

    @Override
    public org.jboss.pnc.dto.BuildPushReport getBrewPushResult(String buildId) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);

        var buildPushOperations = buildPushOperationRepository.queryWithPredicates(BuildPushPredicates.withBuild(id));

        Optional<BuildPushOperation> latestOperation;
        latestOperation = buildPushOperations.stream()
                .filter(o -> o.getProgressStatus() != ProgressStatus.FINISHED)
                .max(Comparator.comparing(BuildPushOperation::getSubmitTime));

        if (latestOperation.isPresent()) {
            return buildPushReportMapper.fromOperation(latestOperation.get());
        }

        latestOperation = buildPushOperations.stream()
                .filter(o -> o.getProgressStatus() == ProgressStatus.FINISHED)
                .max(Comparator.comparing(BuildPushOperation::getEndTime));

        if (latestOperation.isPresent()) {
            BuildPushOperation operation = latestOperation.get();
            if (operation.getResult() == OperationResult.SUCCESSFUL) {
                BuildPushReport buildPushReport = buildPushReportRepository.queryById(operation.getId());
                return buildPushReportMapper.toRef(buildPushReport);
            } else {
                return buildPushReportMapper.fromOperation(operation);
            }
        }

        return null;
    }

    @Override
    public org.jboss.pnc.dto.BuildPushReport getBrewPushReport(String operationId) {
        Base32LongID id = BuildMapper.idMapper.toEntity(operationId);

        BuildPushReport buildPushReport = buildPushReportRepository.queryById(id);
        if (buildPushReport != null) {
            return buildPushReportMapper.toRef(buildPushReport);
        }

        BuildPushOperation buildPushOperation = buildPushOperationRepository.queryById(id);
        return buildPushReportMapper.fromOperation(buildPushOperation);
    }

}
