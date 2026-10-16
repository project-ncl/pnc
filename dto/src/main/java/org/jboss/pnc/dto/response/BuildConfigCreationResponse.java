/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import org.jboss.pnc.dto.BuildConfiguration;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 * The result of Create&Sync call for creating build config with scm url. If the SCM repository config can be created
 * immediately, new build config will be also created immediately and returned by {@link #getBuildConfig()} property. If
 * the repository needs to be synchronized first, {@link #getTaskId()} property provides id of the synchronization task.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = BuildConfigCreationResponse.Builder.class)
public class BuildConfigCreationResponse {

    /**
     * Id of the task that will create and sync the repository and create build config. When the repository doesn't
     * require sync, this is null and {@link #getBuildConfig()} is returned instead.
     */
    private String taskId;

    /**
     * The created build config. When the repository require sync, this is null and {@link #getTaskId()} is returned
     * instead.
     */
    private BuildConfiguration buildConfig;

    public BuildConfigCreationResponse(BuildConfiguration buildConfiguration) {
        this.buildConfig = buildConfiguration;
    }

    public BuildConfigCreationResponse(String taskId) {
        this.taskId = taskId;
    }

    @lombok.Builder(builderClassName = "Builder")
    private BuildConfigCreationResponse(String taskId, BuildConfiguration buildConfig) {
        this.taskId = taskId;
        this.buildConfig = buildConfig;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
