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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;

/**
 * @author Alex Creasy
 */
public class UIModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "ui";

    private final String pncUrl;
    private final String pncNotificationsUrl;
    private final String daUrl;
    private final String daReportsUrl;

    public UIModuleConfig(
            @JsonProperty("pnc-url") String pncUrl,
            @JsonProperty("pnc-notifications-url") String pncNotificationsUrl,
            @JsonProperty("da-url") String daUrl,
            @JsonProperty("da-reports-url") String daReportsUrl) {
        this.pncUrl = pncUrl;
        this.pncNotificationsUrl = pncNotificationsUrl;
        this.daUrl = daUrl;
        this.daReportsUrl = daReportsUrl;
    }

    public String getPncUrl() {
        return pncUrl;
    }

    public String getPncNotificationsUrl() {
        return pncNotificationsUrl;
    }

    public String getDaUrl() {
        return daUrl;
    }

    public String getDaReportsUrl() {
        return daReportsUrl;
    }
}
