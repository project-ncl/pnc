package org.jboss.pnc.spi.datastore;

import org.jboss.pnc.model.ProjectBuildResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public interface Datastore {
    void storeCompletedBuild(ProjectBuildResult buildResult) throws DatastoreException;
}
