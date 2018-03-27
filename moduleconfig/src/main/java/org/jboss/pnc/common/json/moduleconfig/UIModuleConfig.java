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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;

/**
 * Runtime configuration parameters for the Web UI.
 *
 * @author Alex Creasy
 */
@JsonIgnoreProperties({ "@module-config"})
public class UIModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "ui";

    private final String pncUrl;
    private final String pncNotificationsUrl;
    private final String daUrl;
    private final String userGuideUrl;
    private final KeycloakConfig keycloak;

    public UIModuleConfig(
            @JsonProperty("pncUrl") String pncUrl,
            @JsonProperty("pncNotificationsUrl") String pncNotificationsUrl,
            @JsonProperty("daUrl") String daUrl,
            @JsonProperty("userGuideUrl") String userGuideUrl,
            @JsonProperty("keycloak") KeycloakConfig keycloak) {
        this.pncUrl = pncUrl;
        this.pncNotificationsUrl = pncNotificationsUrl;
        this.daUrl = daUrl;
        this.userGuideUrl = userGuideUrl;
        this.keycloak = keycloak;
    }

    /**
     * @return String representation of the PNC REST API base URL.
     */
    @JsonProperty("pncUrl")
    public String getPncUrl() {
        return pncUrl;
    }

    /**
     * @return String representation of the PNC notification WebSocket URL.
     */
    @JsonProperty("pncNotificationsUrl")
    public String getPncNotificationsUrl() {
        return pncNotificationsUrl;
    }

    /**
     * @return String representation of the Dependency Analyzer API base URL.
     */
    @JsonProperty("daUrl")
    public String getDaUrl() {
        return daUrl;
    }

    /**
     * @return String representation of the PNC user guide URL
     */
    public String getUserGuideUrl() {
        return userGuideUrl;
    }

    /**
     * @return Keycloak object of Web UI configuration parameters for the Keycloak JavaScript adapter.
     */
    @JsonProperty("keycloak")
    public KeycloakConfig getKeycloak() {
        return keycloak;
    }

    @Override
    public String toString() {
        return "UIModuleConfig{" +
                "pncUrl='" + pncUrl + '\'' +
                ", pncNotificationsUrl='" + pncNotificationsUrl + '\'' +
                ", daUrl='" + daUrl + '\'' +
                ", userGuideUrl='" + userGuideUrl + '\'' +
                ", keycloak=" + keycloak +
                '}';
    }

}
