/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import java.util.List;

import org.jboss.pnc.dto.BuildConfigurationRevisionRef;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Request to start build of a group config.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = GroupBuildRequest.Builder.class)
public class GroupBuildRequest {

    /**
     * List of group config revisions overrides to be used for builds. Normally the build of group config will start
     * building all the build configs in the group in their latest revision. This list can be used to override this
     * behaviour and specify which revisions to build exactly. All the revisions should be of build configs in the
     * group, but not all build configs from the group must have specified revision (latest will be used).
     */
    private final List<BuildConfigurationRevisionRef> buildConfigurationRevisions;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
    }
}
