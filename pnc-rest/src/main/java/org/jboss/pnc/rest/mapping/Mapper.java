package org.jboss.pnc.rest.mapping;

import org.jboss.pnc.model.ProjectBuildConfiguration;

public class Mapper {

    public static ProjectBuildConfigurationRest mapToProjectBuildConfigurationRest(ProjectBuildConfiguration projectBuildConfiguration) {
        if(projectBuildConfiguration == null) {
            return null;
        }

        ProjectBuildConfigurationRest projectBuildConfigurationRest = new ProjectBuildConfigurationRest();
        projectBuildConfigurationRest.setId(projectBuildConfiguration.getId());
        projectBuildConfigurationRest.setIdentifier(projectBuildConfiguration.getIdentifier());
        projectBuildConfigurationRest.setProjectName(projectBuildConfiguration.getProject().getName());
        return projectBuildConfigurationRest;
    }
}
