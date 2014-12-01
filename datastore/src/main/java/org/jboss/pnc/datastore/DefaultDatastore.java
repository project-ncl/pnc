package org.jboss.pnc.datastore;

import org.jboss.pnc.spi.datastore.Datastore;
import org.jboss.pnc.model.ProjectBuildResult;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on
 * 2014-11-24.
 */
public class DefaultDatastore implements Datastore {
	public void storeCompletedBuild(ProjectBuildResult buildResult) {
		buildResult.getStatus();
		buildResult.getBuiltArtifacts();
		buildResult.getDependencies();
	}
}
