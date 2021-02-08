/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.enums.MilestoneCloseStatus;

import javax.validation.constraints.NotNull;
import java.time.Instant;

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
