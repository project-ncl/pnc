/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 * Artifact identifier parsed into its version, type, and optional classifier.
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
@Data
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = ParsedArtifact.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ParsedArtifact {

    /**
     * ID of the artifact.
     */
    protected final String id;

    /**
     * Artifact identifier version part.
     */
    protected final String artifactVersion;

    /**
     * Artifact identifier type part.
     */
    protected final String type;

    /**
     * Artifact identifier classifier part.
     */
    protected final String classifier;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}