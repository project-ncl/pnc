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
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;

import java.util.Date;
import java.util.EnumSet;
import java.util.Map;

/**
 * The report of the deliverable analysis.
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliverableAnalyzerReport implements DTOEntity {

    /**
     * ID of the report (which is in fact the same as the ID of the deliverable analyzer operation by which this report
     * was done).
     */
    String id;

    /**
     * The time when the deliverable analysis was submitted.
     */
    Date submitTime;

    /**
     * The time when the deliverable analysis was started.
     */
    Date startTime;

    /**
     * The time when the deliverable analysis finished.
     */
    Date endTime;

    /**
     * The user who started the analysis.
     */
    User user;

    /**
     * Map of operation input parameters. These parameters are used by the specific operation for the execution.
     */
    Map<String, String> parameters;

    /**
     * The product milestone on which was the deliverable analysis run (if any).
     */
    ProductMilestoneRef productMilestone;

    /**
     * Set of active labels of this report.
     */
    EnumSet<DeliverableAnalyzerReportLabel> labels;
}
