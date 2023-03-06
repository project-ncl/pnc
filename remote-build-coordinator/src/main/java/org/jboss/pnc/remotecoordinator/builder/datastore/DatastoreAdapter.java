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
package org.jboss.pnc.remotecoordinator.builder.datastore;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.remotecoordinator.BuildCoordinationException;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.coordinator.BuildTaskRef;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repour.RepourResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.jboss.pnc.enums.BuildStatus.CANCELLED;
import static org.jboss.pnc.enums.BuildStatus.FAILED;
import static org.jboss.pnc.enums.BuildStatus.NEW;
import static org.jboss.pnc.enums.BuildStatus.SYSTEM_ERROR;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-15.
 */
@Dependent
public class DatastoreAdapter {

    private Datastore datastore;

    private static final Logger log = LoggerFactory.getLogger(DatastoreAdapter.class);
    private static final Logger userLog = LoggerFactory.getLogger("org.jboss.pnc._userlog_.build-result");

    // needed for EJB/CDI
    @Deprecated
    public DatastoreAdapter() {
    }

    @Inject
    public DatastoreAdapter(Datastore datastore) {
        this.datastore = datastore;
    }

    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord)
            throws DatastoreException {
        return datastore.saveBuildConfigSetRecord(buildConfigSetRecord);
    }

    /**
     * Get the latest audited revision for the given build configuration ID
     *
     * @param buildConfigurationId Build Configuration ID
     * @return The latest revision of the given build configuration
     */
    public BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigurationId) {
        BuildConfigurationAudited buildConfigAudited = datastore
                .getLatestBuildConfigurationAudited(buildConfigurationId);
        loadBuildConfigurations(buildConfigAudited);
        return buildConfigAudited;
    }

    /**
     * Get the latest audited version of the given build configuration and fetch whole dependency tree of the related BC
     *
     * @param buildConfigurationId The id of the config to check
     * @return The latest audited version of the build configuration with fetched dependency tree of the related BC
     */
    public BuildConfigurationAudited getLatestBuildConfigurationAuditedInitializeBCDependencies(
            Integer buildConfigurationId) {
        BuildConfigurationAudited buildConfigAudited = datastore
                .getLatestBuildConfigurationAuditedLoadBCDependencies(buildConfigurationId);
        loadBuildConfigurations(buildConfigAudited);
        return buildConfigAudited;
    }

    /**
     * Fetch build configurations of project to be able access it outside transaction
     * 
     * @param buildConfigAudited build config for which the build configurations are to be fetched
     */
    private void loadBuildConfigurations(BuildConfigurationAudited buildConfigAudited) {
        Project project = buildConfigAudited.getProject();
        project.getBuildConfigurations().forEach(BuildConfiguration::getId);
    }

    public BuildRecord storeResult(BuildTaskRef buildTaskRef, BuildResult buildResult) throws DatastoreException {
        try {
            BuildStatus buildRecordStatus = NEW;

            BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTaskRef);

            if (buildResult.getRepourResult().isPresent()) {
                RepourResult repourResult = buildResult.getRepourResult().get();
                buildRecordBuilder.repourLog(repourResult.getLog());
                buildRecordBuilder.executionRootName(repourResult.getExecutionRootName());
                buildRecordBuilder.executionRootVersion(repourResult.getExecutionRootVersion());
                CompletionStatus repourCompletionStatus = repourResult.getCompletionStatus();
                if (repourCompletionStatus != null) {
                    switch (repourCompletionStatus) {
                        case SUCCESS:
                        case NO_REBUILD_REQUIRED:
                            break;
                        case FAILED:
                            buildRecordBuilder.appendLog(
                                    "\nBuild failed during the alignment phase, please check the 'Alignment Log' tab for more information.\n");
                            buildRecordStatus = FAILED;
                            break;
                        case CANCELLED:
                            buildRecordBuilder.appendLog("\nBuild cancelled during alignment phase.\n");
                            buildRecordStatus = CANCELLED;
                            break;
                        case TIMED_OUT:
                            buildRecordBuilder.appendLog("\nBuild timed-out during alignment phase.\n");
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                        case SYSTEM_ERROR:
                            buildRecordBuilder.appendLog(
                                    "\nBuild failed with SYSTEM_ERROR during the alignment phase, "
                                            + "please check the 'Alignment Log' tab for more information.\n");
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                        default:
                            buildRecordBuilder.appendLog(
                                    "\nInvalid status during the alignment phase, failing with SYSTEM_ERROR.\n");
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                    }
                }
            } else {
                userLog.warn("Missing Repour Result!");
                log.warn("[BuildTask:" + buildTaskRef.getId() + "] Missing RepourResult.");
            }

            if (buildResult.getBuildDriverResult().isPresent()) {
                BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult().get();
                buildRecordBuilder.appendLog(buildDriverResult.getBuildLog());
                buildDriverResult.getOutputChecksum().ifPresent(buildRecordBuilder::buildOutputChecksum);
                buildRecordStatus = buildDriverResult.getBuildStatus(); // TODO buildRecord should use CompletionStatus
            } else if (!buildResult.hasFailed()) {
                return storeResult(
                        buildTaskRef,
                        Optional.of(buildResult),
                        new BuildCoordinationException(
                                "Trying to store success build with incomplete result. Missing BuildDriverResult."));
            }

            if (buildResult.getEnvironmentDriverResult().isPresent()) {
                EnvironmentDriverResult environmentDriverResult = buildResult.getEnvironmentDriverResult().get();
                buildRecordBuilder.appendLog(environmentDriverResult.getLog());

                environmentDriverResult.getSshCredentials().ifPresent(c -> {
                    buildRecordBuilder.sshCommand(c.getCommand());
                    buildRecordBuilder.sshPassword(c.getPassword());
                });

                if (environmentDriverResult.getCompletionStatus() != null) {
                    switch (environmentDriverResult.getCompletionStatus()) {
                        case SUCCESS:
                        case NO_REBUILD_REQUIRED:
                            break;
                        case FAILED:
                            buildRecordStatus = FAILED;
                            break;
                        case CANCELLED:
                            buildRecordStatus = CANCELLED;
                            break;
                        case TIMED_OUT:
                        case SYSTEM_ERROR:
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                        default:
                            buildRecordBuilder.appendLog(
                                    "\nInvalid status during the environment setup phase, failing with SYSTEM_ERROR.\n");
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                    }
                }
            }

            List<Artifact> builtArtifacts = Collections.emptyList();
            List<Artifact> dependencies = Collections.emptyList();
            if (buildResult.getRepositoryManagerResult().isPresent()) {
                RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult().get();

                buildRecordBuilder.appendLog(repositoryManagerResult.getLog());
                if (repositoryManagerResult.getCompletionStatus() != null) {
                    switch (repositoryManagerResult.getCompletionStatus()) { // TODO, do not mix statuses
                        case SUCCESS:
                        case NO_REBUILD_REQUIRED:
                            break;
                        case FAILED:
                            buildRecordStatus = FAILED;
                            break;
                        case CANCELLED:
                            buildRecordStatus = CANCELLED;
                            break;
                        case TIMED_OUT:
                        case SYSTEM_ERROR:
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                        default:
                            buildRecordBuilder.appendLog(
                                    "\nInvalid status during the promotion phase, failing with SYSTEM_ERROR.\n");
                            buildRecordStatus = SYSTEM_ERROR;
                            break;
                    }
                }

                builtArtifacts = repositoryManagerResult.getBuiltArtifacts();
                if (buildTaskRef.isTemporaryBuild()) {
                    checkTemporaryArtifacts(builtArtifacts);
                }
                Map<Artifact, String> builtConflicts = datastore.checkForBuiltArtifacts(builtArtifacts);
                if (builtConflicts.size() > 0) {
                    return storeResult(
                            buildTaskRef,
                            Optional.of(buildResult),
                            new BuildCoordinationException(
                                    "Trying to store success build with invalid repository manager result. Conflicting artifact data found: "
                                            + builtConflicts.toString()));
                }

                dependencies = repositoryManagerResult.getDependencies();
            } else if (!buildResult.hasFailed()) {
                return storeResult(
                        buildTaskRef,
                        Optional.of(buildResult),
                        new BuildCoordinationException(
                                "Trying to store success build with incomplete result. Missing RepositoryManagerResult."));
            }

            if (NEW.equals(buildRecordStatus)) {
                switch (buildResult.getCompletionStatus()) {
                    case SUCCESS:
                    case NO_REBUILD_REQUIRED:
                    case FAILED:
                    case SYSTEM_ERROR:
                        break;
                    case CANCELLED:
                        buildRecordStatus = CANCELLED;
                        break;
                    case TIMED_OUT:
                        buildRecordStatus = SYSTEM_ERROR;
                        buildRecordBuilder.appendLog("-- Operation TIMED-OUT --");
                        userLog.warn("Operation TIMED-OUT.");
                        break;
                    default:
                        buildRecordBuilder.appendLog(
                                "\nInvalid status detected in the final completion status, failing with SYSTEM_ERROR.\n");
                        break;
                }
            }

            log.debug("Setting status " + buildRecordStatus.toString() + " to buildRecord.");
            buildRecordBuilder.status(buildRecordStatus);

            if (buildResult.getBuildExecutionConfiguration().isPresent()) {
                BuildExecutionConfiguration buildExecutionConfig = buildResult.getBuildExecutionConfiguration().get();
                buildRecordBuilder.scmRepoURL(buildExecutionConfig.getScmRepoURL());
                buildRecordBuilder.scmRevision(buildExecutionConfig.getScmRevision());
                buildRecordBuilder.scmTag(buildExecutionConfig.getScmTag());
            } else if (!buildResult.hasFailed()) {
                return storeResult(
                        buildTaskRef,
                        Optional.of(buildResult),
                        new BuildCoordinationException(
                                "Trying to store success build with incomplete result. Missing BuildExecutionConfiguration."));
            }

            log.debug("Storing results of buildTask [{}] to datastore.", buildTaskRef.getId());
            userLog.info("Successfully completed.");
            return datastore.storeCompletedBuild(buildRecordBuilder, builtArtifacts, dependencies);
        } catch (Exception e) {
            return storeResult(buildTaskRef, Optional.of(buildResult), e);
        }
    }

    public BuildRecord storeResult(BuildTaskRef buildTask, Optional<BuildResult> buildResult, Throwable e)
            throws DatastoreException {
        BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);
        buildRecordBuilder.status(SYSTEM_ERROR);

        StringBuilder errorLog = new StringBuilder();

        buildResult.ifPresent(result -> {

            result.getRepourResult().ifPresent(repourResult -> {
                buildRecordBuilder.executionRootName(repourResult.getExecutionRootName());
                buildRecordBuilder.executionRootVersion(repourResult.getExecutionRootVersion());
                buildRecordBuilder.repourLog(repourResult.getLog());
            });

            result.getBuildDriverResult().ifPresent(buildDriverResult -> {
                errorLog.append(buildDriverResult.getBuildLog());
                errorLog.append("\n---- End Build Log ----\n");
            });

            result.getRepositoryManagerResult().ifPresent(rmr -> {
                errorLog.append(rmr.getLog());
                errorLog.append("\n---- End Repository Manager Log ----\n");
                errorLog.append("\n---- Start Built Artifacts List ----\n");
                rmr.getBuiltArtifacts().forEach(b -> errorLog.append(b).append('\n'));
                errorLog.append("\n---- End Built Artifacts List ----\n");
            });

            result.getEnvironmentDriverResult().ifPresent(r -> {
                if (r.getLog() != null && !r.getLog().equals(""))
                    errorLog.append(r.getLog());
                errorLog.append("\n---- End Environment Driver Log ----\n");
            });

            // store scm information of failed build if present
            result.getBuildExecutionConfiguration().ifPresent(r -> {
                buildRecordBuilder.scmRepoURL(r.getScmRepoURL());
                buildRecordBuilder.scmRevision(r.getScmRevision());
                buildRecordBuilder.scmTag(r.getScmTag());

            });
        });

        errorLog.append("Build status: ").append(getBuildStatus(buildResult)).append("\n");
        errorLog.append("Caught exception: ").append(e.toString()).append("\n");
        StringWriter stackTraceWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTraceWriter));
        errorLog.append(stackTraceWriter.getBuffer());
        buildRecordBuilder.buildLog(errorLog.toString());

        userLog.error("Build status: {}.", getBuildStatus(buildResult));
        log.debug("Storing ERROR result of BCA: " + buildTask.getIdRev().toString() + " to datastore.", e);
        return datastore.storeCompletedBuild(buildRecordBuilder, Collections.emptyList(), Collections.emptyList());
    }

    public BuildRecord storeRecordForNoRebuild(BuildTaskRef buildTask) throws DatastoreException {
        try {
            log.debug("Storing record for non required rebuild of buildTask [{}] to datastore.", buildTask.getId());

            BuildRecord buildRecord = initBuildRecordBuilder(buildTask).status(BuildStatus.NO_REBUILD_REQUIRED)
                    .appendLog("No rebuild was required.")
                    .build();

            BuildRecord storedRecord = datastore.storeRecordForNoRebuild(buildRecord);
            userLog.info("Successfully completed.");
            return storedRecord;
        } catch (Exception e) {
            return storeResult(buildTask, Optional.empty(), e);
        }
    }

    /**
     * Check if the artifact have correctly set the TEMPORARY quality, if not fix it and log as error.
     */
    private void checkTemporaryArtifacts(List<Artifact> artifacts) {
        List<Artifact> badArtifacts = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            if (artifact.getArtifactQuality() != ArtifactQuality.TEMPORARY) {
                artifact.setArtifactQuality(ArtifactQuality.TEMPORARY);
                badArtifacts.add(artifact);
            }
        }
        if (!badArtifacts.isEmpty()) {
            log.error("Temporary build produced artifact without TEMPORARY quality. Fixed artifacts: " + artifacts);
        }
    }

    private CompletionStatus getBuildStatus(Optional<BuildResult> buildResult) {
        return buildResult.map(BuildResult::getCompletionStatus).orElse(null);
    }

    public void storeRejected(BuildTaskRef buildTask, String statusDescription) throws DatastoreException {
        BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);

        buildRecordBuilder.status(BuildStatus.fromBuildCoordinationStatus(buildTask.getStatus()));
        buildRecordBuilder.buildLog(statusDescription);

        userLog.warn(statusDescription);

        log.debug(
                "Storing REJECTED build of {} to datastore. Reason: {}",
                datastore.getBuildConfigurationAudited(buildTask.getIdRev()).getName(), // TODO Print just IdRev or
                                                                                        // keep?
                statusDescription);
        datastore.storeCompletedBuild(buildRecordBuilder, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Initialize a new BuildRecord.Builder based on the data contained in the BuildTask. Note, this must be done inside
     * a transaction because it fetches the BuildRecordSet entities from the database.
     *
     * @return The initialized build record builder
     */

    private BuildRecord.Builder initBuildRecordBuilder(BuildTaskRef buildTask) {
        BuildRecord.Builder builder = BuildRecord.Builder.newBuilder()
                .id(buildTask.getId())
                .buildConfigurationAudited(datastore.getBuildConfigurationAudited(buildTask.getIdRev()))
                .user(buildTask.getUser())
                .status(BuildStatus.fromBuildCoordinationStatus(buildTask.getStatus()))
                .buildContentId(buildTask.getContentId())
                .submitTime(Date.from(buildTask.getSubmitTime()))
                .productMilestone(buildTask.getProductMilestone())
                .temporaryBuild(buildTask.isTemporaryBuild())
                .alignmentPreference(buildTask.getAlignmentPreference())
                .noRebuildCause(buildTask.getNoRebuildCause());

        if (buildTask.getStartTime() != null) {
            builder.startTime(Date.from(buildTask.getStartTime()));
        }

        if (buildTask.getEndTime() != null) {
            builder.endTime(Date.from(buildTask.getEndTime()));
        } else {
            builder.endTime(Date.from(Instant.now()));
        }

        if (buildTask.getBuildConfigSetRecord() != null) {
            builder.buildConfigSetRecord(buildTask.getBuildConfigSetRecord());
        }

        builder.dependencyBuildRecordIds(
                buildTask.getDependencies().stream().map(Base32LongID::new).toArray(Base32LongID[]::new));

        builder.dependentBuildRecordIds(
                buildTask.getDependants().stream().map(Base32LongID::new).toArray(Base32LongID[]::new));

        return builder;
    }

    /**
     * @return cause for no rebuild or Empty if a rebuild is required.
     */
    public Optional<BuildRecord> requiresRebuild(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference,
            Set<Integer> processedDependenciesCache) {

        ObjectWrapper<BuildRecord> rebuildCause = new ObjectWrapper<>();
        datastore.requiresRebuild(
                buildConfigurationAudited,
                checkImplicitDependencies,
                temporaryBuild,
                alignmentPreference,
                processedDependenciesCache,
                rebuildCause::set);
        return Optional.ofNullable(rebuildCause.get());
    }

    @Deprecated
    public boolean requiresRebuild(BuildTask task, Set<Integer> processedDependenciesCache) {
        return datastore.requiresRebuild(
                task.getBuildConfigurationAudited(),
                task.getBuildOptions().isImplicitDependenciesCheck(),
                task.getBuildOptions().isTemporaryBuild(),
                task.getBuildOptions().getAlignmentPreference(),
                processedDependenciesCache,
                task::setNoRebuildCause);
    }

    public Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet) {
        return datastore.getBuildConfigurations(buildConfigurationSet);
    }

    public BuildConfigSetRecord getBuildCongigSetRecordById(Long buildConfigSetRecordId) {
        return datastore.getBuildConfigSetRecordById(buildConfigSetRecordId);
    }
}
