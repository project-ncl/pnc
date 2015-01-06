package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
@ApplicationScoped
public class DatastoreMock implements Datastore {

    private Logger log = Logger.getLogger(DatastoreMock.class.getName());

    private List<ProjectBuildResult> buildResults = new ArrayList<>();

    @Override
    public void storeCompletedBuild(ProjectBuildResult buildResult) {
        log.info("Storing build " + buildResult.getProjectBuildConfiguration());
        synchronized (this) {
            buildResults.add(buildResult);
        }
    }

    public List<ProjectBuildResult> getBuildResults() {
        return buildResults;
    }
}
