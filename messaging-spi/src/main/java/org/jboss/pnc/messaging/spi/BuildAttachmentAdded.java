/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging.spi;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Attachment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = BuildAttachmentAdded.Builder.class)
public class BuildAttachmentAdded implements Message {
    public static final String ATTRIBUTE = "build-attachment-added-event";

    private final Attachment newAttachment;

    @Override
    public String toJson() {
        return JsonOutputConverterMapper.apply(this);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
