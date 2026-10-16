/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Delivered Artifact and its versions in Milestones it was delivered in.
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = DeliveredArtifactInMilestones.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliveredArtifactInMilestones {

    /**
     * Artifact identifier without the version part.
     * 
     * For example, Maven: demo:built-artifact:jar:1.0.redhat-a -> demo:built-artifact
     * NPM: @demo/built-artifact:1.0.redhat-a -> @demo/built-artifact
     */
    private final String artifactIdentifierPrefix;

    /**
     * Product Milestone ID mapped to list of Artifact versions delivered in the Milestone with the same identifier
     * prefix.
     */
    private final Map<String, List<ParsedArtifact>> productMilestoneArtifacts;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
