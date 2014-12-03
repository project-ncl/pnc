package org.jboss.pnc.jenkinsbuilddriver.test;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.jenkinsbuilddriver.JenkinsBuildDriver;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import javax.inject.Inject;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
//@RunWith(Arquillian.class)
public class JenkinsDriverTest {

    private JenkinsServer jenkins;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties")
                .addAsResource("jenkins-job-template.xml")
                .addAsResource("pnc-config.ini")
                .addPackages(true, org.apache.http.client.HttpResponseException.class.getPackage())
                .addClass(Configuration.class)
                .addClass(JenkinsBuildDriver.class);
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    JenkinsBuildDriver jenkinsBuildDriver;

    //TODO disable/enable by maven profile
    //@Test /** disabled by default you need to configure pnc-config.ini in test/resources */
    public void startJenkinsJobTestCase() throws Exception {

        ProjectBuildConfiguration projectBuildConfiguration = new ProjectBuildConfiguration();
        projectBuildConfiguration.setScmUrl("https://github.com/project-ncl/pnc.git");
        projectBuildConfiguration.setBuildScript("mvn clean install");
        Project project = new Project();
        project.setName("PNC-executed-from-test");
        projectBuildConfiguration.setProject(project);

        Consumer<TaskStatus> updateStatus = (ts) -> {};
        jenkinsBuildDriver.startProjectBuild(projectBuildConfiguration, null, updateStatus);

    }
}
