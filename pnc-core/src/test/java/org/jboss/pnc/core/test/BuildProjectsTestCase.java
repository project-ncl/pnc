package org.jboss.pnc.core.test;

import static org.assertj.core.api.Assertions.assertThat;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class BuildProjectsTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addClass(ProjectBuilder.class)
                .addClass(BuildDriverFactory.class).addClass(RepositoryManagerFactory.class).addClass(Resources.class)
                .addClass(EnvironmentBuilder.class).addClass(EnvironmentDriverProvider.class)
                .addPackage(BuildDriverMock.class.getPackage()).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties");
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    ProjectBuilder projectBuilder;

    @Inject
    DatastoreMock datastore;

    @Inject
    Logger log;

    @Test
    public void createProjectStructure() throws InterruptedException, CoreException {
        Environment javaEnvironment = EnvironmentBuilder.defaultEnvironment().build();
        Environment nativeEnvironment = EnvironmentBuilder.defaultEnvironment().withNative().build();

        Project p1 = new Project();
        p1.setId(1);
        p1.setName("p1-native");
        ProjectBuildConfiguration projectBuildConfigurationA1 = new ProjectBuildConfiguration();
        projectBuildConfigurationA1.setEnvironment(nativeEnvironment);
        projectBuildConfigurationA1.setProject(p1);
        p1.addProjectBuildConfiguration(projectBuildConfigurationA1);

        Project p2 = new Project();
        p2.setId(2);
        p2.setName("p2-java");
        ProjectBuildConfiguration projectBuildConfigurationB1 = new ProjectBuildConfiguration();
        projectBuildConfigurationB1.setEnvironment(javaEnvironment);
        projectBuildConfigurationB1.setProject(p2);
        projectBuildConfigurationB1.addDependency(projectBuildConfigurationA1);
        p2.addProjectBuildConfiguration(projectBuildConfigurationB1);

        Project p3 = new Project();
        p3.setId(3);
        p3.setName("p3-java");
        ProjectBuildConfiguration projectBuildConfigurationC1 = new ProjectBuildConfiguration();
        projectBuildConfigurationC1.setEnvironment(javaEnvironment);
        projectBuildConfigurationC1.setProject(p3);
        p3.addProjectBuildConfiguration(projectBuildConfigurationC1);

        Project p4 = new Project();
        p4.setId(4);
        p4.setName("p4-java");
        ProjectBuildConfiguration projectBuildConfigurationD1 = new ProjectBuildConfiguration();
        projectBuildConfigurationD1.setProject(p4);
        projectBuildConfigurationD1.addDependency(projectBuildConfigurationB1);
        projectBuildConfigurationD1.addDependency(projectBuildConfigurationC1);
        p4.addProjectBuildConfiguration(projectBuildConfigurationD1);

        Project p5 = new Project();
        p5.setId(5);
        p5.setName("p5-docker");
        ProjectBuildConfiguration projectBuildConfigurationE1 = new ProjectBuildConfiguration();
        projectBuildConfigurationE1.setProject(p5);
        projectBuildConfigurationE1.addDependency(projectBuildConfigurationD1);
        p5.addProjectBuildConfiguration(projectBuildConfigurationE1);

        Project p6 = new Project();
        p6.setId(6);
        p6.setName("p6-java");

        ProjectBuildConfiguration projectBuildConfigurationF1 = new ProjectBuildConfiguration();
        projectBuildConfigurationF1.setEnvironment(javaEnvironment);
        projectBuildConfigurationF1.setProject(p6);
        p6.addProjectBuildConfiguration(projectBuildConfigurationF1);

        HashSet<ProjectBuildConfiguration> projectBuildConfigurations = new HashSet<>(
                Arrays.asList(new ProjectBuildConfiguration[] { projectBuildConfigurationA1, projectBuildConfigurationB1,
                        projectBuildConfigurationC1, projectBuildConfigurationD1, projectBuildConfigurationE1,
                        projectBuildConfigurationF1 }));

        log.info("Got projectBuilder: " + projectBuilder);
        log.info("Building projectBuildConfigurations: " + projectBuildConfigurations.size());

        BuildCollection buildCollection = new BuildCollection();
        buildCollection.setProductName("foo");
        buildCollection.setProductVersion("1.0");

        projectBuilder.buildProjects(projectBuildConfigurations, buildCollection);

        assertThat(datastore.getBuildResults()).hasSize(3);
    }
}
