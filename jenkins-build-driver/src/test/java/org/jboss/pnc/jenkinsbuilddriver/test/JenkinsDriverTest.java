package org.jboss.pnc.jenkinsbuilddriver.test;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.jenkinsbuilddriver.JenkinsBuildDriver;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
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

    @Test
    public void startJenkinsJobTestCase() throws Exception {

        Project project = new Project("PNC-executed-from-test", new Environment());
        project.setScmUrl("https://github.com/project-ncl/pnc.git");
        jenkinsBuildDriver.startProjectBuild(project);

    }
}
