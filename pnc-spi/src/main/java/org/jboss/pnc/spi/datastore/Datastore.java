package org.jboss.pnc.spi.datastore;

import org.jboss.pnc.model.BuildResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public interface Datastore {
    public void storeCompletedBuild(BuildResult buildResult);
}
