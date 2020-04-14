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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.enums.MilestoneReleaseStatus;
import org.jboss.pnc.processor.annotation.PatchSupport;

import java.time.Instant;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ProductMilestoneRelease.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductMilestoneRelease extends ProductMilestoneReleaseRef {

    private ProductMilestoneRef milestone;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ProductMilestoneRelease(
            ProductMilestoneRef milestone,
            String id,
            Instant startingDate,
            Instant endDate,
            MilestoneReleaseStatus status) {
        super(id, status, startingDate, endDate);
        this.milestone = milestone;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
