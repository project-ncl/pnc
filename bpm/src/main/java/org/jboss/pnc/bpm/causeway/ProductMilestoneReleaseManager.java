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
package org.jboss.pnc.bpm.causeway;

import org.jboss.pnc.bpm.BpmEventType;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.bpm.BpmTask;
import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.bpm.model.BpmStringMapNotificationRest;
import org.jboss.pnc.bpm.model.causeway.BuildImportResultRest;
import org.jboss.pnc.bpm.model.causeway.BuildImportStatus;
import org.jboss.pnc.bpm.model.causeway.MilestoneReleaseResultRest;
import org.jboss.pnc.bpm.task.MilestoneReleaseTask;
import org.jboss.pnc.dto.ArtifactImportError;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.MilestoneReleaseStatus;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.jboss.pnc.common.util.CollectionUtils.ofNullableCollection;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/31/16 Time: 8:42 AM
 */
@Stateless
public class ProductMilestoneReleaseManager {

    private static final Logger log = LoggerFactory.getLogger(ProductMilestoneReleaseManager.class);
    public static final String BREW_ID = "brewId";
    public static final String BREW_LINK = "brewLink";

    private BpmManager bpmManager;

    private ArtifactRepository artifactRepository;
    private ProductVersionRepository productVersionRepository;
    private BuildRecordRepository buildRecordRepository;
    private ProductMilestoneReleaseRepository productMilestoneReleaseRepository;
    private ProductMilestoneRepository milestoneRepository;
    private BuildRecordPushResultRepository buildRecordPushResultRepository;

    @Deprecated // for ejb
    public ProductMilestoneReleaseManager() {
    }

    @Inject
    public ProductMilestoneReleaseManager(
            ProductMilestoneReleaseRepository productMilestoneReleaseRepository,
            BpmManager bpmManager,
            ArtifactRepository artifactRepository,
            ProductVersionRepository productVersionRepository,
            BuildRecordRepository buildRecordRepository,
            ProductMilestoneRepository milestoneRepository,
            BuildRecordPushResultRepository buildRecordPushResultRepository) {
        this.productMilestoneReleaseRepository = productMilestoneReleaseRepository;
        this.bpmManager = bpmManager;
        this.artifactRepository = artifactRepository;
        this.productVersionRepository = productVersionRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.milestoneRepository = milestoneRepository;
        this.buildRecordPushResultRepository = buildRecordPushResultRepository;
    }

    /**
     * Starts milestone release process
     * 
     * @param milestone product milestone to start the release for
     * @param accessToken
     * @return
     */
    public ProductMilestoneRelease startRelease(ProductMilestone milestone, String accessToken) {
        ProductMilestoneRelease release = triggerRelease(milestone, accessToken);
        return productMilestoneReleaseRepository.save(release);
    }

    public void cancel(ProductMilestone milestoneInDb) {
        Collection<BpmTask> activeTasks = bpmManager.getActiveTasks();
        Optional<MilestoneReleaseTask> milestoneReleaseTask = activeTasks.stream()
                .map(task -> (MilestoneReleaseTask) task)
                .filter(task -> task.getMilestone().getId().equals(milestoneInDb.getId()))
                .findAny();

        if (milestoneReleaseTask.isPresent()) {
            bpmManager.cancelTask(milestoneReleaseTask.get());
        }

        ProductMilestoneRelease milestoneRelease = productMilestoneReleaseRepository
                .findLatestByMilestone(milestoneInDb);
        milestoneRelease.setStatus(MilestoneReleaseStatus.CANCELED);
        productMilestoneReleaseRepository.save(milestoneRelease);
    }

    public boolean noReleaseInProgress(ProductMilestone milestone) {
        return !getInProgress(milestone).isPresent();
    }

    public Optional<ProductMilestoneRelease> getInProgress(ProductMilestone milestone) {
        ProductMilestoneRelease latestRelease = productMilestoneReleaseRepository.findLatestByMilestone(milestone);
        if (latestRelease != null && latestRelease.getStatus() == MilestoneReleaseStatus.IN_PROGRESS) {
            return Optional.of(latestRelease);
        } else {
            return Optional.empty();
        }
    }

