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
package org.jboss.pnc.coordinator.builder.datastore;

import org.jboss.pnc.coordinator.BuildCoordinationException;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.enums.BuildStatus.CANCELLED;
import static org.jboss.pnc.enums.BuildStatus.FAILED;
import static org.jboss.pnc.enums.BuildStatus.SYSTEM_ERROR;
import static org.jboss.pnc.enums.BuildStatus.NEW;

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

    public BuildRecord storeResult(BuildTask buildTask, BuildResult buildResult) throws DatastoreException {
        try {
            BuildStatus buildRecordStatus = NEW;

            BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);
            buildRecordBuilder.buildContentId(buildTask.getContentId());

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
                log.warn("[BuildTask:" + buildTask.getId() + "] Missing RepourResult.");
            }

            if (buildResult.getBuildDriverResult().isPresent()) {
                BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult().get();
                buildRecordBuilder.appendLog(buildDriverResult.getBuildLog());
                buildDriverResult.getOutputChecksum().ifPresent(buildRecordBuilder::buildOutputChecksum);
                buildRecordStatus = buildDriverResult.getBuildStatus(); // TODO buildRecord should use CompletionStatus
            } else if (!buildResult.hasFailed()) {
                return storeResult(
                        buildTask,
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

                if (environmentDriverResult.getCompletionStatus() != null
                        && environmentDriverResult.getCompletionStatus().isFailed()) {
                    buildRecordStatus = FAILED;
                }
            }

            List<Artifact> builtArtifacts = Collections.emptyList();
            List<Artifact> dependencies = Collections.emptyList();
            if (buildResult.getRepositoryManagerResult().isPresent()) {
                RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult().get();

                buildRecordBuilder.appendLog(repositoryManagerResult.getLog());
                if (repositoryManagerResult.getCompletionStatus().isFailed()) {
                    buildRecordStatus = FAILED; // TODO, do not mix statuses
                }

                builtArtifacts = repositoryManagerResult.getBuiltArtifacts();
                Map<Artifact, String> builtConflicts = datastore.checkForBuiltArtifacts(builtArtifacts);
                if (builtConflicts.size() > 0) {
                    return storeResult(
                            buildTask,
                            Optional.of(buildResult),
                            new BuildCoordinationException(
                                    "Trying to store success build with invalid repository manager result. Conflicting artifact data found: "
                                            + builtConflicts.toString()));
                }

                dependencies = repositoryManagerResult.getDependencies();
            } else if (!buildResult.hasFailed()) {
                return storeResult(
                        buildTask,
                        Optional.of(buildResult),
                        new BuildCoordinationException(
                                "Trying to store success build with incomplete result. Missing RepositoryManagerResult."));
            }

            if (NEW.equals(buildRecordStatus)) {
                if (buildResult.getCompletionStatus().equals(CompletionStatus.CANCELLED)) {
                    buildRecordStatus = CANCELLED;
                } else if (buildResult.getCompletionStatus().equals(CompletionStatus.TIMED_OUT)) {
                    buildRecordStatus = SYSTEM_ERROR;
                    buildRecordBuilder.appendLog("-- Operation TIMED-OUT --");
                    userLog.warn("Operation TIMED-OUT.");
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
                        buildTask,
                        Optional.of(buildResult),
                        new BuildCoordinationException(
                                "Trying to store success build with incomplete result. Missing BuildExecutionConfiguration."));
            }

            log.debug("Storing results of buildTask [{}] to datastore.", buildTask.getId());
            userLog.info("Successfully completed.");
            return datastore.storeCompletedBuild(buildRecordBuilder, builtArtifacts, dependencies);
        } catch (Exception e) {
            return storeResult(buildTask, Optional.of(buildResult), e);
        }
    }

    public BuildRecord storeRecordForNoRebuild(BuildTask buildTask) throws DatastoreException {
        try {
            log.debug("Storing record for non required rebuild of buildTask [{}] to datastore.", buildTask.getId());
            BuildRecord buildRecord = initBuildRecordBuilder(buildTask).status(BuildStatus.NO_REBUILD_REQUIRED)
                    .appendLog("No rebuild was required.")
                    .buildContentId(buildTask.getContentId())
                    .build();
            BuildRecord storedRecord = datastore.storeRecordForNoRebuild(buildRecord);
            userLog.info("Successfully completed.");
            return storedRecord;
        } catch (Exception e) {
            return storeResult(buildTask, Optional.empty(), e);
        }
    }

    /**
     * Store build result along with error information appended to the build log
     *
     * @param buildTask task
     * @param buildResult result of running the task
     * @param e The error that occurred during the build process
     * @throws DatastoreException on failure to store data
     */
    public BuildRecord storeResult(BuildTask buildTask, Optional<BuildResult> buildResult, Throwable e)
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
        log.debug(
                "Storing ERROR result of " + buildTask.getBuildConfigurationAudited().getName() + " to datastore.",
                e);
        return datastore.storeCompletedBuild(buildRecordBuilder, Collections.emptyList(), Collections.emptyList());
    }

    private CompletionStatus getBuildStatus(Optional<BuildResult> buildResult) {
        if (buildResult.isPresent()) {
            return buildResult.get().getCompletionStatus();
        } else {
            return null;
        }
    }

    public void storeRejected(BuildTask buildTask) throws DatastoreException {
        BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);
        buildRecordBuilder.status(BuildStatus.fromBuildCoordinationStatus(buildTask.getStatus()));
        buildRecordBuilder.buildLog(buildTask.getStatusDescription());

        userLog.warn(buildTask.getStatusDescription());

        log.debug(
                "Storing REJECTED build of {} to datastore. Reason: {}",
                buildTask.getBuildConfigurationAudited().getName(),
                buildTask.getStatusDescription());
        datastore.storeCompletedBuild(buildRecordBuilder, Collections.emptyList(), Collections.emptyList());
    }

    /**
     * Initialize a new BuildRecord.Builder based on the data contained in the BuildTask. Note, this must be done inside
     * a transaction because it fetches the BuildRecordSet entities from the database.
     *
     * @return The initialized build record builder
     */
    private BuildRecord.Builder initBuildRecordBuilder(BuildTask buildTask) {
        BuildOptions buildOptions = buildTask.getBuildOptions();
        BuildRecord.Builder builder = BuildRecord.Builder.newBuilder()
                .id(buildTask.getId())
                .buildConfigurationAudited(buildTask.getBuildConfigurationAudited())
                .user(buildTask.getUser())
                .submitTime(buildTask.getSubmitTime())
                .startTime(buildTask.getStartTime())
                .productMilestone(buildTask.getProductMilestone())
                .temporaryBuild(buildOptions.isTemporaryBuild())
                .noRebuildCause(buildTask.getNoRebuildCause());

        if (buildTask.getEndTime() == null) {
            buildTask.setEndTime(Date.from(Instant.now()));
        }

        builder.endTime(buildTask.getEndTime());

        if (buildTask.getBuildConfigSetRecordId() != null) {
            BuildConfigSetRecord buildConfigSetRecord = datastore
                    .getBuildConfigSetRecordById(buildTask.getBuildConfigSetRecordId());
            builder.buildConfigSetRecord(buildConfigSetRecord);
        }

        List<Base32LongID> dependencies = buildTask.getDependencies()
                .stream()
                .map(BuildTask::getId)
                .map(Base32LongID::new)
                .collect(Collectors.toList());
        builder.dependencyBuildRecordIds(dependencies.toArray(new Base32LongID[dependencies.size()]));

        List<Base32LongID> dependants = buildTask.getDependants()
                .stream()
                .map(BuildTask::getId)
                .map(Base32LongID::new)
                .collect(Collectors.toList());
        builder.dependentBuildRecordIds(dependants.toArray(new Base32LongID[dependants.size()]));

        return builder;
    }

    public boolean requiresRebuild(
            BuildConfigurationAudited buildConfigurationAudited,
            boolean checkImplicitDependencies,
            boolean temporaryBuild,
            Set<Integer> processedDependenciesCache) {
        return datastore.requiresRebuild(
                buildConfigurationAudited,
                checkImplicitDependencies,
                temporaryBuild,
                processedDependenciesCache);
    }

    public boolean requiresRebuild(BuildTask task, Set<Integer> processedDependenciesCache) {
        return datastore.requiresRebuild(
                task.getBuildConfigurationAudited(),
                task.getBuildOptions().isImplicitDependenciesCheck(),
                task.getBuildOptions().isTemporaryBuild(),
                processedDependenciesCache,
                task::setNoRebuildCause);
    }

    public Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet) {
        return datastore.getBuildConfigurations(buildConfigurationSet);
    }

    public BuildConfigSetRecord getBuildCongigSetRecordById(Integer buildConfigSetRecordId) {
        return datastore.getBuildConfigSetRecordById(buildConfigSetRecordId);
    }
}
