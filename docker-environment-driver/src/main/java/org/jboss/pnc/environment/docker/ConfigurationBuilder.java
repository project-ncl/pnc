package org.jboss.pnc.environment.docker;

import java.io.IOException;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.common.util.IoUtils;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.util.StringPropertyReplacer;

/**
 * Builds needed configuration
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@ApplicationScoped
public class ConfigurationBuilder {

    /**
     * Creates Maven configuration data based on template.
     * 
     * @param dependencyUrl AProx dependencyUrl
     * @param deployUrl AProx deployUrl
     * @return Prepared configuration data for Maven
     * @throws EnvironmentDriverException Thrown if template cannot be loaded
     */
    public String createMavenConfig(String dependencyUrl, String deployUrl)
            throws EnvironmentDriverException {
        String template = loadConfigTemplate("jenkins-maven-config", "jenkins-maven-config.xml");

        Properties propertiesToReplace = new Properties();
        propertiesToReplace.setProperty("dependencyUrl", dependencyUrl);
        propertiesToReplace.setProperty("deployUrl", deployUrl);

        return StringPropertyReplacer.replaceProperties(template, propertiesToReplace);
    }

    /**
     * Loads configuration template
     * 
     * @param systemPropertyName Name of system property with filename
     * @param fileName Name of default template file name
     * @return Loaded template
     * @throws EnvironmentDriverException Thrown if template cannot be loaded
     */
    private String loadConfigTemplate(String systemPropertyName, String fileName)
            throws EnvironmentDriverException {
        try {
            return IoUtils.readFileOrResource(systemPropertyName, fileName,
                    getClass().getClassLoader());
        } catch (IOException e) {
            throw new EnvironmentDriverException("Cannot load Maven config template.", e);
        }
    }
}
