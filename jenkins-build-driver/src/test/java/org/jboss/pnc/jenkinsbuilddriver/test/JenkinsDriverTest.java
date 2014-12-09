package org.jboss.pnc.jenkinsbuilddriver.test;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.BooleanWrapper;
import org.jboss.pnc.jenkinsbuilddriver.JenkinsBuildDriver;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

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
                .addPackages(true, org.apache.http.client.HttpResponseException.class.getPackage())
                .addClass(Configuration.class)
                .addClass(JenkinsBuildDriver.class);
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    JenkinsBuildDriver jenkinsBuildDriver;

    //TODO disable/enable by maven profile
    @Test
    /** disabled by default you need to configure pnc-config.ini in test/resources */
    public void startJenkinsJobTestCase() throws Exception {

        ProjectBuildConfiguration pbc = new ProjectBuildConfiguration();
        pbc.setScmUrl("https://github.com/project-ncl/pnc.git");
        pbc.setBuildScript("mvn clean install -Dmaven.test.skip");
        Project project = new Project();
        project.setName("PNC-executed-from-jenkins-driver-test");
        pbc.setProject(project);

        Consumer<TaskStatus> updateStatus = (ts) -> {};
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration() {
            @Override
            public RepositoryType getType() {
                return RepositoryType.MAVEN;
            }

            @Override
            public String getId() {
                return "mock-config";
            }

            @Override
            public RepositoryConnectionInfo getConnectionInfo() {
                return new RepositoryConnectionInfo() {
                    @Override
                    public String getDependencyUrl() {
                        return "https://repository.jboss.org/nexus/content/repositories/central";
                    }

                    @Override
                    public String getToolchainUrl() {
                        return null;
                    }

                    @Override
                    public String getDeployUrl() {
                        return null;
                    }

                    @Override
                    public Map<String, String> getProperties() {
                        return null;
                    }
                };
            }
        };

        final Semaphore mutex = new Semaphore(1);
        BooleanWrapper completed = new BooleanWrapper(false);

        Consumer<String> onComplete = (id) -> {
            completed.set(true);
            mutex.release();
        };
        Consumer<Exception> onError = (e) -> {
            e.printStackTrace();
        };
        mutex.acquire();
        jenkinsBuildDriver.startProjectBuild(pbc, repositoryConfiguration, onComplete, onError);

        mutex.acquire(); //wait for callback to release
        Assert.assertTrue("There was no complete callback.", completed.get());
    }

    class BooleanWrap {
    }
}
