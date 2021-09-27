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

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REMOVE;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

import java.time.Instant;
import java.util.Map;

import org.jboss.pnc.api.enums.OperationStatus;
import org.jboss.pnc.processor.annotation.PatchSupport;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = Operation.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Operation extends OperationRef {

    /**
     * The user who started this operation.
     */
    protected final User user;

    /**
     * Map of operation input parameters. These parameters are used by the specific operation for the execution.
     */
    @PatchSupport({ ADD, REMOVE, REPLACE })
    protected final Map<String, String> parameters;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true, builderMethodName = "operationBuilder")
    protected Operation(
            User user,
            Map<String, String> parameters,
            String id,
            Instant startTime,
            Instant endTime,
            OperationStatus status) {
        super(id, startTime, endTime, status);
        this.user = user;
        this.parameters = parameters;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Builder {
    }

}
