/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.json.moduleconfig.OpenshiftBuildAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class Configurations {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    public static String getContentAsString(Resource resource, OpenshiftBuildAgentConfig openshiftBuildAgentConfig) {
        String content = null;
        if (openshiftBuildAgentConfig != null) {
            content = getContentFromConfigFile(resource, openshiftBuildAgentConfig);
        }
        // if no configuration in pnc-config
        if (content == null) {
            content = resource.getDefaultConfiguration();
        }
        return content;
    }

    private static String getContentFromConfigFile(
            Resource resource,
            OpenshiftBuildAgentConfig openshiftBuildAgentConfig) {
        String content = null;
        switch (resource) {
            case PNC_BUILDER_POD:
                content = openshiftBuildAgentConfig.getBuilderPod();
                break;
            case PNC_BUILDER_SERVICE:
                content = openshiftBuildAgentConfig.getPncBuilderService();
                break;
            case PNC_BUILDER_ROUTE:
                content = openshiftBuildAgentConfig.getPncBuilderRoute();
                break;
            case PNC_BUILDER_SSH_SERVICE:
                content = openshiftBuildAgentConfig.getPncBuilderSshRoute();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported resource type: " + resource.toString());
        }
        return content;
    }
}
