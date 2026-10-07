/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import javax.validation.constraints.NotBlank;

import org.jboss.pnc.dto.validation.constraints.SCMUrl;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Request to create new SCM repository config with given URL.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = CreateAndSyncSCMRequest.Builder.class)
public class CreateAndSyncSCMRequest {

    /**
     * The SCM repository URL. The URL can be internal or external.
     */
    @NotBlank(groups = { WhenCreatingNew.class })
    @SCMUrl(groups = { WhenCreatingNew.class })
    private final String scmUrl;

    /**
     * Pre-builds sync enablement flag. Is taken into account only when scmUrl contains an external URL. Defaults to
     * true.
     */
    private final Boolean preBuildSyncEnabled;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
