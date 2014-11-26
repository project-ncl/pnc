package org.jboss.pnc.core.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.core.BuildDriverFactory;
import org.jboss.pnc.core.RepositoryManagerFactory;
import org.jboss.pnc.core.builder.ProjectBuilder;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.datastore.Datastore;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Project;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;

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
                .addClass(Datastore.class)
                .addClass(Resources.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    ProjectBuilder projectBuilder;

    @Test
    public void createProjectStructure() throws InterruptedException, CoreException {
        Project p1 = new Project("p1-native", BuildType.NATIVE);
        Project p2 = new Project("p2-java", BuildType.JAVA, p1);
        Project p3 = new Project("p3-java", BuildType.JAVA);
        Project p4 = new Project("p4-java", BuildType.JAVA, p2, p3);
        Project p5 = new Project("p5-docker", BuildType.DOCKER, p4);
        Project p6 = new Project("p6-java", BuildType.JAVA);

        HashSet<Project> projects = new HashSet<Project>(Arrays.asList(new Project[]{p5, p6}));

        projectBuilder.buildProjects(projects);

    }


}
