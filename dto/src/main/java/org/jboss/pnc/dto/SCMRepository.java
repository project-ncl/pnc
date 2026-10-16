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
import org.jboss.pnc.dto.validation.constraints.SCMUrl;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration of the SCM repository.
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@PatchSupport
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = SCMRepository.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SCMRepository implements DTOEntity {

    /**
     * ID of the SCM Repository.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * URL to the internal SCM repository, which is the main repository used for the builds. New commits can be added to
     * this repository, during the pre-build steps of the build process.
     */
    @NotBlank(groups = { WhenUpdating.class, WhenCreatingNew.class })
    @SCMUrl(groups = { WhenUpdating.class, WhenCreatingNew.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String internalUrl;

    /**
     * URL to the upstream SCM repository.
     */
    @PatchSupport({ REPLACE })
    @SCMUrl(groups = { WhenUpdating.class, WhenCreatingNew.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String externalUrl;

    /**
     * Declares whether the pre-build repository synchronization from external repository should happen or not.
     */
    @PatchSupport({ REPLACE })
    protected final Boolean preBuildSyncEnabled;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
