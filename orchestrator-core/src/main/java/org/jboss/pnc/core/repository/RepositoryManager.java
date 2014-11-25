package org.jboss.pnc.core.repository;

import org.jboss.pnc.core.buildinfo.model.BuildIdentifier;
import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.project.model.BuildRecipe;
import org.jboss.pnc.core.repository.model.RunnableRepositoriesConfiguration;

public interface RepositoryManager {
    RunnableRepositoriesConfiguration configureRepositories(BuildRecipe buildRecipe, BuildIdentifier buildId);

    void cleanupRepositoryConfiguration(BuildInfo buildInfo);
}
