/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.requests.labels;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * Label request for {@link DeliverableAnalyzerReportLabel} entity.
 */
@SuperBuilder(toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeliverableAnalyzerReportLabelRequest extends GenericLabelRequest<DeliverableAnalyzerReportLabel> {

}
