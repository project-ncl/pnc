package org.jboss.pnc.core.test.mock;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.spi.datastore.Datastore;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
@ApplicationScoped
public class DatastoreMock implements Datastore {
    private List<ProjectBuildResult> buildResults = new ArrayList<>();

    @Override
    public void storeCompletedBuild(ProjectBuildResult buildResult) {
        buildResults.add(buildResult);
    }

    public List<ProjectBuildResult> getBuildResults() {
        return buildResults;
    }
}
