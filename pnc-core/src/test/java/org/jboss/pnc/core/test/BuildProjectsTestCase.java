package org.jboss.pnc.core.test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;
import org.jboss.pnc.spi.environment.EnvironmentDriverProvider;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

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

        Environment dockerEnvironment = EnvironmentBuilder.defaultEnvironment().withDocker().build();
        Environment javaEnvironment = EnvironmentBuilder.defaultEnvironment().build();
        Environment nativeEnvironment = EnvironmentBuilder.defaultEnvironment().withNative().build();

        Project p1 = new Project();
        p1.setName("p1-native");

        ProjectBuildConfiguration projectBuildConfigurationA1 = new ProjectBuildConfiguration();
        projectBuildConfigurationA1.setIdentifier("com.foo.A1");
        projectBuildConfigurationA1.setBuildScript("build.exe");
        projectBuildConfigurationA1.setEnvironment(nativeEnvironment);
        projectBuildConfigurationA1.setCreationTime(Timestamp.from(Instant.now()));
        projectBuildConfigurationA1.setProject(p1);
        projectBuildConfigurationA1.setScmUrl("git+https://code.engineering.redhat.com/gerrit/A1");
        projectBuildConfigurationA1.setPatchesUrl("git://pkgs.devel.redhat.com/rpms/A1");

        p1.addProjectBuildConfiguration(projectBuildConfigurationA1);

        Project p2 = new Project();
        p2.setName("p2-java");

        ProjectBuildConfiguration projectBuildConfigurationB1 = new ProjectBuildConfiguration();
        projectBuildConfigurationB1.setIdentifier("com.foo.B1");
        projectBuildConfigurationA1.setBuildScript("mvn clean package");
        projectBuildConfigurationB1.setEnvironment(javaEnvironment);
        projectBuildConfigurationB1.setCreationTime(Timestamp.from(Instant.now()));
        projectBuildConfigurationB1.setProject(p2);
        projectBuildConfigurationB1.setScmUrl("git+https://code.engineering.redhat.com/gerrit/B1");
        projectBuildConfigurationB1.setPatchesUrl("git://pkgs.devel.redhat.com/rpms/B1");
        projectBuildConfigurationB1.addDependency(projectBuildConfigurationA1);

        p2.addProjectBuildConfiguration(projectBuildConfigurationB1);

        Project p3 = new Project();
        p3.setName("p3-java");

        ProjectBuildConfiguration projectBuildConfigurationC1 = new ProjectBuildConfiguration();
        projectBuildConfigurationC1.setIdentifier("com.foo.C1");
        projectBuildConfigurationA1.setBuildScript("mvn clean install");
        projectBuildConfigurationC1.setEnvironment(javaEnvironment);
        projectBuildConfigurationC1.setCreationTime(Timestamp.from(Instant.now()));
        projectBuildConfigurationC1.setProject(p3);
        projectBuildConfigurationC1.setScmUrl("git+https://code.engineering.redhat.com/gerrit/C1");
        projectBuildConfigurationC1.setPatchesUrl("git://pkgs.devel.redhat.com/rpms/C1");

        p3.addProjectBuildConfiguration(projectBuildConfigurationC1);

        Project p4 = new Project();
        p4.setName("p4-java");

        ProjectBuildConfiguration projectBuildConfigurationD1 = new ProjectBuildConfiguration();
        projectBuildConfigurationD1.setIdentifier("com.foo.D1");
        projectBuildConfigurationA1.setBuildScript("mvn clean deploy");
        projectBuildConfigurationD1.setEnvironment(javaEnvironment);
        projectBuildConfigurationD1.setCreationTime(Timestamp.from(Instant.now()));
        projectBuildConfigurationD1.setProject(p4);
        projectBuildConfigurationD1.setScmUrl("git+https://code.engineering.redhat.com/gerrit/D1");
        projectBuildConfigurationD1.setPatchesUrl("git://pkgs.devel.redhat.com/rpms/D1");
        projectBuildConfigurationD1.addDependency(projectBuildConfigurationB1);
        projectBuildConfigurationD1.addDependency(projectBuildConfigurationC1);

        p4.addProjectBuildConfiguration(projectBuildConfigurationD1);

        Project p5 = new Project();
        p5.setName("p5-docker");

        ProjectBuildConfiguration projectBuildConfigurationE1 = new ProjectBuildConfiguration();
        projectBuildConfigurationE1.setIdentifier("com.foo.E1");
        projectBuildConfigurationA1.setBuildScript("mvn clean package");
        projectBuildConfigurationE1.setEnvironment(dockerEnvironment);
        projectBuildConfigurationE1.setCreationTime(Timestamp.from(Instant.now()));
        projectBuildConfigurationE1.setProject(p5);
        projectBuildConfigurationE1.setScmUrl("git+https://code.engineering.redhat.com/gerrit/E1");
        projectBuildConfigurationE1.setPatchesUrl("git://pkgs.devel.redhat.com/rpms/E1");
        projectBuildConfigurationE1.addDependency(projectBuildConfigurationD1);

        p5.addProjectBuildConfiguration(projectBuildConfigurationE1);

        Project p6 = new Project();
        p6.setName("p6-java");

        ProjectBuildConfiguration projectBuildConfigurationF1 = new ProjectBuildConfiguration();
        projectBuildConfigurationF1.setIdentifier("com.foo.F1");
        projectBuildConfigurationA1.setBuildScript("mvn clean install");
        projectBuildConfigurationF1.setEnvironment(javaEnvironment);
        projectBuildConfigurationF1.setCreationTime(Timestamp.from(Instant.now()));
        projectBuildConfigurationF1.setProject(p6);
        projectBuildConfigurationF1.setScmUrl("git+https://code.engineering.redhat.com/gerrit/F1");
        projectBuildConfigurationF1.setPatchesUrl("git://pkgs.devel.redhat.com/rpms/F1");

        p6.addProjectBuildConfiguration(projectBuildConfigurationF1);

        HashSet<ProjectBuildConfiguration> projectBuildConfigurations = new HashSet<ProjectBuildConfiguration>(
                Arrays.asList(new ProjectBuildConfiguration[] { projectBuildConfigurationA1, projectBuildConfigurationB1,
                        projectBuildConfigurationC1, projectBuildConfigurationD1, projectBuildConfigurationE1,
                        projectBuildConfigurationF1 }));

        projectBuilder.buildProjects(projectBuildConfigurations);

        log.info("Got " + datastore.getBuildResults().size() + " results.");
        Assert.assertTrue(datastore.getBuildResults().size() > 0);
    }
}
