package org.jboss.pnc.datastore;

import org.jboss.pnc.datastore.spi.Datastore;
import org.jboss.pnc.model.BuildResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public class DefaultDatastore implements Datastore {
    public void storeCompletedBuild(BuildResult buildResult) {
        buildResult.getStatus();
        buildResult.getBuildArtifacts();
        buildResult.getDependencies();
    }
}
