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
package org.jboss.pnc.remotecoordinator.maintenance;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushReport;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildPushReportRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Bean providing an interface to delete temporary builds
 *
 * @author Jakub Bartecek
 */
@Stateless
public class TemporaryBuildsCleaner {
    private final Logger log = LoggerFactory.getLogger(TemporaryBuildsCleaner.class);

    private BuildRecordRepository buildRecordRepository;

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private ArtifactRepository artifactRepository;

    private BuildPushOperationRepository buildPushOperationRepository;

    private BuildPushReportRepository buildPushReportRepository;

    private RemoteBuildsCleaner remoteBuildsCleaner;

    @Deprecated
    public TemporaryBuildsCleaner() {
    }

    @Inject
    public TemporaryBuildsCleaner(
            BuildRecordRepository buildRecordRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            ArtifactRepository artifactRepository,
            BuildPushOperationRepository buildPushOperationRepository,
            BuildPushReportRepository buildPushReportRepository,
            RemoteBuildsCleaner remoteBuildsCleaner) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.artifactRepository = artifactRepository;
        this.buildPushOperationRepository = buildPushOperationRepository;
        this.buildPushReportRepository = buildPushReportRepository;
        this.remoteBuildsCleaner = remoteBuildsCleaner;
    }

    /**
     * Deletes a temporary build and artifacts created during the build or orphan dependencies used
     *
     * @param buildRecordId BuildRecord to be deleted
     * @return true if success
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Result deleteTemporaryBuild(Base32LongID buildRecordId) throws ValidationException {
        BuildRecord buildRecord = buildRecordRepository.findByIdFetchAllProperties(buildRecordId);
        if (buildRecord == null) {
            throw new ValidationException(
                    "Cannot delete temporary build with id " + BuildMapper.idMapper.toDto(buildRecordId)
                            + " as no build with this id exists");
        }
        return deleteTemporaryBuild(buildRecord);
    }

    private Result deleteTemporaryBuild(BuildRecord buildRecord) throws ValidationException {

        if (!buildRecord.isTemporaryBuild()) {
            throw new ValidationException("Only deletion of the temporary builds is allowed");
        }

        // first delete BRs where this build is noRebuildCause
        List<BuildRecord> noRebuildBRs = buildRecordRepository.getBuildByCausingRecord(buildRecord.getId());
        for (BuildRecord noRebuildBR : noRebuildBRs) {
            log.info("Deleting build {} which has noRebuildCause {}.", noRebuildBR.getId(), buildRecord.getId());
            deleteTemporaryBuild(noRebuildBR.getId());
        }

        // delete the build itself
        log.info(
                "Starting deletion of a temporary build {}; Built artifacts: {}; Dependencies: {}",
                buildRecord,
                buildRecord.getBuiltArtifacts(),
                buildRecord.getDependencies());

        String externalBuildId = BuildMapper.idMapper.toDto(buildRecord.getId());

        BuildStatus buildStatus = buildRecord.getStatus();
        if (buildStatus.isFinal() && buildStatus != BuildStatus.NO_REBUILD_REQUIRED
                && !buildStatus.name().startsWith("REJECTED")) {
            // there's only an indy repository if the build actually tried to run. NO_REBUILD_REQUIRED and REJECTED_*
            // never ran
            Result result = remoteBuildsCleaner.deleteRemoteBuilds(buildRecord);
            if (!result.isSuccess()) {
                log.error("Failed to delete remote temporary builds for BR.id:{}.", buildRecord.getId());
                return new Result(externalBuildId, ResultStatus.FAILED, "Failed to delete remote temporary builds.");
            }
        }

        removeBuiltArtifacts(buildRecord);
        removeAttachedArtifacts(buildRecord);
        removeBuildPushOperations(buildRecord);

        buildRecordRepository.delete(buildRecord.getId());
        log.info("Deletion of the temporary build {} finished successfully.", buildRecord);
        return new Result(externalBuildId, ResultStatus.SUCCESS);
    }

    private void deleteArtifact(Artifact artifact) {
        if (artifact.getDependantBuildRecords().isEmpty()) {
            log.info("Deleting temporary artifact: {}", artifact.getDescriptiveString());
            artifactRepository.delete(artifact.getId());
        } else {
            log.info("Marking temporary artifact as DELETED: {}", artifact.getDescriptiveString());
            artifact.setArtifactQuality(ArtifactQuality.DELETED);
            artifactRepository.save(artifact);
        }
    }

    /**
     * Deletes a BuildConfigSetRecord and BuildRecords produced in the build
     *
     * @param buildConfigSetRecordId BuildConfigSetRecord to be deleted
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Result deleteTemporaryBuildConfigSetRecord(Base32LongID buildConfigSetRecordId) throws ValidationException {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);

        if (buildConfigSetRecord == null) {
            throw new ValidationException(
                    "Cannot delete temporary BuildConfigSetRecord with id " + buildConfigSetRecordId
                            + " as no BuildConfigSetRecord with this id exists");
        }

        if (!buildConfigSetRecord.isTemporaryBuild()) {
            throw new ValidationException("Only deletion of the temporary builds is allowed");
        }
        log.info("Starting deletion of a temporary build record set {}", buildConfigSetRecord);

        for (BuildRecord br : buildConfigSetRecord.getBuildRecords()) {
            br.setBuildConfigSetRecord(null);
            buildRecordRepository.save(br);
        }
        buildConfigSetRecordRepository.delete(buildConfigSetRecord.getId());

        log.info("Deletion of a temporary build record set {} finished successfully.", buildConfigSetRecord);
        return new Result(buildConfigSetRecordId.toString(), ResultStatus.SUCCESS);
    }

    private void removeBuiltArtifacts(BuildRecord buildRecord) {
        Set<Artifact> toDelete = new HashSet<>(buildRecord.getBuiltArtifacts());
        for (Artifact artifact : toDelete) {
            log.debug(
                    String.format(
                            "Deleting relation BR-Artifact. BR=%s, artifact=%s",
                            buildRecord,
                            artifact.getDescriptiveString()));

            if (!artifact.getDeliveredInProductMilestones().isEmpty()) {
                log.error(
                        "Temporary artifact was delivered in milestone! Artifact: " + artifact + "\n Milestones: "
                                + artifact.getDeliveredInProductMilestones());
                continue;
            }

            artifact.setBuildRecord(null);
            deleteArtifact(artifact);
        }
    }

    private void removeAttachedArtifacts(BuildRecord buildRecord) {
        Set<Artifact> toDelete = new HashSet<>(buildRecord.getAttachedArtifacts());
        for (Artifact artifact : toDelete) {
            log.debug(
                    String.format(
                            "Deleting relation BR-Artifact. BR=%s, artifact=%s",
                            buildRecord,
                            artifact.getDescriptiveString()));

            if (!artifact.getDeliveredInProductMilestones().isEmpty()) {
                log.error(
                        "Temporary artifact was delivered in milestone! Artifact: " + artifact + "\n Milestones: "
                                + artifact.getDeliveredInProductMilestones());
                continue;
            }

            artifact.setAttachedBuild(null);
            deleteArtifact(artifact);
        }
    }

    /**
     * Remove build push operations linked to a build record
     * 
     * @param buildRecord build record to remove
     */
    private void removeBuildPushOperations(BuildRecord buildRecord) {

        log.debug("Deleting build record {} brew push operations", buildRecord.getId());
        List<BuildPushOperation> buildPushOperations = buildPushOperationRepository
                .queryWithPredicates(BuildPushPredicates.withBuild(buildRecord.getId()));

        if (buildPushOperations != null) {
            for (BuildPushOperation buildPushOperation : buildPushOperations) {
                log.debug("Deleting build push operation {}", buildPushOperation);
                BuildPushReport report = buildPushOperation.getReport();
                if (report != null) {
                    buildPushReportRepository.delete(report);
                }
                buildPushOperationRepository.delete(buildPushOperation);
            }
        }
    }
}
