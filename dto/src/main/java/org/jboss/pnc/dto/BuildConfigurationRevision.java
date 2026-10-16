/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;
import java.util.Map;

import org.jboss.pnc.enums.BuildType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class is used to maintain an audit trail of modifications made to a Build Config. Each instance represents a
 * specific revision of a build config.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = BuildConfigurationRevision.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildConfigurationRevision extends BuildConfigurationRevisionRef {

    /**
     * SCM repository where the build's sources are stored.
     */
    private final SCMRepository scmRepository;

    /**
     * The project which the build config is part of.
     */
    private final ProjectRef project;

    /**
     * Build environment that the build will be run in.
     */
    private final Environment environment;

    /**
     * Map of build parameters. These parameters can influence various parts of the build like alignment phase or
     * builder pod memory available.
     */
    private final Map<String, String> parameters;

    /**
     * User who created the build config.
     */
    private final User creationUser;

    /**
     * User who last modified the build config.
     */
    private final User modificationUser;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private BuildConfigurationRevision(
            SCMRepository scmRepository,
            ProjectRef project,
            Environment environment,
            Map<String, String> parameters,
            String id,
            Integer rev,
            String name,
            String buildScript,
            String scmRevision,
            Instant creationTime,
            Instant modificationTime,
            BuildType buildType,
            User creationUser,
            User modificationUser,
            String defaultAlignmentParams,
            boolean brewPullActive) {
        super(
                id,
                rev,
                name,
                buildScript,
                scmRevision,
                creationTime,
                modificationTime,
                buildType,
                defaultAlignmentParams,
                brewPullActive);
        this.scmRepository = scmRepository;
        this.project = project;
        this.environment = environment;
        this.parameters = parameters;
        this.creationUser = creationUser;
        this.modificationUser = modificationUser;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
