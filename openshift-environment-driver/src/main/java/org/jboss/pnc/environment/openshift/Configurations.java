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

package org.jboss.pnc.environment.openshift;

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public enum Configurations {

    PNC_BUILDER_POD("pnc-builder-pod.json"),
    PNC_BUILDER_SERVICE("pnc-builder-service.json"),
    PNC_BUILDER_ROUTE("pnc-builder-route.json"),
    PNC_BUILDER_SSH_SERVICE("pnc-builder-ssh-service.json");

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private static final String CONFIGURATIONS_FOLDER = "openshift.configurations/";

    private final String filePath;

    private Configuration configuration;

    Configurations(String fileName) {
        this.filePath = CONFIGURATIONS_FOLDER + fileName;
        this.configuration = new Configuration();
    }

    public String getContentAsString() {

        String content = getContentFromConfigFile();

        // if no configuration in pnc-config
        if (content == null) {
            try {
                content = IoUtils.readResource(filePath, Configurations.class.getClassLoader());
            } catch(IOException e){
                logger.error("Cannot read configuration file " + filePath, e);
                throw new RuntimeException("Could not read configuration file " + filePath + ": " + e.getMessage());
            }
        }
        return content;
    }

    private String getContentFromConfigFile() {

        OpenshiftBuildAgentConfig config = null;

        try {
            config = configuration.getModuleConfig(new PncConfigProvider<>(OpenshiftBuildAgentConfig.class));
        } catch (ConfigurationParseException e) {
            logger.warn("Could not parse openshift-build-agent config");
            logger.warn("Either the config is absent, or there's a mistake in the config file");
        }

        String content = null;
        // read from pnc-config file
        if (config != null) {
            switch (this) {
                case PNC_BUILDER_POD:
                    content = config.getBuilderPod();
                    break;
                case PNC_BUILDER_SERVICE:
                    content = config.getPncBuilderService();
                    break;
                case PNC_BUILDER_ROUTE:
                    content = config.getPncBuilderRoute();
                    break;
                case PNC_BUILDER_SSH_SERVICE:
                    content = config.getPncBuilderSshRoute();
                    break;
            }
        }
        return content;
    }
}
