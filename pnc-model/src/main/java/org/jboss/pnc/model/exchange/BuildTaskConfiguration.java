package org.jboss.pnc.model.exchange;

import org.jboss.pnc.model.ProjectBuildConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-04.
 */
public class BuildTaskConfiguration {
    private final ProjectBuildConfiguration projectBuildConfiguration;
    private final RepositoryConfiguration repositoryConfiguration;

    public BuildTaskConfiguration(ProjectBuildConfiguration projectBuildConfiguration, org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration repositoryConfiguration) {
        this.projectBuildConfiguration = projectBuildConfiguration;
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }
}
