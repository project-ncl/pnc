/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * A PNC project is something that can be thought of as an upstream (or internal) scm repository (e.g. GitHub).
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProjectRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectRef implements DTOEntity {
    /**
     * ID of the project.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Project name. Typically in the form ${organization}/${repository}.
     */
    @PatchSupport({ REPLACE })
    @NotBlank(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String name;

    /**
     * Project description.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String description;

    /**
     * URL of the issue tracker for the project.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String issueTrackerUrl;

    /**
     * URL of the project.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String projectUrl;

    /**
     * The engineering team in charge of the project.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String engineeringTeam;

    /**
     * The technical leader of the project.
     */
    @PatchSupport({ REPLACE })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String technicalLeader;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
