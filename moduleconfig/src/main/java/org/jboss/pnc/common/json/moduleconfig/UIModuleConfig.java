/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.common.util.StringUtils;

import javax.ws.rs.DefaultValue;
import java.util.Map;

/**
 * Runtime configuration parameters for the Web UI.
 *
 * @author Alex Creasy
 */
@JsonIgnoreProperties({ "@module-config" })
public class UIModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "ui";

    private final String pncNotificationsUrl;
    private final String userGuideUrl;
    private final Integer ssoTokenLifespan;
    private final KeycloakConfig keycloak;
    private final Map<String, String> grafana;

    public UIModuleConfig(
            @JsonProperty("pncNotificationsUrl") String pncNotificationsUrl,
            @JsonProperty("userGuideUrl") String userGuideUrl,
            @JsonProperty("ssoTokenLifespan") String ssoTokenLifespan,
            @JsonProperty("keycloak") KeycloakConfig keycloak,
            @JsonProperty("grafana") @DefaultValue("{}") Map<String, String> grafana) {
        this.pncNotificationsUrl = pncNotificationsUrl;
        this.userGuideUrl = userGuideUrl;
        this.ssoTokenLifespan = StringUtils.parseInt(ssoTokenLifespan, 86400000); // default to 24h
        this.keycloak = keycloak;
        this.grafana = grafana;
    }

    /**
     * @return String representation of the PNC notification WebSocket URL.
     */
    @JsonProperty("pncNotificationsUrl")
    public String getPncNotificationsUrl() {
        return pncNotificationsUrl;
    }

    /**
     * @return String representation of the PNC user guide URL
     */
    public String getUserGuideUrl() {
        return userGuideUrl;
    }

    public Integer getSsoTokenLifespan() {
        return ssoTokenLifespan;
    }

    /**
     * @return Keycloak object of Web UI configuration parameters for the Keycloak JavaScript adapter.
     */
    @JsonProperty("keycloak")
    public KeycloakConfig getKeycloak() {
        return keycloak;
    }

    /**
     * @return A map of grafana URLs to widgets embedded in the UI Dashboard
     */
    @JsonProperty("grafana")
    public Map<String, String> getGrafana() {
        return grafana;
    }

    @Override
    public String toString() {
        return "UIModuleConfig{" + ", pncNotificationsUrl='" + pncNotificationsUrl + '\'' + ", userGuideUrl='"
                + userGuideUrl + '\'' + ", ssoTokenLifespan=" + ssoTokenLifespan + ", keycloak=" + keycloak
                + ", grafana=" + grafana + '}';
    }
}
