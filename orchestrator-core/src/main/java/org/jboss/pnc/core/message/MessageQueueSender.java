package org.jboss.pnc.core.message;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.User;
import org.jboss.pnc.core.environment.model.RunnableEnvironment;
import org.jboss.pnc.core.repository.model.RunnableRepositoriesConfiguration;

public interface MessageQueueSender {
    void notifyBuildStarted(BuildIdentifier buildId, User whoStartedBuild);
    void notifyEnvironmentAssembled(BuildIdentifier buildId, RunnableEnvironment environmentForBuild, RunnableRepositoriesConfiguration repositoriesForBuild);
    void notifyBuildFinished(BuildIdentifier buildId, User whoFinishedBuild);
}
