/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import javax.validation.constraints.NotBlank;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.validation.constraints.SCMUrl;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Request to create new build config and SCM repository config given by SCM URL. If the URL is for external SCM, new
 * internal repository will be created and synced.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BuildConfigWithSCMRequest.Builder.class)
public class BuildConfigWithSCMRequest {

    /**
     * The SCM repository URL. The URL can be internal or external.
     */
    @NotBlank
    @SCMUrl
    private final String scmUrl;

    /**
     * Pre-builds sync enablement flag. Is taken into account only when scmUrl contains an external URL. Defaults to
     * true.
     */
    private final Boolean preBuildSyncEnabled;

    /**
     * The build config to be created.
     */
    private final BuildConfiguration buildConfig;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
