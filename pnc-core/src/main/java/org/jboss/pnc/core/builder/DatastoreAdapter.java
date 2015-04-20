package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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

    public void storeResult(BuildTask buildTask, BuildResult buildResult) throws DatastoreException {
        try {
            BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();
            
            BuildDriverResult buildDriverResult = buildResult.getBuildDriverResult();
            RepositoryManagerResult repositoryManagerResult = buildResult.getRepositoryManagerResult();

            BuildRecord buildRecord = new BuildRecord();
            buildRecord.setLatestBuildConfiguration(buildConfiguration);
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
            // Additional information needed for historical purpose
            //buildRecord.setBuildConfigurationRev(buildConfiguration.getRev());
            setAuditDataToBuildRecord(buildRecord, buildTask);

            log.debugf("Storing results of %s to datastore.", buildConfiguration.getName());
            datastore.storeCompletedBuild(buildRecord);
        } catch (Exception e) {
            throw new DatastoreException("Error storing the result to datastore.", e);
        }
    }

    public void storeResult(BuildTask buildTask, Throwable e) throws DatastoreException {
        BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();

        BuildRecord buildRecord = new BuildRecord();
        StringWriter stackTraceWriter = new StringWriter();
        PrintWriter stackTracePrinter = new PrintWriter(stackTraceWriter);
        e.printStackTrace(stackTracePrinter);
        buildRecord.setStatus(BuildStatus.SYSTEM_ERROR); 
        
        String errorMessage = "Last build status: " + buildTask.getStatus().toString() + "\n";
        errorMessage += "Caught exception: " + stackTraceWriter.toString();
        buildRecord.setBuildLog(errorMessage);
        buildRecord.setLatestBuildConfiguration(buildConfiguration);
        // Additional information needed for historical purpose
        //buildRecord.setBuildConfigurationRev(buildConfiguration.getRev());
        setAuditDataToBuildRecord(buildRecord, buildTask);

        log.debugf("Storing ERROR result of %s to datastore. Error: %s", buildConfiguration.getName() + "\n\n\n Exception: " + errorMessage, e);
        datastore.storeCompletedBuild(buildRecord);
    }

    private void setAuditDataToBuildRecord(BuildRecord buildRecord, BuildTask buildTask) {
        buildRecord.setBuildConfigurationAudited(null); //TODO add BuildConfigurationAudit
        buildRecord.setUser(buildTask.getUser());
        buildRecord.setStartTime(Timestamp.from(Instant.ofEpochMilli(buildTask.getStartTime())));
        buildRecord.setEndTime(Timestamp.from(Instant.now()));
    }

    private void linkArtifactsWithBuildRecord(List<Artifact> artifacts, BuildRecord buildRecord) {
        artifacts.forEach(a -> a.setBuildRecord(buildRecord));
    }

    public boolean isBuildConfigurationBuilt() {
        return false; // TODO
    }
}
