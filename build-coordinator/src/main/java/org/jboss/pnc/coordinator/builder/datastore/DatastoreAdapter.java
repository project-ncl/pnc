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

import org.jboss.logging.Logger;
import org.jboss.pnc.coordinator.BuildCoordinationException;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildExecutionStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.jboss.pnc.model.BuildStatus.FAILED;
import static org.jboss.pnc.model.BuildStatus.REJECTED;
import static org.jboss.pnc.model.BuildStatus.SYSTEM_ERROR;
import static org.jboss.pnc.model.BuildStatus.UNKNOWN;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-15.
 */
public class DatastoreAdapter {

    private Datastore datastore;

    private static final Logger log = Logger.getLogger(DatastoreAdapter.class);

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
        buildConfigAudited.getProject().getBuildConfigurations().forEach(BuildConfiguration::getId);
    }

    public void storeResult(BuildTask buildTask, BuildResult buildResult) throws DatastoreException {
        try {
            BuildStatus buildRecordStatus = UNKNOWN;

            BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);

            buildResult.getExecutionRootName().ifPresent(buildRecordBuilder::executionRootName);
            buildResult.getExecutionRootVersion().ifPresent(buildRecordBuilder::executionRootVersion);

            buildResult.getSshCredentials().ifPresent(
                    c -> {
                        buildRecordBuilder.sshCommand(c.getCommand());
                        buildRecordBuilder.sshPassword(c.getPassword());
                    }
            );

            if (buildResult.getBuildDriverResult().isPresent()) {
                BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult().get();
                buildRecordBuilder.appendLog(buildDriverResult.getBuildLog());
                buildRecordStatus = buildDriverResult.getBuildStatus();
            } else if (!buildResult.hasFailed()) {
                storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with incomplete result. Missing BuildDriverResult."));
                return;
            }

            if (buildResult.getRepositoryManagerResult().isPresent()) {
                RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult().get();

                buildRecordBuilder.appendLog(repositoryManagerResult.getLog());
                if (repositoryManagerResult.getStatus().hasFailed()) {
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

            buildRecordBuilder.status(buildRecordStatus);


            if (buildResult.getBuildExecutionConfiguration().isPresent()) {
                BuildExecutionConfiguration buildExecutionConfig = buildResult.getBuildExecutionConfiguration().get();
                buildRecordBuilder.scmRepoURL(buildExecutionConfig.getScmRepoURL());
                buildRecordBuilder.scmRevision(buildExecutionConfig.getScmRevision());
            } else if (!buildResult.hasFailed()) {
                storeResult(buildTask, Optional.of(buildResult), new BuildCoordinationException("Trying to store success build with incomplete result. Missing BuildExecutionConfiguration."));
                return;
            }

            log.debugf("Storing results of buildTask [%s] to datastore.", buildTask.getId());
            datastore.storeCompletedBuild(buildRecordBuilder);
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

        buildResult.ifPresent(r -> {

            r.getExecutionRootName().ifPresent(buildRecordBuilder::executionRootName);
            r.getExecutionRootVersion().ifPresent(buildRecordBuilder::executionRootVersion);

            r.getBuildDriverResult().ifPresent(
                buildDriverResult -> {
                    errorLog.append(buildDriverResult.getBuildLog());
                    errorLog.append("\n---- End Build Log ----\n");
            });
            r.getRepositoryManagerResult().ifPresent(
                rmr -> {
                    errorLog.append(rmr.getLog());
                    errorLog.append("\n---- End Repository Manager Log ----\n");
            });
        });

        errorLog.append("Last build status: ").append(getLastBuildStatus(buildResult)).append("\n");
        errorLog.append("Caught exception: ").append(e.toString()).append("\n");
        StringWriter stackTraceWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTraceWriter));
        errorLog.append(stackTraceWriter.getBuffer());
        buildRecordBuilder.buildLog(errorLog.toString());

        log.debugf("Storing ERROR result of %s to datastore. Error: %s", buildTask.getBuildConfigurationAudited().getName() + "\n\n\n Exception: " + errorLog, e);
        datastore.storeCompletedBuild(buildRecordBuilder);
    }

    private BuildExecutionStatus getLastBuildStatus(Optional<BuildResult> buildResult) {
        Optional<BuildExecutionStatus> status = buildResult.flatMap(BuildResult::getFailedReasonStatus);

        return status.orElse(null);
    }

    public void storeRejected(BuildTask buildTask) throws DatastoreException {
        BuildRecord.Builder buildRecordBuilder = initBuildRecordBuilder(buildTask);
        buildRecordBuilder.status(REJECTED);
        buildRecordBuilder.buildLog(buildTask.getStatusDescription());

        log.debugf("Storing REJECTED build of %s to datastore. Reason: %s", buildTask.getBuildConfigurationAudited().getName(), buildTask.getStatusDescription());
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
        BuildRecord.Builder builder = BuildRecord.Builder.newBuilder().id(buildTask.getId())
                .buildConfigurationAudited(buildTask.getBuildConfigurationAudited())
                .user(buildTask.getUser())
                .submitTime(buildTask.getSubmitTime())
                .startTime(buildTask.getStartTime())
                .productMilestone(buildTask.getProductMilestone());

        if (buildTask.getEndTime() != null) {
            builder.endTime(buildTask.getEndTime());
        } else {
            builder.endTime(Date.from(Instant.now()));
        }

        if (buildTask.getEndTime() == null) {
            builder.endTime(Date.from(Instant.now()));
        } else {
            builder.endTime(buildTask.getEndTime());
        }
        
        builder.latestBuildConfiguration(buildTask.getBuildConfiguration());
        if (buildTask.getBuildConfigSetRecordId() != null) {
            BuildConfigSetRecord buildConfigSetRecord = datastore.getBuildConfigSetRecordById(buildTask.getBuildConfigSetRecordId());
            builder.buildConfigSetRecord(buildConfigSetRecord);
        }

        return builder;
    }

    public <T> T runInTransactionWithBuildConfig(Integer buildConfigurationId, Function<BuildConfiguration, T> job) {
        return datastore.runInTransactionWithBuildConfig(buildConfigurationId, job);
    }

    public Integer getNextBuildRecordId() {
        return datastore.getNextBuildRecordId();
    }

    public boolean hasSuccessfulBuildRecord(BuildConfiguration buildConfiguration) {
        return datastore.hasSuccessfulBuildRecord(buildConfiguration);
    }
}
