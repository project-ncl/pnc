package org.jboss.pnc.jenkinsbuilddriver.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.util.ObjectWrapper;
import org.jboss.pnc.jenkinsbuilddriver.JenkinsBuildDriver;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.CompletedBuild;
import org.jboss.pnc.spi.builddriver.RunningBuild;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.environment.RunningEnvironment;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@RunWith(Arquillian.class)
public class JenkinsDriverRemoteTest {

    private static final Logger log = Logger.getLogger(JenkinsDriverRemoteTest.class.getName());

    @Deployment
    public static JavaArchive createDeployment() {
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class).addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("META-INF/logging.properties").addAsResource("freeform-job-template.xml")
                .addPackages(true, org.apache.http.client.HttpResponseException.class.getPackage())
                .addPackages(true, Configuration.class.getPackage())
                .addPackage(JenkinsBuildDriver.class.getPackage());
        System.out.println(jar.toString(true));
        return jar;
    }

    @Inject
    JenkinsBuildDriver jenkinsBuildDriver;

    @Test
    //@Ignore("To be fixed by NCL-554")
    public void startJenkinsJobTestCase() throws Exception {
        BuildConfiguration pbc = getBuildConfiguration();

        RunningEnvironment runningEnvironment = getRunningEnvironment();

        final Semaphore mutex = new Semaphore(1);
        ObjectWrapper<Boolean> completed = new ObjectWrapper<>(false);
        ObjectWrapper<BuildDriverResult> resultWrapper = new ObjectWrapper<>();
        ObjectWrapper<Long> buildStarted = new ObjectWrapper<>();
        ObjectWrapper<Long> buildTook = new ObjectWrapper<>();

        class BuildTask {
            CompletedBuild buildJobDetails;
        }

        final BuildTask buildTask = new BuildTask();

        Consumer<CompletedBuild> onComplete = (completedBuild) -> {
            buildTask.buildJobDetails = completedBuild;
            completed.set(true);
            buildTook.set(System.currentTimeMillis() - buildStarted.get());
            log.info("Received build completed in " + buildTook.get() + "ms.");

            try {
                resultWrapper.set(completedBuild.getBuildResult());
            } catch (BuildDriverException e) {
                throw new AssertionError("Cannot get build result.", e);
            }

            mutex.release();
        };

        Consumer<Exception> onError = (e) -> {
            throw new AssertionError(e);
        };

        mutex.acquire();
        RunningBuild runningBuild = jenkinsBuildDriver.startProjectBuild(pbc, runningEnvironment);
        buildStarted.set(System.currentTimeMillis());
        runningBuild.monitor(onComplete, onError);
        mutex.tryAcquire(60, TimeUnit.SECONDS); // wait for callback to release

        Assert.assertTrue("There was no complete callback.", completed.get());
        Assert.assertNotNull(buildTask.buildJobDetails);

        long minBuildTime = 5000;
        Assert.assertTrue("Received build completed in " + buildTook.get() + " while expected >" + minBuildTime + ".",
                buildTook.get() >= minBuildTime);

        BuildDriverResult buildDriverResult = resultWrapper.get();

        Assert.assertEquals(BuildDriverStatus.SUCCESS, buildDriverResult.getBuildDriverStatus());
        Assert.assertTrue("Incomplete build log.", buildDriverResult.getBuildLog().contains("Building in workspace"));
        Assert.assertTrue("Incomplete build log.", buildDriverResult.getBuildLog().contains("Finished: SUCCESS"));

        Assert.assertTrue("There was no complete callback.", completed.get());
    }

    private RunningEnvironment getRunningEnvironment() {
        final RepositorySession repositoryConfiguration = getRepositoryConfiguration();
        return new RunningEnvironment() {
            
            @Override
            public void transferDataToEnvironment(String pathOnHost, String data) throws EnvironmentDriverException {
                
            }
            
            @Override
            public void transferDataToEnvironment(String pathOnHost, InputStream stream)
                    throws EnvironmentDriverException {
                
            }
            
            @Override
            public RepositorySession getRepositorySession() {
                return repositoryConfiguration;
            }
            
            @Override
            public String getJenkinsUrl() {
                return System.getenv("PNC_JENKINS_URL") + ":" + getJenkinsPort();
            }
            
            @Override
            public int getJenkinsPort() {
                return Integer.parseInt(System.getenv("PNC_JENKINS_PORT"));
            }
            
            @Override
            public String getId() {
                return null;
            }
            
            @Override
            public void destroyEnvironment() throws EnvironmentDriverException {
                
            }
        };
    }

    private RepositorySession getRepositoryConfiguration() {
        return new RepositorySession() {
            @Override
            public RepositoryType getType() {
                return RepositoryType.MAVEN;
            }

            @Override
            public String getBuildRepositoryId() {
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

            @Override
            public RepositoryManagerResult extractBuildArtifacts() throws RepositoryManagerException {
                return new RepositoryManagerResult() {
                    @Override
                    public List<Artifact> getBuiltArtifacts() {
                        List<Artifact> builtArtifacts = Collections.emptyList();
                        builtArtifacts.add(getArtifact(1));
                        return builtArtifacts;
                    }

                    @Override
                    public List<Artifact> getDependencies() {
                        List<Artifact> dependencies = Collections.emptyList();
                        dependencies.add(getArtifact(10));
                        return dependencies;
                    }
                };
            }

            private Artifact getArtifact(int i) {
                Artifact artifact = new Artifact();
                artifact.setId(i);
                artifact.setIdentifier("test" + i);
                return artifact;
            }

            @Override
            public String getBuildSetRepositoryId() {
                return null;
            }

        };
    }

    private BuildConfiguration getBuildConfiguration() {
        BuildConfiguration pbc = new BuildConfiguration();
        pbc.setScmRepoURL("https://github.com/project-ncl/pnc.git");
        pbc.setScmRevision("*/master"); // this is default
        pbc.setBuildScript("mvn validate");
        pbc.setName("PNC-executed-from-jenkins-driver-test");
        Project project = new Project();
        project.setName("PNC-executed-from-jenkins-driver-test");
        pbc.setProject(project);
        return pbc;
    }

}
