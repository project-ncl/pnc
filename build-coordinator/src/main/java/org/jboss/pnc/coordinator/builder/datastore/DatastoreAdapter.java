/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.model.BuildStatus.CANCELLED;
import static org.jboss.pnc.model.BuildStatus.FAILED;
import static org.jboss.pnc.model.BuildStatus.REJECTED;
import static org.jboss.pnc.model.BuildStatus.SYSTEM_ERROR;
import static org.jboss.pnc.model.BuildStatus.UNKNOWN;

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

    public BuildConfigSetRecord saveBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) throws DatastoreException {
        return datastore.saveBuildConfigSetRecord(buildConfigSetRecord);
    }

    /**
     * Get the latest audited revision for the given build configuration ID
     *
     * @param buildConfigurationId
     * @return The latest revision of the given build configuration
     */
    public BuildConfigurationAudited getLatestBuildConfigurationAudited(Integer buildConfigurationId) {
        BuildConfigurationAudited buildConfigAudited = datastore.getLatestBuildConfigurationAudited(buildConfigurationId);
        loadBuildConfigurations(buildConfigAudited);
        return buildConfigAudited;
    }

    /**
     * Fetch build configurations of project to be able access it outside transaction
     * @param buildConfigAudited build config for which the build configurations are to be fetched
     */
    private void loadBuildConfigurations(BuildConfigurationAudited buildConfigAudited) {
        Project project = buildConfigAudited.getProject();
        project.getBuildConfigurations().forEach(BuildConfiguration::getId);
    }

    public void storeResult(BuildTask buildTask, BuildResult buildResult) throws DatastoreException {
        try {
            BuildStatus buildRecordStatus = UNKNOWN;

            BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);

            if (buildResult.getRepourResult().isPresent()) {
                RepourResult repourResult = buildResult.getRepourResult().get();
                buildRecordBuilder.repourLog(repourResult.getLog());
                buildRecordBuilder.executionRootName(repourResult.getExecutionRootName());
                buildRecordBuilder.executionRootVersion(repourResult.getExecutionRootVersion());
            } else {
                userLog.warn("Missing Repour Result!");
                log.warn("[BuildTask:" + buildTask.getId() + "] Missing RepourResult.");
            }

            if (buildResult.getBuildDriverResult().isPresent()) {
                BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult().get();
                buildRecordBuilder.appendLog(buildDriverResult.getBuildLog());
                buildRecordStatus = buildDriverResult.getBuildStatus(); //TODO buildRecord should use CompletionStatus
            } else if (!buildResult.hasFailed()) {
                storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with incomplete result. Missing BuildDriverResult."));
                return;
            }

            if (buildResult.getEnvironmentDriverResult().isPresent()) {
                EnvironmentDriverResult environmentDriverResult = buildResult.getEnvironmentDriverResult().get();
                buildRecordBuilder.appendLog(environmentDriverResult.getLog());

                environmentDriverResult.getSshCredentials().ifPresent(c -> {
                    buildRecordBuilder.sshCommand(c.getCommand());
                    buildRecordBuilder.sshPassword(c.getPassword());
                });
            }

            if (buildResult.getRepositoryManagerResult().isPresent()) {
                RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult().get();

                buildRecordBuilder.appendLog(repositoryManagerResult.getLog());
                if (repositoryManagerResult.getCompletionStatus().isFailed()) {
                    buildRecordStatus = FAILED; //TODO, do not mix statuses
                }

                buildRecordBuilder.buildContentId(repositoryManagerResult.getBuildContentId());

                Collection<Artifact> builtArtifacts = repositoryManagerResult.getBuiltArtifacts();
                Map<Artifact, String> builtConflicts = datastore.checkForConflictingArtifacts(builtArtifacts);
                if (builtConflicts.size() > 0) {
                    storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with invalid repository manager result. Conflicting artifact data found: " + builtConflicts.toString()));
                    return;
                }
                buildRecordBuilder.builtArtifacts(repositoryManagerResult.getBuiltArtifacts());

                Map<Artifact, String> depConflicts = datastore.checkForConflictingArtifacts(builtArtifacts);
                if (depConflicts.size() > 0) {
                    storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with invalid repository manager result. Conflicting artifact data found: " + depConflicts.toString()));
                    return;
                }
                buildRecordBuilder.dependencies(repositoryManagerResult.getDependencies());
            } else if (!buildResult.hasFailed()) {
                storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with incomplete result. Missing RepositoryManagerResult."));
                return;
            }

            if (UNKNOWN.equals(buildRecordStatus)) {
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
            } else if (!buildResult.hasFailed()) {
                storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with incomplete result. Missing BuildExecutionConfiguration."));
                return;
            }

            log.debug("Storing results of buildTask [{}] to datastore.", buildTask.getId());
            datastore.storeCompletedBuild(buildRecordBuilder);
            userLog.info("Successfully completed.");
        } catch (Exception e) {
            storeResult(buildTask, Optional.of(buildResult), e);
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
    public void storeResult(BuildTask buildTask, Optional<BuildResult> buildResult, Throwable e) throws DatastoreException {
        BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);
        buildRecordBuilder.status(SYSTEM_ERROR);

        StringBuilder errorLog = new StringBuilder();

        buildResult.ifPresent(result -> {

            result.getRepourResult().ifPresent(repourResult -> {
                buildRecordBuilder.executionRootName(repourResult.getExecutionRootName());
                buildRecordBuilder.executionRootVersion(repourResult.getExecutionRootVersion());
                buildRecordBuilder.repourLog(repourResult.getLog());
            });

            result.getBuildDriverResult().ifPresent(
                buildDriverResult -> {
                    errorLog.append(buildDriverResult.getBuildLog());
                    errorLog.append("\n---- End Build Log ----\n");
            });

            result.getRepositoryManagerResult().ifPresent(
                rmr -> {
                    errorLog.append(rmr.getLog());
                    errorLog.append("\n---- End Repository Manager Log ----\n");
            });

            result.getEnvironmentDriverResult().ifPresent(
                r -> {
                    if (r.getLog() != null && !r.getLog().equals(""))
                    errorLog.append(r.getLog());
                    errorLog.append("\n---- End Environment Driver Log ----\n");
            });
        });

        errorLog.append("Build status: ").append(getBuildStatus(buildResult)).append("\n");
        errorLog.append("Caught exception: ").append(e.toString()).append("\n");
        StringWriter stackTraceWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTraceWriter));
        errorLog.append(stackTraceWriter.getBuffer());
        buildRecordBuilder.buildLog(errorLog.toString());

        userLog.error("Build status: {}.", getBuildStatus(buildResult));
        log.debug("Storing ERROR result of buildTask.getBuildConfigurationAudited().getName() to datastore.",  e);
        datastore.storeCompletedBuild(buildRecordBuilder);
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
        buildRecordBuilder.status(REJECTED);
        buildRecordBuilder.buildLog(buildTask.getStatusDescription());

        userLog.warn(buildTask.getStatusDescription());

        log.debug("Storing REJECTED build of {} to datastore. Reason: {}", buildTask.getBuildConfigurationAudited().getName(), buildTask.getStatusDescription());
        datastore.storeCompletedBuild(buildRecordBuilder);
    }


    /**
     * Initialize a new BuildRecord.Builder based on the data contained in the BuildTask.
     * Note, this must be done inside a transaction because it fetches the BuildRecordSet entities from
     * the database.
     *
     * @return The initialized build record builder
     */
    private BuildRecord.Builder initBuildRecordBuilder(BuildTask buildTask) {
        BuildOptions buildOptions = buildTask.getBuildOptions();
        BuildRecord.Builder builder = BuildRecord.Builder.newBuilder().id(buildTask.getId())
                .buildConfigurationAudited(buildTask.getBuildConfigurationAudited())
                .user(buildTask.getUser())
                .submitTime(buildTask.getSubmitTime())
                .startTime(buildTask.getStartTime())
                .productMilestone(buildTask.getProductMilestone())
                .temporaryBuild(buildOptions.isTemporaryBuild());

        if (buildTask.getEndTime() == null) {
            buildTask.setEndTime(Date.from(Instant.now()));
        }

        builder.endTime(buildTask.getEndTime());

        if (buildTask.getBuildConfigSetRecordId() != null) {
            BuildConfigSetRecord buildConfigSetRecord = datastore.getBuildConfigSetRecordById(buildTask.getBuildConfigSetRecordId());
            builder.buildConfigSetRecord(buildConfigSetRecord);
        }

        List<Integer> dependencies = buildTask.getDependencies().stream().map(t -> t.getId()).collect(Collectors.toList());
        builder.dependencyBuildRecordIds(dependencies.toArray(new Integer[dependencies.size()]));

        List<Integer> dependants = buildTask.getDependants().stream().map(t -> t.getId()).collect(Collectors.toList());
        builder.dependentBuildRecordIds(dependants.toArray(new Integer[dependants.size()]));

        return builder;
    }

    public Integer getNextBuildRecordId() {
        return datastore.getNextBuildRecordId();
    }

    public boolean requiresRebuild(BuildConfiguration configuration) {
        return datastore.requiresRebuild(configuration);
    }

    public boolean requiresRebuild(BuildTask task) {
        return datastore.requiresRebuild(task);
    }

    public Set<BuildConfiguration> getBuildConfigurations(BuildConfigurationSet buildConfigurationSet) {
        return datastore.getBuildConfigurations(buildConfigurationSet);
    }

    public BuildConfigSetRecord getBuildCongigSetRecordById(Integer buildConfigSetRecordId) {
        return datastore.getBuildConfigSetRecordById(buildConfigSetRecordId);
    }
}
