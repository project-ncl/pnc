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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.util.StringUtils;

import java.net.MalformedURLException;

@ToString
public class BpmModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "bpm-config";

    /**
     * Username to authenticate against remote BPM server for build signal callbacks
     */
    @Getter
    @Setter
    private String username;

    /**
     * Password to authenticate against remote BPM server for build signal callbacks
     */
    @Getter
    @Setter
    private String password;

    @Deprecated
    private final String jenkinsBaseUrl;

    @Getter
    private String deploymentId;

    @Getter
    private String componentBuildProcessId;

    private String releaseProcessId;

    @Getter
    private String bcCreationProcessId;

    @Getter
    private String communityBuild;

    @Getter
    private String versionAdjust;

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

    @Getter
    private final boolean newBpmForced;

    public BpmModuleConfig(
            @JsonProperty("username") String username,
            @JsonProperty("password") String password,
            @JsonProperty("deploymentId") String deploymentId,
            @JsonProperty("componentBuildProcessId") String componentBuildProcessId,
            @JsonProperty("releaseProcessId") String releaseProcessId,
            @JsonProperty("bcCreationProcessId") String bcCreationProcessId,
            @JsonProperty("jenkinsBaseUrl") String jenkinsBaseUrl,
            @JsonProperty("communityBuild") String communityBuild,
            @JsonProperty("versionAdjust") String versionAdjust,
            @JsonProperty("connectionRequestTimeout") Integer httpConnectionRequestTimeout,
            @JsonProperty("connectTimeout") Integer httpConnectTimeout,
            @JsonProperty("socketTimeout") Integer httpSocketTimeout,
            @JsonProperty("newBpmForced") String newBpmForced,
            @JsonProperty("bpmNewBaseUrl") String bpmNewBaseUrl,
            @JsonProperty("bpmNewDeploymentId") String bpmNewDeploymentId,
            @JsonProperty("bpmNewBuildProcessName") String bpmNewBuildProcessName,
            @JsonProperty("bpmNewUsername") String bpmNewUsername,
            @JsonProperty("bpmNewPassword") String bpmNewPassword,
            @JsonProperty("newBcCreationProcessId") String newBcCreationProcessId,
            @JsonProperty("bpmNewReleaseProcessId") String bpmNewReleaseProcessId,
            @JsonProperty("analyzeDeliverablesBpmProcessId") String analyzeDeliverablesBpmProcessId)
            throws MalformedURLException {
        this.username = username;
        this.password = password;
        this.deploymentId = deploymentId;
        this.componentBuildProcessId = componentBuildProcessId;
        this.releaseProcessId = releaseProcessId;
        this.bcCreationProcessId = bcCreationProcessId;
        this.jenkinsBaseUrl = jenkinsBaseUrl;
        this.communityBuild = communityBuild;
        this.versionAdjust = versionAdjust;
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
        if (StringUtils.isEmpty(newBpmForced)) {
            this.newBpmForced = false;
        } else {
            this.newBpmForced = Boolean.parseBoolean(newBpmForced);
        }
        this.bpmNewBaseUrl = bpmNewBaseUrl;
        this.bpmNewDeploymentId = bpmNewDeploymentId;
        this.bpmNewBuildProcessName = bpmNewBuildProcessName;
        this.bpmNewUsername = bpmNewUsername;
        this.bpmNewPassword = bpmNewPassword;
        this.newBcCreationProcessId = newBcCreationProcessId;
        this.bpmNewReleaseProcessId = bpmNewReleaseProcessId;
    }

    public String getMilestoneReleaseProcessId() {
        return releaseProcessId;
    }

    @Deprecated
    public String getJenkinsBaseUrl() {
        return jenkinsBaseUrl;
    }
}
