/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;
import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.time.Instant;

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

    @Size(max = 32)
    @NotNull(groups = { WhenCreatingNew.class, WhenCreatingNew.class, WhenImporting.class })
    protected final String md5;
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
