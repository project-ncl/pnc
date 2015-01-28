package org.jboss.pnc.jenkinsbuilddriver;

import java.net.URI;
import java.net.URISyntaxException;

import javax.inject.Inject;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.JenkinsBuildDriverModuleConfig;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;

import com.offbytwo.jenkins.JenkinsServer;

/**
 * Factory to produce Jenkins server connection.
 * Each server configuration (each build) has its own factory.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public class JenkinsServerFactory {

    @Inject
    Configuration<JenkinsBuildDriverModuleConfig> configuration;

    JenkinsServer getJenkinsServer() throws BuildDriverException {
        try {
            JenkinsBuildDriverModuleConfig config = configuration.getModuleConfig(JenkinsBuildDriverModuleConfig.class);

            String url = config.getUrl();
            String username = config.getUsername();
            String password = config.getPassword();

            if (url == null || username == null || password == null) {
                throw new BuildDriverException("Missing config to instantiate " + JenkinsBuildDriver.DRIVER_ID + ".");
            }

            return new JenkinsServer(new URI(url), username, password);
        } catch (URISyntaxException e) {
            throw new BuildDriverException("Cannot instantiate " + JenkinsBuildDriver.DRIVER_ID + ".", e);
        } catch (ConfigurationParseException e) {
            throw new BuildDriverException("Cannot read configuration for " + JenkinsBuildDriver.DRIVER_ID + ".", e);        }
    }
}
