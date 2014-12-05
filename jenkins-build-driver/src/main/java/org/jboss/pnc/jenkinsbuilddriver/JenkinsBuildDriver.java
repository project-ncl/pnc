package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.TaskStatus;
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
public class JenkinsBuildDriver implements BuildDriver {

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
//FIXME: NullPointerException thrown, see BuildTest#shouldTriggerBuildAndFinishWithoutProblems
//TODO check with missing config
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
    public boolean startProjectBuild(ProjectBuildConfiguration projectBuildConfiguration,
                                     RepositoryConfiguration repositoryConfiguration,
                                     Consumer<TaskStatus> onUpdate) {
        //FIXME: NullPointerException thrown, see BuildTest#shouldTriggerBuildAndFinishWithoutProblems
        //TODO check with missing config
        Runnable projectBuild = () -> {
                try {
                    BuildJob build = new BuildJob(getJenkinsServer());
                    boolean configured = build.configure(projectBuildConfiguration, repositoryConfiguration, true);
                    if (!configured) {
                        throw new AssertionError("Cannot configure build job.");
                    }
                    build.start();
                    onUpdate.accept(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, 0));
                } catch (BuildDriverException e) {
                    onUpdate.accept(new TaskStatus(TaskStatus.Operation.BUILD_SCHEDULED, -1));
                }
        };
        //TODO use thread pool, return false if there are no available executors
        //new Thread(projectBuild).start();
        projectBuild.run();
        return true;
    }

    @Override
    public boolean canBuild(BuildType buildType) {
        return BuildType.JAVA.equals(buildType);
    }



}
