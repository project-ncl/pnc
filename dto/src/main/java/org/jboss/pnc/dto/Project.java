/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.util.Map;

import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * A PNC project is something that can be thought of as an upstream (or internal) scm repository (e.g. GitHub).
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Project.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Project extends ProjectRef {

    /**
     * List of build configs in this project.
     */
    @PatchSupport({ ADD, REPLACE })
    private final Map<String, BuildConfigurationRef> buildConfigs;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Project(
            Map<String, BuildConfigurationRef> buildConfigs,
            String id,
            String name,
            String description,
            String issueTrackerUrl,
            String projectUrl,
            String engineeringTeam,
            String technicalLeader) {
        super(id, name, description, issueTrackerUrl, projectUrl, engineeringTeam, technicalLeader);
        this.buildConfigs = buildConfigs;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
