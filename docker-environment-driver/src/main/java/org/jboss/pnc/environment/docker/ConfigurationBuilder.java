/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
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
        String template = loadConfigTemplate("jenkins-maven-config.xml");

        Properties propertiesToReplace = new Properties();
        propertiesToReplace.setProperty("dependencyUrl", dependencyUrl);
        propertiesToReplace.setProperty("deployUrl", deployUrl);

        return StringPropertyReplacer.replaceProperties(template, propertiesToReplace);
    }

    /**
     * Loads configuration template
     * 
     * @param fileName Name of default template file name
     * @return Loaded template
     * @throws EnvironmentDriverException Thrown if template cannot be loaded
     */
    private String loadConfigTemplate(String fileName)
            throws EnvironmentDriverException {
        try {
            return IoUtils.readResource(fileName, getClass().getClassLoader());
        } catch (IOException e) {
            throw new EnvironmentDriverException("Cannot load Maven config template.", e);
        }
    }
}
