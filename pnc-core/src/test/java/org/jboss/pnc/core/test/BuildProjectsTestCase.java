package org.jboss.pnc.core.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.test.mock.BuildDriverMock;
import org.jboss.pnc.core.test.mock.DatastoreMock;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Project;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class BuildProjectsTestCase {

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClass(ProjectBuilder.class)
                .addClass(BuildDriverFactory.class)
                .addClass(RepositoryManagerFactory.class)
                .addClass(Resources.class)
                .addPackage(BuildDriverMock.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
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

        Project p1 = new Project("p1-native", BuildType.NATIVE);
        Project p2 = new Project("p2-java", BuildType.JAVA, p1);
        Project p3 = new Project("p3-java", BuildType.JAVA);
        Project p4 = new Project("p4-java", BuildType.JAVA, p2, p3);
        Project p5 = new Project("p5-docker", BuildType.DOCKER, p4);
        Project p6 = new Project("p6-java", BuildType.JAVA);

        HashSet<Project> projects = new HashSet<Project>(Arrays.asList(new Project[]{p1, p2, p3, p4, p5, p6}));

        projectBuilder.buildProjects(projects);

        log.info("Got " + datastore.getBuildResults().size() + " results.");
        Assert.assertTrue(datastore.getBuildResults().size() > 0);
    }
}
