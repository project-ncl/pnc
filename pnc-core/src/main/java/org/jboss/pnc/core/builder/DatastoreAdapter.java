package org.jboss.pnc.core.builder;

import org.jboss.logging.Logger;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.builddriver.BuildResult;
import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.spi.datastore.DatastoreException;

import javax.inject.Inject;
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
            ProjectBuildConfiguration projectBuildConfiguration = buildTask.getProjectBuildConfiguration();

            ProjectBuildResult buildResult = new ProjectBuildResult();
            buildResult.setBuildLog(completedBuild.getBuildLog());
            buildResult.setStatus(completedBuild.getBuildDriverStatus());
            buildResult.setProjectBuildConfiguration(projectBuildConfiguration);
            log.debugf("Storing results of %s to datastore.", projectBuildConfiguration.getIdentifier());
            datastore.storeCompletedBuild(buildResult);
        } catch (Exception e) {
            throw new DatastoreException("Error storing the result to datastore.", e);
        }
    }

    public void storeResult(BuildTask buildTask, Throwable e) throws DatastoreException {
        ProjectBuildConfiguration projectBuildConfiguration = buildTask.getProjectBuildConfiguration();

        ProjectBuildResult buildResult = new ProjectBuildResult();
        StringWriter stackTraceWriter = new StringWriter();
        buildResult.setStatus(BuildDriverStatus.UNKNOWN); //TODO set error status
        buildResult.setBuildLog(stackTraceWriter.toString());
        log.debugf("Storing ERROR result of %s to datastore. Error: %s", projectBuildConfiguration.getIdentifier(), e);
        datastore.storeCompletedBuild(buildResult);
    }

    public boolean isProjectBuildConfigurationBuilt() {
        return false; //TODO
    }
}
