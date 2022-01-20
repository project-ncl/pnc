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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.enums.MilestoneCloseStatus;
import org.jboss.pnc.processor.annotation.PatchSupport;

import java.time.Instant;
import java.util.List;

/**
 * Result of the milestone close operation.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ProductMilestoneCloseResult.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductMilestoneCloseResult extends ProductMilestoneCloseResultRef {

    /**
     * Mileston that was being closed.
     */
    private final ProductMilestoneRef milestone;

    /**
     * List of results of builds being pushed to Koji.
     */
    private final List<BuildPushResultRef> buildPushResults;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ProductMilestoneCloseResult(
            ProductMilestoneRef milestone,
            List<BuildPushResultRef> buildPushResults,
            String id,
            Instant startingDate,
            Instant endDate,
            MilestoneCloseStatus status) {
        super(id, status, startingDate, endDate);
        this.milestone = milestone;
        this.buildPushResults = buildPushResults;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
