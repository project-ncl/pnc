package org.jboss.pnc.core.builder;

import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.builddriver.BuildJobConfiguration;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-15.
 */
public class DatastoreAdapter {

    private Datastore datastore;

    @Inject
    public DatastoreAdapter(Datastore datastore) {
        this.datastore = datastore;
    }

    public void storeResult(BuildJobDetails buildDetails, BuildJobConfiguration buildJobConfiguration) {
        ProjectBuildResult buildResult = new ProjectBuildResult();
        buildResult.setBuildLog(buildDetails.getBuildLog());
        buildResult.setStatus(buildResult.getStatus());
        buildResult.setProjectBuildConfiguration(buildJobConfiguration.getProjectBuildConfiguration());
        datastore.storeCompletedBuild(buildResult);
    }

}
