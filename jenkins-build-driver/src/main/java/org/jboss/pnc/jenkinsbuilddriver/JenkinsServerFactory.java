package org.jboss.pnc.jenkinsbuilddriver;

import com.offbytwo.jenkins.JenkinsServer;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Factory to produce Jenkins server connection.
 * Each server configuration (each build) has its own factory.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public class JenkinsServerFactory {

    @Inject
    Configuration configuration;

    JenkinsServer getJenkinsServer() throws BuildDriverException {
        try {
            Properties properties = configuration.getModuleConfig(JenkinsBuildDriver.DRIVER_ID);

            String url = properties.getProperty("url");
            String username = properties.getProperty("username");
            String password = properties.getProperty("password");

            if (url == null || username == null || password == null) {
                throw new BuildDriverException("Missing config to instantiate " + JenkinsBuildDriver.DRIVER_ID + ".");
            }

            return new JenkinsServer(new URI(url), username, password);
        } catch (URISyntaxException e) {
            throw new BuildDriverException("Cannot instantiate " + JenkinsBuildDriver.DRIVER_ID + ".", e);
        }
    }
}
