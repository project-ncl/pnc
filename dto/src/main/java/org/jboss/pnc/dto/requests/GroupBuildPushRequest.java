/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Request to push builds from group build to Koji.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = GroupBuildPushRequest.Builder.class)
public class GroupBuildPushRequest {

    /**
     * Koji tag prefix, to which the builds should be tagged upon import.
     */
    private final String tagPrefix;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
