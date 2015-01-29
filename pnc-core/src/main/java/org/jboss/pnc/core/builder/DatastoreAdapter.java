package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;

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

    public void storeResult(BuildTask buildTask, BuildResult completedBuild) throws DatastoreException {
        try {
            BuildConfiguration buildConfiguration = buildTask.getBuildConfiguration();

            BuildRecord buildRecord = new BuildRecord();
            buildRecord.setBuildLog(completedBuild.getBuildLog());
            buildRecord.setStatus(completedBuild.getBuildDriverStatus());
            buildRecord.setBuildConfiguration(buildConfiguration);
            // Additional information needed for historical purpose
            buildRecord.setBuildScript(buildConfiguration.getBuildScript());
            buildRecord.setSourceUrl(buildConfiguration.getScmUrl());

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
        buildRecord.setStatus(BuildDriverStatus.UNKNOWN); //TODO set error status. Is it ok to store DBS (Jenkins), if we are storing BSD than UNKNOWN is the right one

        String errorMessage = "Last build status: " + buildTask.getStatus().toString() + "\n";
        errorMessage += "Caught exception: " + stackTraceWriter.toString();
        buildRecord.setBuildLog(errorMessage);
        buildRecord.setBuildConfiguration(buildConfiguration);
        // Additional information needed for historical purpose
        buildRecord.setBuildScript(buildConfiguration.getBuildScript());
        buildRecord.setSourceUrl(buildConfiguration.getScmUrl());
        log.debugf("Storing ERROR result of %s to datastore. Error: %s", buildConfiguration.getName(), e);
        datastore.storeCompletedBuild(buildRecord);
    }

    public boolean isBuildConfigurationBuilt() {
        return false; //TODO
    }
}
