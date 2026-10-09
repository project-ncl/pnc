/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = AttachmentRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttachmentRef implements DTOEntity {

    /**
     * ID of the attachment
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = { WhenCreatingNew.class, WhenImporting.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final String id;

    /**
     * Name of the Attachment
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final String name;

    /**
     * Build config description.
     */
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final String description;

    /**
     * SHA-256 Digest of the Attachment. Used to verify contents in url.
     */
    @Size(max = 64)
    @NotNull(groups = { WhenCreatingNew.class, WhenCreatingNew.class, WhenImporting.class })
    protected final String sha256;

    /**
     * URL pointing to the place where artifact lives
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    @URL(protocol = "https")
    protected final String url;

    /**
     * The time when the attachment was created.
     */
    @Null(groups = { WhenCreatingNew.class, WhenImporting.class })
    protected final Instant creationTime;

    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class, WhenImporting.class })
    protected final AttachmentType type;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
