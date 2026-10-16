/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto;

import java.util.Date;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.api.enums.LabelOperation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
     * Holds the information whether the {@link DeliverableAnalyzerReportLabel} is added (removed) to (from) this entry.
     */
    LabelOperation change;

    /**
     * The reason of the change.
     */
    String reason;

    /**
     * The user who triggered the change.
     */
    User user;
}
