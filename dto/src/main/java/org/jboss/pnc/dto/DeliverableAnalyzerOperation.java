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

import java.time.Instant;
import java.util.Map;

import lombok.experimental.SuperBuilder;
import org.jboss.pnc.api.enums.OperationResult;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.ProgressStatus;

@Data
@Jacksonized
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuperBuilder(builderMethodName = "delAnalyzerBuilder", toBuilder = true)
public class DeliverableAnalyzerOperation extends Operation {

    /**
     * The product milestone for which this deliverable analyzer operation was performed.
     */
    protected final ProductMilestoneRef productMilestone;
}
