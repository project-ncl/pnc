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
package org.jboss.pnc.coordinator.maintenance;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
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

    private RemoteBuildsCleaner remoteBuildsCleaner;

    @Deprecated
    public TemporaryBuildsCleaner() {
    }

    @Inject
    public TemporaryBuildsCleaner(
            BuildRecordRepository buildRecordRepository,
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            ArtifactRepository artifactRepository,
            RemoteBuildsCleaner remoteBuildsCleaner) {
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.artifactRepository = artifactRepository;
        this.remoteBuildsCleaner = remoteBuildsCleaner;
    }

    /**
     * Deletes a temporary build and artifacts created during the build or orphan dependencies used
     *
     * @param buildRecordId BuildRecord to be deleted
     * @param authToken
     * @return true if success
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Result deleteTemporaryBuild(Long buildRecordId, String authToken) throws ValidationException {
        BuildRecord buildRecord = buildRecordRepository.findByIdFetchAllProperties(buildRecordId);
        if (buildRecord == null) {
            throw new ValidationException(
                    "Cannot delete temporary build with id " + buildRecordId + " as no build with this id exists");
        }
        return deleteTemporaryBuild(buildRecord, authToken);
    }

    private Result deleteTemporaryBuild(BuildRecord buildRecord, String authToken) throws ValidationException {
        Long buildRecordId = buildRecord.getId();

        if (!buildRecord.isTemporaryBuild()) {
            throw new ValidationException("Only deletion of the temporary builds is allowed");
        }

        // first delete BRs where this build is noRebuildCause
        List<BuildRecord> noRebuildBRs = buildRecordRepository.getBuildByCausingRecord(buildRecordId);
        for (BuildRecord noRebuildBR : noRebuildBRs) {
            log.info("Deleting build " + noRebuildBR.getId() + " which has noRebuildCause " + buildRecordId + ".");
            deleteTemporaryBuild(noRebuildBR.getId(), authToken);
        }

        // delete the build itself
        log.info(
                "Starting deletion of a temporary build " + buildRecord + "; Built artifacts: "
                        + buildRecord.getBuiltArtifacts() + "; Dependencies: " + buildRecord.getDependencies());

        Result result = remoteBuildsCleaner.deleteRemoteBuilds(buildRecord, authToken);
        if (!result.isSuccess()) {
            log.error("Failed to delete remote temporary builds for BR.id:{}.", buildRecord.getId());
            return new Result(
                    buildRecordId.toString(),
                    ResultStatus.FAILED,
                    "Failed to delete remote temporary builds.");
        }

        removeBuiltArtifacts(buildRecord);

        buildRecordRepository.delete(buildRecord.getId());
        log.info("Deletion of the temporary build {} finished successfully.", buildRecord);
        return new Result(buildRecordId.toString(), ResultStatus.SUCCESS);
    }

    private void deleteArtifact(Artifact artifact) {
        if (artifact.getDependantBuildRecords().size() > 0) {
            log.info("Marking temporary artifact as DELETED: " + artifact.getDescriptiveString());
            artifact.setArtifactQuality(ArtifactQuality.DELETED);
            artifactRepository.save(artifact);
        } else {
            log.info("Deleting temporary artifact: " + artifact.getDescriptiveString());
            artifactRepository.delete(artifact.getId());
        }
    }

    /**
     * Deletes a BuildConfigSetRecord and BuildRecords produced in the build
     *
     * @param buildConfigSetRecordId BuildConfigSetRecord to be deleted
     * @param authToken
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Result deleteTemporaryBuildConfigSetRecord(Integer buildConfigSetRecordId, String authToken)
            throws ValidationException {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(buildConfigSetRecordId);

        if (buildConfigSetRecord == null) {
            throw new ValidationException(
                    "Cannot delete temporary BuildConfigSetRecord with id " + buildConfigSetRecordId
                            + " as no BuildConfigSetRecord with this id exists");
        }

        if (!buildConfigSetRecord.isTemporaryBuild()) {
            throw new ValidationException("Only deletion of the temporary builds is allowed");
        }
        log.info("Starting deletion of a temporary build record set " + buildConfigSetRecord);

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

            if (!artifact.getDistributedInProductMilestones().isEmpty()) {
                log.error(
                        "Temporary artifact was distributed in milestone! Artifact: " + artifact.toString()
                                + "\n Milestones: " + artifact.getDistributedInProductMilestones().toString());
                continue;
            }

            artifact.setBuildRecord(null);
            deleteArtifact(artifact);
        }
    }
}
