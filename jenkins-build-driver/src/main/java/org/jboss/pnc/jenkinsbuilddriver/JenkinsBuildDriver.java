package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
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

    ExecutorService executor;

    JenkinsBuildDriver() {
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();
        executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.HOURS, workQueue); //TODO configurable
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
                                     Consumer<String> onComplete, Consumer<Exception> onError) {
        try {
            Runnable job = () -> {
                BuildJob build = null;
                try {
                    build = new BuildJob(getJenkinsServer());
                    boolean configured = build.configure(projectBuildConfiguration, true);
                    if (!configured) {
                        throw new AssertionError("Cannot configure build job.");
                    }
                    build.start();
                    onComplete.accept("JOB-ID"); //TODO
                } catch (BuildDriverException e) {
                    onError.accept(e);
                }
            };
            executor.execute(job);
        } catch (Exception e) {
            onError.accept(e);
        }
    }

}