    private <T extends BpmEvent> ProductMilestoneRelease triggerRelease(
            ProductMilestone milestone,
            String accessToken) {
        ProductMilestoneRelease release = new ProductMilestoneRelease();
        release.setStartingDate(new Date());
        release.setMilestone(milestone);
        try {
            MilestoneReleaseTask releaseTask = new MilestoneReleaseTask(milestone, accessToken);
            Integer id = milestone.getId();
            releaseTask.<MilestoneReleaseResultRest> addListener(
                    BpmEventType.BREW_IMPORT_SUCCESS,
                    r -> onSuccessfulPush(id, r));
            releaseTask.<BpmStringMapNotificationRest> addListener(
                    BpmEventType.BREW_IMPORT_ERROR,
                    r -> onFailedPush(milestone.getId(), r));
            release.setStatus(MilestoneReleaseStatus.IN_PROGRESS);
            bpmManager.startTask(releaseTask);
            release.setLog("Brew push task started\n");

            return release;
        } catch (CoreException e) {
            log.error("Error trying to start brew push task for milestone: {}", milestone.getId(), e);
            release.setLog("Brew push BPM task creation failed.\nCheck log for more details.\n");
            release.setStatus(MilestoneReleaseStatus.SYSTEM_ERROR);
            release.setEndDate(new Date());
            return release;
        }
    }

    private void onSuccessfulPush(Integer milestoneId, MilestoneReleaseResultRest result) {
        log.debug("Storing milestone release result: {}", result);
        withMilestone(milestoneId, result, this::storeSuccess);
    }

    private void onFailedPush(Integer milestoneId, BpmStringMapNotificationRest result) {
        log.debug("Storing failed milestone release result: {}", result);
        withMilestone(milestoneId, result, this::storeFailure);
    }

    private <T> void withMilestone(Integer milestoneId, T result, BiConsumer<ProductMilestone, T> consumer) {
        ProductMilestone milestone = milestoneRepository.queryById(milestoneId);

        if (milestone == null) {
            log.error("No milestone found for milestone id {}", milestoneId);
            return;
        }
        consumer.accept(milestone, result);
    }

    private void storeSuccess(ProductMilestone milestone, MilestoneReleaseResultRest result) {
        String message = describeCompletedPush(result);
        updateRelease(milestone, message, result.getReleaseStatus().getMilestoneReleaseStatus());

        for (BuildImportResultRest buildRest : ofNullableCollection(result.getBuilds())) {
            storeBrewBuildParameters(buildRest);
        }

        if (result.getReleaseStatus().getMilestoneReleaseStatus() == MilestoneReleaseStatus.SUCCEEDED) {
            // set milestone end date to now when the release process is successful
            milestone.setEndDate(new Date());
            milestoneRepository.save(milestone);

            removeCurrentFlagFromMilestone(milestone);
        }
    }

    private <T> void storeFailure(ProductMilestone milestone, BpmStringMapNotificationRest result) {
        updateRelease(milestone, "BREW IMPORT FAILED\nResult: " + result, MilestoneReleaseStatus.SYSTEM_ERROR);
    }

    private void storeBrewBuildParameters(BuildImportResultRest buildRest) {
        Integer recordId = buildRest.getBuildRecordId();
        BuildRecord record = buildRecordRepository.queryById(recordId);
        if (record == null) {
            log.error("No record found for record id: {}, skipped saving info: {}", recordId, buildRest);
            return;
        }

        String combinedLog = ArtifactImportError.combineMessages(buildRest.getErrorMessage(), buildRest.getErrors());

        BuildPushStatus status;
        try {
            status = convertStatus(buildRest.getStatus());
        } catch (ProcessManagerException e) {
            log.error("Cannot convert status.", e);
            throw new RuntimeException("Cannot convert status.", e);
        }

        BuildRecordPushResult buildRecordPush = BuildRecordPushResult.newBuilder()
                .buildRecord(record)
                .status(status)
                .log(combinedLog)
                .brewBuildId(buildRest.getBrewBuildId())
                .brewBuildUrl(buildRest.getBrewBuildUrl())
                .tagPrefix("") // TODO tag!
                .build();
        buildRecordPushResultRepository.save(buildRecordPush);
    }

