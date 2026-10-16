/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.Null;

import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Attachment.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment extends AttachmentRef {

    /**
     * Build this attachment is attached to
     */
    @Null(groups = { WhenImporting.class })
    @RefHasId(groups = { WhenCreatingNew.class, WhenUpdating.class })
    private final BuildRef build;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private Attachment(
            String id,
            String name,
            String description,
            String sha256,
            String url,
            Instant creationTime,
            AttachmentType type,
            BuildRef build) {
        super(id, name, description, sha256, url, creationTime, type);
        this.build = build;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}