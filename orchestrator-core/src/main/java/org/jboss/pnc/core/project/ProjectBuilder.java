package org.jboss.pnc.core.project;

import org.jboss.pnc.core.buildinfo.model.BuildInfo;
import org.jboss.pnc.core.environment.model.RunnableEnvironment;
import org.jboss.pnc.core.project.model.ProjectBuildInfo;
import org.jboss.pnc.core.repository.model.RunnableRepositoriesConfiguration;

public interface ProjectBuilder {
    ProjectBuildInfo buildProject(RunnableEnvironment environmentForBuild, RunnableRepositoriesConfiguration repositoriesForBuild);
    ProjectBuildInfo collectResults(BuildInfo buildInfo);
}