    private BuildPushStatus convertStatus(BuildImportStatus status) throws ProcessManagerException {
        switch (status) {
            case SUCCESSFUL:
                return BuildPushStatus.SUCCESS;
            case FAILED:
                return BuildPushStatus.FAILED;
            case ERROR:
                return BuildPushStatus.SYSTEM_ERROR;
        }
        throw new ProcessManagerException("Invalid BuildImportStatus: " + status.toString());
    }

    private void updateRelease(ProductMilestone milestone, String message, MilestoneReleaseStatus status) {
        ProductMilestoneRelease release = productMilestoneReleaseRepository.findLatestByMilestone(milestone);
        if (release == null) {
            log.error("No milestone release found for milestone {}", milestone.getId());
            return;
        }
        if (status != MilestoneReleaseStatus.IN_PROGRESS) {
            release.setEndDate(new Date());
        }
        release.setStatus(status);
        release.setLog(release.getLog() + message);
        productMilestoneReleaseRepository.save(release);
    }

    private String describeCompletedPush(MilestoneReleaseResultRest result) {
        boolean success = result.isSuccessful();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Brew push ").append(success ? "SUCCEEDED" : "FAILED").append("\n");
        stringBuilder.append("Import details:\n");

        String errorMessage = result.getErrorMessage();
        if (errorMessage != null) {
            stringBuilder.append(errorMessage).append("\n");
        }
        for (BuildImportResultRest buildImport : result.getBuilds()) {
            describeBuildImport(stringBuilder, buildImport);
        }

        return stringBuilder.toString();
    }

    private void describeBuildImport(StringBuilder stringBuilder, BuildImportResultRest buildImport) {
        Integer buildRecordId = buildImport.getBuildRecordId();
        BuildRecord record = orNull(buildRecordId, buildRecordRepository::queryById);
        BuildConfigurationAudited buildConfiguration = orNull(record, BuildRecord::getBuildConfigurationAudited); // TODO
                                                                                                                  // fix
                                                                                                                  // audited
                                                                                                                  // entity
                                                                                                                  // usage
        stringBuilder.append("\n-------------------------------------------------------------------------\n");
        String buildMessage = String.format(
                "%s [buildRecordId: %d, built from %s revision %s tag %s] import %s. Brew build id: %d, Brew build url: %s\n",
                orNull(buildConfiguration, BuildConfigurationAudited::getName),
                orNull(record, BuildRecord::getId),
                orNull(record, BuildRecord::getScmRepoURL),
                orNull(record, BuildRecord::getScmRevision),
                orNull(record, BuildRecord::getScmTag),
                buildImport.getStatus(),
                buildImport.getBrewBuildId(),
                buildImport.getBrewBuildUrl());
        stringBuilder.append(buildMessage);
        if (buildImport.getStatus() != BuildImportStatus.SUCCESSFUL) {
            stringBuilder.append("Error message: ").append(buildImport.getErrorMessage());
            List<ArtifactImportError> errors = buildImport.getErrors();
            if (errors != null && !errors.isEmpty()) {
                errors.forEach(e -> describeArtifactImportError(stringBuilder, e));
            }
        }
        stringBuilder.append("\n");
    }

    private void describeArtifactImportError(StringBuilder stringBuilder, ArtifactImportError e) {
        String artifactId = e.getArtifactId();
        Artifact artifact = artifactRepository.queryById(Integer.valueOf(artifactId));

        stringBuilder.append(
                String.format(
                        "Failed to import %s [artifactId:%s]. Error message: %s\n",
                        orNull(artifact, Artifact::getIdentifier),
                        artifactId,
                        e.getErrorMessage()));
    }

    /**
     * [NCL-3112] Mark the milestone provided as not current
     *
     * @param milestone ProductMilestone to not be current anymore
     */
    private void removeCurrentFlagFromMilestone(ProductMilestone milestone) {
        ProductVersion productVersion = milestone.getProductVersion();

        if (productVersion.getCurrentProductMilestone() != null
                && productVersion.getCurrentProductMilestone().getId().equals(milestone.getId())) {

            productVersion.setCurrentProductMilestone(null);
            productVersionRepository.save(productVersion);
        }
    }

    private static <T, R> R orNull(T value, Function<T, R> f) {
        return value == null ? null : f.apply(value);
    }

}
