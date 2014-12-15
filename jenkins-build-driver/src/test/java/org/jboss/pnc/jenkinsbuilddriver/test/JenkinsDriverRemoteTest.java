package org.jboss.pnc.jenkinsbuilddriver.test;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.Resources;
import org.jboss.pnc.common.util.BooleanWrapper;
import org.jboss.pnc.jenkinsbuilddriver.JenkinsBuildDriver;
import org.jboss.pnc.jenkinsbuilddriver.buildmonitor.JenkinsBuildMonitor;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryConnectionInfo;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class JenkinsDriverRemoteTest {

    private JenkinsServer jenkins;

    @Inject
    Logger log;

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties")
                .addAsResource("jenkins-job-template.xml")
                .addPackages(true, org.apache.http.client.HttpResponseException.class.getPackage())
                .addClass(Configuration.class)
                .addClass(Resources.class)
                .addClass(JenkinsBuildDriver.class)
                .addClass(JenkinsBuildMonitor.class);
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    JenkinsBuildDriver jenkinsBuildDriver;

    @Test
    @Ignore //requires configuration //TODO
    public void startJenkinsJobTestCase() throws Exception {
        ProjectBuildConfiguration pbc = getProjectBuildConfiguration();

        RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration();

        final Semaphore mutex = new Semaphore(1);
        BooleanWrapper completed = new BooleanWrapper(false);

        class BuildTask {
            BuildJobDetails buildJobDetails;
        }

        final BuildTask buildTask = new BuildTask();

        Consumer<BuildJobDetails> onComplete = (buildJobDetails) -> {
            buildTask.buildJobDetails = buildJobDetails;
            completed.set(true);
            mutex.release();
        };
        Consumer<Exception> onError = (e) -> {
            throw new AssertionError(e);
        };
        mutex.acquire();
        jenkinsBuildDriver.startProjectBuild(pbc, repositoryConfiguration, onComplete, onError);
        mutex.tryAcquire(30, TimeUnit.SECONDS); //wait for callback to release

        Assert.assertTrue("There was no complete callback.", completed.get());
        Assert.assertNotNull(buildTask.buildJobDetails);

        completed.set(false);
        long waitStarted = System.currentTimeMillis();
        final Long[] buildTook = new Long[1];

        Consumer<String> onWaitComplete = (s) -> {
            completed.set(true);
            buildTook[0] = System.currentTimeMillis() - waitStarted;
            log.info("Received build completed in " + buildTook[0] + "ms.");
            mutex.release();
        };
        Consumer<Exception> onWaitError = (e) -> {
            throw new AssertionError(e);
        };

        jenkinsBuildDriver.waitBuildToComplete(buildTask.buildJobDetails, onWaitComplete, onWaitError);
        mutex.tryAcquire(120, TimeUnit.SECONDS); //wait for callback to release

        long minBuildTime = 10000;
        Assert.assertTrue("Received build completed in " + buildTook[0] + " while expected >" + minBuildTime + ".", buildTook[0] >= minBuildTime);

        Assert.assertTrue("There was no complete callback.", completed.get());


        completed.set(false);

        BuildDriverResult buildDriverResult = new BuildDriverResult();

        Consumer<BuildDriverResult> onResultComplete = (receivedBuildDriverResult) -> {
            completed.set(true);
            buildDriverResult.setBuildStatus(receivedBuildDriverResult.getBuildStatus());
            buildDriverResult.setConsoleOutput(receivedBuildDriverResult.getConsoleOutput());
            mutex.release();
        };
        Consumer<Exception> onResultError = (e) -> {
            throw new AssertionError(e);
        };

        jenkinsBuildDriver.retrieveBuildResults(buildTask.buildJobDetails, onResultComplete, onResultError);
        mutex.tryAcquire(30, TimeUnit.SECONDS); //wait for callback to release

        Assert.assertEquals(BuildStatus.SUCCESS, buildDriverResult.getBuildStatus());
        Assert.assertTrue("Incomplete build log.", buildDriverResult.getConsoleOutput().contains("Building in workspace"));
        Assert.assertTrue("Incomplete build log.", buildDriverResult.getConsoleOutput().contains("Finished: SUCCESS"));

        Assert.assertTrue("There was no complete callback.", completed.get());

    }

    private RepositoryConfiguration getRepositoryConfiguration() {
        return new RepositoryConfiguration() {
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
    }

    private ProjectBuildConfiguration getProjectBuildConfiguration() {
        ProjectBuildConfiguration pbc = new ProjectBuildConfiguration();
        pbc.setScmUrl("https://github.com/project-ncl/pnc.git");
        pbc.setBuildScript("mvn clean install -Dmaven.test.skip");
        pbc.setIdentifier("PNC-executed-from-jenkins-driver-test");
        Project project = new Project();
        project.setName("PNC-executed-from-jenkins-driver-test");
        pbc.setProject(project);
        return pbc;
    }

}
