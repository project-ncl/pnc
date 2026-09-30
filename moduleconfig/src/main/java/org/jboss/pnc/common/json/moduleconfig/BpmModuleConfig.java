/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import java.net.MalformedURLException;
import java.util.Objects;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

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
        // default to 5 sec
        this.httpConnectionRequestTimeout = Objects.requireNonNullElse(httpConnectionRequestTimeout, 5000);
        // default to 5 sec
        this.httpConnectTimeout = Objects.requireNonNullElse(httpConnectTimeout, 5000);
        // default to 5 sec
        this.httpSocketTimeout = Objects.requireNonNullElse(httpSocketTimeout, 5000);
        this.bpmNewBaseUrl = bpmNewBaseUrl;
        this.bpmNewDeploymentId = bpmNewDeploymentId;
        this.bpmNewBuildProcessName = bpmNewBuildProcessName;
        this.bpmNewUsername = bpmNewUsername;
        this.bpmNewPassword = bpmNewPassword;
        this.newBcCreationProcessId = newBcCreationProcessId;
        this.bpmNewReleaseProcessId = bpmNewReleaseProcessId;
    }

}
