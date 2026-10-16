/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.time.Instant;

import javax.validation.constraints.NotNull;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.MilestoneCloseStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * Result of the milestone close operation.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@Builder(builderClassName = "Builder", builderMethodName = "refBuilder")
@JsonDeserialize(builder = ProductMilestoneCloseResultRef.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductMilestoneCloseResultRef implements DTOEntity {

    /**
     * ID of the close attempt.
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * Status of the close attempt.
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected MilestoneCloseStatus status;

    /**
     * The time when the close operation started.
     */
    @NotNull(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected Instant startingDate;

    /**
     * The time when the close operation ended.
     */
    protected Instant endDate;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
