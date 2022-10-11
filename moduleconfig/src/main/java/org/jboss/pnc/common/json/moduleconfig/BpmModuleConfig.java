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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.net.MalformedURLException;

@ToString
public class BpmModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "bpm-config";

    @Getter
    private int httpConnectionRequestTimeout;

    @Getter
    private int httpConnectTimeout;

    @Getter
    private int httpSocketTimeout;

    @Getter
    private final String bpmNewBaseUrl;

    @Getter
    private final String bpmNewDeploymentId;

    @Getter
    private final String bpmNewBuildProcessName;

    @Getter
    private final String bpmNewUsername;

    @Getter
    private final String bpmNewPassword;

    @Getter
    private final String newBcCreationProcessId;

    /** process id when closing a milestone */
    @Getter
    private final String bpmNewReleaseProcessId;

    @Getter
    private final String analyzeDeliverablesBpmProcessId;

    public BpmModuleConfig(
            @JsonProperty("connectionRequestTimeout") Integer httpConnectionRequestTimeout,
            @JsonProperty("connectTimeout") Integer httpConnectTimeout,
            @JsonProperty("socketTimeout") Integer httpSocketTimeout,
            @JsonProperty("bpmNewBaseUrl") String bpmNewBaseUrl,
            @JsonProperty("bpmNewDeploymentId") String bpmNewDeploymentId,
            @JsonProperty("bpmNewBuildProcessName") String bpmNewBuildProcessName,
            @JsonProperty("bpmNewUsername") String bpmNewUsername,
            @JsonProperty("bpmNewPassword") String bpmNewPassword,
            @JsonProperty("newBcCreationProcessId") String newBcCreationProcessId,
            @JsonProperty("bpmNewReleaseProcessId") String bpmNewReleaseProcessId,
            @JsonProperty("analyzeDeliverablesBpmProcessId") String analyzeDeliverablesBpmProcessId)
            throws MalformedURLException {
        this.analyzeDeliverablesBpmProcessId = analyzeDeliverablesBpmProcessId;
        if (httpConnectionRequestTimeout == null) {
            this.httpConnectionRequestTimeout = 5000; // default to 5 sec
        } else {
            this.httpConnectionRequestTimeout = httpConnectionRequestTimeout;
        }
        if (httpConnectTimeout == null) {
            this.httpConnectTimeout = 5000; // default to 5 sec
        } else {
            this.httpConnectTimeout = httpConnectTimeout;
        }
        if (httpSocketTimeout == null) {
            this.httpSocketTimeout = 5000; // default to 5 sec
        } else {
            this.httpSocketTimeout = httpSocketTimeout;
        }
        this.bpmNewBaseUrl = bpmNewBaseUrl;
        this.bpmNewDeploymentId = bpmNewDeploymentId;
        this.bpmNewBuildProcessName = bpmNewBuildProcessName;
        this.bpmNewUsername = bpmNewUsername;
        this.bpmNewPassword = bpmNewPassword;
        this.newBcCreationProcessId = newBcCreationProcessId;
        this.bpmNewReleaseProcessId = bpmNewReleaseProcessId;
    }

}
