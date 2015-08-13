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
package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.BuildExecutionType;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.jboss.pnc.model.BuildStatus.SYSTEM_ERROR;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-15.
 */
public class DatastoreAdapter {

    private Datastore datastore;

    private static final Logger log = Logger.getLogger(DatastoreAdapter.class);

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
        return datastore.getLatestBuildConfigurationAudited(buildConfigurationId);
    }

    public BuildRecord storeResult(BuildTask buildTask, BuildResult buildResult, int buildRecordId) throws DatastoreException {
        try {
            BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult();
            RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult();

            BuildRecord buildRecord = buildTask.getBuildRecord();
            // Build driver results
            buildRecord.setBuildLog(buildDriverResult.getBuildLog());
            buildRecord.setStatus(buildDriverResult.getBuildDriverStatus().toBuildStatus());

            // Repository manager results, it's null in case of failed build
            if (repositoryManagerResult != null) {
                linkArtifactsWithBuildRecord(repositoryManagerResult.getBuiltArtifacts(), buildRecord);
                buildRecord.setBuiltArtifacts(repositoryManagerResult.getBuiltArtifacts());
                linkArtifactsWithBuildRecord(repositoryManagerResult.getDependencies(), buildRecord);
                buildRecord.setDependencies(repositoryManagerResult.getDependencies());
            }
            if (buildTask.getBuildExecutionType().equals(BuildExecutionType.COMPOSED_BUILD)) {
                buildRecord.setBuildConfigSetRecord(buildTask.getBuildSetTask().getBuildConfigSetRecord());
            }
            buildRecord.setEndTime(Date.from(Instant.now()));

            log.debugf("Storing results of %s to datastore.", buildTask.getBuildConfigurationAudited().getName());
            return datastore.storeCompletedBuild(buildRecord);
        } catch (Exception e) {
            throw new DatastoreException("Error storing the result to datastore.", e);
        }
    }

    public void storeResult(BuildTask buildTask, Throwable e) throws DatastoreException {
        BuildRecord buildRecord = buildTask.getBuildRecord();
        StringWriter stackTraceWriter = new StringWriter();
        PrintWriter stackTracePrinter = new PrintWriter(stackTraceWriter);
        e.printStackTrace(stackTracePrinter);
        buildRecord.setStatus(SYSTEM_ERROR);

        String errorMessage = "Last build status: " + buildTask.getStatus().toString() + "\n";
        errorMessage += "Caught exception: " + stackTraceWriter.toString();
        buildRecord.setBuildLog(errorMessage);
        buildRecord.setEndTime(Date.from(Instant.now()));

        log.debugf("Storing ERROR result of %s to datastore. Error: %s", buildTask.getBuildConfigurationAudited().getName() + "\n\n\n Exception: " + errorMessage, e);
        datastore.storeCompletedBuild(buildRecord);
    }

    public Integer getNextBuildRecordId() {
        return datastore.getNextBuildRecordId();
    }

    private void linkArtifactsWithBuildRecord(List<Artifact> artifacts, BuildRecord buildRecord) {
        artifacts.forEach(a -> a.setBuildRecord(buildRecord));
    }

    public boolean isBuildConfigurationBuilt() {
        return false; // TODO
    }
}
