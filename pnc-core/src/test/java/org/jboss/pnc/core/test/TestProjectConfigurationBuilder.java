package org.jboss.pnc.core.test;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectConfigurationBuilder {

    Environment javaEnvironment = EnvironmentBuilder.defaultEnvironment().build();

    public ProjectBuildConfiguration build(int id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        ProjectBuildConfiguration projectBuildConfiguration = new ProjectBuildConfiguration();
        projectBuildConfiguration.setIdentifier(id + "");
        projectBuildConfiguration.setEnvironment(javaEnvironment);
        projectBuildConfiguration.setProject(project);
        projectBuildConfiguration.addDependency(projectBuildConfiguration);
        project.addProjectBuildConfiguration(projectBuildConfiguration);
        return projectBuildConfiguration;
    }


}
