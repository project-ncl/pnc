package org.jboss.pnc.core.test.mock;

import org.jboss.pnc.core.spi.datastore.Datastore;
import org.jboss.pnc.model.BuildResult;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
@ApplicationScoped
public class DatastoreMock implements Datastore {
    private List<BuildResult> buildResults = new ArrayList<>();

    @Override
    public void storeCompletedBuild(BuildResult buildResult) {
        buildResults.add(buildResult);
    }

    public List<BuildResult> getBuildResults() {
        return buildResults;
    }
}
