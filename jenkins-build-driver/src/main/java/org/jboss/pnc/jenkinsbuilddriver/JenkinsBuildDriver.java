package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
import org.jboss.pnc.model.exchange.BuildTaskConfiguration;
import org.jboss.pnc.model.exchange.Task;
import org.jboss.pnc.spi.builddriver.BuildDriver;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.repositorymanager.RepositoryConfiguration;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.function.Consumer;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class JenkinsBuildDriver implements BuildDriver<ProjectBuildConfiguration> {

    private static final String DRIVER_ID = "jenkins-build-driver";

    /**
     * Server instance, use getter for lazy initialization
     */
    private JenkinsServer jenkinsServer;

    @Inject
    Configuration configuration;

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
    public boolean startProjectBuild(Task<BuildTaskConfiguration> buildTask) {
        BuildTaskConfiguration buildConfig = buildTask.getTaskConfiguration();

        ProjectBuildConfiguration projectBuildConfiguration = buildConfig.getProjectBuildConfiguration();

        Runnable projectBuild = () -> {
            try {
                BuildJob build = new BuildJob(getJenkinsServer());
                boolean configured = build.configure(projectBuildConfiguration, true);
                if (!configured) {
                    throw new AssertionError("Cannot configure build job.");
                }
                build.start();
                buildTask.setStatus(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, 0));
            } catch (BuildDriverException e) {
                buildTask.setStatus(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, -1));
            }
        };
        //TODO use thread pool, return false if there are no available executors
        new Thread(projectBuild).start();
        return true;
    }


}
