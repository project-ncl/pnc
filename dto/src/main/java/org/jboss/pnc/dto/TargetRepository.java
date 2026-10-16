/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.RepositoryType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Contains information related to a repository of build artifacts (i.e. Maven, NPM, etc).
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder", toBuilder = true)
@JsonDeserialize(builder = TargetRepository.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetRepository implements DTOEntity {
    /**
     * ID of the target repository.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = { WhenCreatingNew.class, WhenImporting.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final String id;

    /**
     * Is the reposiotry for temporary builds?
     */
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class, WhenImporting.class })
    protected final Boolean temporaryRepo;

    /**
     * Identifier to link repository configurations (eg. hostname)
     */
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class, WhenImporting.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final String identifier;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc). This field determines the format of the
     * identifier string.
     */
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class, WhenImporting.class })
    protected final RepositoryType repositoryType;

    /**
     * Path that needs to be appended to the hostname eg. "ga" for https://maven.repository.redhat.com/ga/ or "maven2"
     * for https://repo1.maven.org/maven2/ or "" (empty string) when the repository content starts at root
     */
    @NotNull(groups = { WhenUpdating.class, WhenCreatingNew.class, WhenImporting.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final String repositoryPath;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
