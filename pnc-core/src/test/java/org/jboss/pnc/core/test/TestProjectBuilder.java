package org.jboss.pnc.core.test;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectBuilder {

    Environment javaEnvironment = EnvironmentBuilder.defaultEnvironment().build();
    Environment nativeEnvironment = EnvironmentBuilder.defaultEnvironment().withNative().build();

    public Project buildProject(int id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        ProjectBuildConfiguration projectBuildConfigurationB1 = new ProjectBuildConfiguration();
        projectBuildConfigurationB1.setEnvironment(javaEnvironment);
        projectBuildConfigurationB1.setProject(project);
        projectBuildConfigurationB1.addDependency(projectBuildConfigurationB1);
        project.addProjectBuildConfiguration(projectBuildConfigurationB1);
        return project;
    }

//TODO create test projects
//        Project p1 = new Project();
//        p1.setId(1);
//        p1.setName("p1-native");
//        ProjectBuildConfiguration projectBuildConfigurationA1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationA1.setEnvironment(nativeEnvironment);
//        projectBuildConfigurationA1.setProject(p1);
//        p1.addProjectBuildConfiguration(projectBuildConfigurationA1);


//        Project p3 = new Project();
//        p3.setId(3);
//        p3.setName("p3-java");
//        ProjectBuildConfiguration projectBuildConfigurationC1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationC1.setEnvironment(javaEnvironment);
//        projectBuildConfigurationC1.setProject(p3);
//        p3.addProjectBuildConfiguration(projectBuildConfigurationC1);
//
//        Project p4 = new Project();
//        p4.setId(4);
//        p4.setName("p4-java");
//        ProjectBuildConfiguration projectBuildConfigurationD1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationD1.setEnvironment(javaEnvironment);
//        projectBuildConfigurationD1.setProject(p4);
//        projectBuildConfigurationD1.addDependency(projectBuildConfigurationB1);
//        projectBuildConfigurationD1.addDependency(projectBuildConfigurationC1);
//        p4.addProjectBuildConfiguration(projectBuildConfigurationD1);
//
//        Project p5 = new Project();
//        p5.setId(5);
//        p5.setName("p5-native");
//        ProjectBuildConfiguration projectBuildConfigurationE1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationE1.setEnvironment(nativeEnvironment);
//        projectBuildConfigurationE1.setProject(p5);
//        projectBuildConfigurationE1.addDependency(projectBuildConfigurationD1);
//        p5.addProjectBuildConfiguration(projectBuildConfigurationE1);
//
//        Project p6 = new Project();
//        p6.setId(6);
//        p6.setName("p6-java");
//
//        ProjectBuildConfiguration projectBuildConfigurationF1 = new ProjectBuildConfiguration();
//        projectBuildConfigurationF1.setEnvironment(javaEnvironment);
//        projectBuildConfigurationF1.setProject(p6);
//        p6.addProjectBuildConfiguration(projectBuildConfigurationF1);
//
//        HashSet<ProjectBuildConfiguration> projectBuildConfigurations = new HashSet<>(
//                Arrays.asList(new ProjectBuildConfiguration[] { projectBuildConfigurationA1, projectBuildConfigurationB1,
//                        projectBuildConfigurationC1, projectBuildConfigurationD1, projectBuildConfigurationE1,
//                        projectBuildConfigurationF1 }));
//
//        log.info("Got projectBuilder: " + projectBuilder);
//        log.info("Building projectBuildConfigurations: " + projectBuildConfigurations.size());


}
