package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.jenkinsbuilddriver.buildmonitor.JenkinsBuildMonitor;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.builddriver.BuildJobDetails;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class JenkinsBuildDriver implements BuildDriver {

    private static final String DRIVER_ID = "jenkins-build-driver";

    /**
     * Server instance, always use getter for lazy initialization
     */
    private JenkinsServer jenkinsServer;

    @Inject
    Configuration configuration;

    @Inject
    private Logger log;

    @Inject
    JenkinsBuildMonitor jenkinsBuildMonitor;

    ExecutorService executor;

    JenkinsBuildDriver() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();
        //Jenkins IO thread pool
        executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, workQueue); //TODO configurable

        //TODO executor shutdown
        //TODO jenkinsBuildMonitor shutdown

    }

    private JenkinsServer getJenkinsServer() throws BuildDriverException {
        if (jenkinsServer == null) {
            initJenkinsServer();
        }
        return jenkinsServer;
    }

    private void initJenkinsServer() throws BuildDriverException {
        try {
            Properties properties = configuration.getModuleConfig(getDriverId());

            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");

            if (url == null || username == null || password == null) {
                throw new BuildDriverException("Missing config to instantiate " + getDriverId() + ".");
            }

            jenkinsServer = new JenkinsServer(new URI(url), username, password);
        } catch (URISyntaxException e) {
            throw new BuildDriverException("Cannot instantiate " + getDriverId() + ".", e);
        }
    }

    @Override
    public String getDriverId() {
        return DRIVER_ID;
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return BuildType.JAVA.equals(buildType);
    }

    @Override
    public void startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration,
                                  RepositoryConfiguration repositoryConfiguration,
                                  Consumer<BuildJobDetails> onComplete, Consumer<Exception> onError) {
        try {
            Runnable job = () -> {
                BuildJob build = null;
                try {
                    build = new BuildJob(getJenkinsServer(), projectBuildConfiguration);
                    boolean configured = build.configure(repositoryConfiguration ,true);
                    if (!configured) {
                        throw new AssertionError("Cannot configure build job.");
                    }
                    int buildNumber = build.start();
                    BuildJobDetails buildJobDetails = new BuildJobDetails(build.getJobName(), buildNumber);
                    onComplete.accept(buildJobDetails);
                } catch (Exception e) {
                    onError.accept(e);
                }
            };
            executor.execute(job);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void waitBuildToComplete(BuildJobDetails buildJobDetails,
                                    Consumer<String> onComplete, Consumer<Exception> onError) {
        try {
            Consumer<String> onMonitorComplete = (jobId) -> {
                onComplete.accept("JOB-ID"); //TODO
            };

            Consumer<Exception> onMonitorError = (e) -> {
                onError.accept(e);
            };

            jenkinsBuildMonitor.monitor(getJenkinsServer(), buildJobDetails, onMonitorComplete, onMonitorError);

        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public void retrieveBuildResults(BuildJobDetails buildJobDetails,
                                     Consumer<BuildDriverResult> onComplete, Consumer<Exception> onError) {
        try {
            Runnable job = () -> {
                try {
                    Build jenkinsBuild = getBuild(getJenkinsServer(), buildJobDetails);
                    BuildWithDetails jenkinsBuildDetails = jenkinsBuild.details();

                    BuildStatusAdapter bsa = new BuildStatusAdapter(jenkinsBuildDetails.getResult());

                    BuildDriverResult buildDriverResult = new BuildDriverResult();
                    buildDriverResult.setBuildStatus(bsa.getBuildStatus());
                    buildDriverResult.setConsoleOutput(jenkinsBuildDetails.getConsoleOutputText());

                    onComplete.accept(buildDriverResult);
                } catch (Exception e) {
                    onError.accept(e);
                }
            };
            executor.execute(job);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    private Build getBuild(JenkinsServer jenkinsServer, BuildJobDetails buildJobDetails) throws IOException, BuildDriverException {
        String jobName = buildJobDetails.getJobName();
        JobWithDetails buildJob = jenkinsServer.getJob(jobName);
        Build jenkinsBuild = buildJob.getLastBuild();
        int buildNumber = jenkinsBuild.getNumber();
        if (buildNumber != buildJobDetails.getBuildNumber()) {
            throw new BuildDriverException("Retrieved wrong build.");
        }
        return jenkinsBuild;
    }
}
