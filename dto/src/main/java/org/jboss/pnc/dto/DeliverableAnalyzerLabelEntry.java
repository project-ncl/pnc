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

@Value
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class DeliverableAnalyzerLabelEntry {

    /**
     * The {@link DeliverableAnalyzerReportLabel} assigned to this label entry.
     */
    DeliverableAnalyzerReportLabel label;

    /**
     * The date of the change.
     */
    Date date;

    /**
     * The reason of the change.
     */
    String reason;

    /**
     * The user who triggered the change.
     */
    User user;
}
