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
package org.jboss.pnc.common.json.moduleconfig;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Runtime configuration parameters for the Build Agent
 *
 * @author Alex Creasy
 */
@JsonIgnoreProperties({ "@module-config" })
public class OpenshiftBuildAgentConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "openshift-build-agent";

    private final JsonNode pncBuilderPod;
    private final JsonNode pncBuilderService;
    private final JsonNode pncBuilderRoute;
    private final JsonNode pncBuilderSshRoute;

    public OpenshiftBuildAgentConfig(
            @JsonProperty("pncBuilderPod") JsonNode pncBuilderPod,
            @JsonProperty("pncBuilderService") JsonNode pncBuilderService,
            @JsonProperty("pncBuilderRoute") JsonNode pncBuilderRoute,
            @JsonProperty("pncBuilderSshRoute") JsonNode pncBuilderSshRoute) {
        this.pncBuilderPod = pncBuilderPod;
        this.pncBuilderService = pncBuilderService;
        this.pncBuilderRoute = pncBuilderRoute;
        this.pncBuilderSshRoute = pncBuilderSshRoute;
    }

    public String getBuilderPod() {
        if (pncBuilderPod.isNull()) {
            return null;
        }
        return pncBuilderPod.toString();
    }

    public String getPncBuilderService() {
        if (pncBuilderService.isNull()) {
            return null;
        }
        return pncBuilderService.toString();
    }

    public String getPncBuilderRoute() {
        if (pncBuilderRoute.isNull()) {
            return null;
        }
        return pncBuilderRoute.toString();
    }

    public String getPncBuilderSshRoute() {
        if (pncBuilderSshRoute.isNull()) {
            return null;
        }
        return pncBuilderSshRoute.toString();
    }
}
