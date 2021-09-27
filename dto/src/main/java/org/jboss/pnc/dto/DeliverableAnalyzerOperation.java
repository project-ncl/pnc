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

import java.time.Instant;
import java.util.Map;

import org.jboss.pnc.api.enums.OperationStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = DeliverableAnalyzerOperation.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliverableAnalyzerOperation extends Operation {

    /**
     * The product milestone for which this deliverable analyzer operation was performed.
     */
    protected final ProductMilestoneRef productMilestone;

    @lombok.Builder(builderClassName = "DeliverableAnalyzerBuilder", toBuilder = true)
    private DeliverableAnalyzerOperation(
            User user,
            Map<String, String> parameters,
            String id,
            Instant startTime,
            Instant endTime,
            OperationStatus status,
            ProductMilestoneRef productMilestone) {
        super(user, parameters, id, startTime, endTime, status);
        this.productMilestone = productMilestone;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeliverableAnalyzerBuilder extends Builder {
    }

}
