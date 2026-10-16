/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model.utils;

import java.util.EnumSet;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;

/**
 * The converter between {@link DeliverableAnalyzerReportLabel} and {@link String}.
 */
@Converter(autoApply = true)
public class DeliverableAnalyzerReportLabelToStringConverter
        extends EnumSetToStringConverter<DeliverableAnalyzerReportLabel>
        implements AttributeConverter<EnumSet<DeliverableAnalyzerReportLabel>, String> {

    public DeliverableAnalyzerReportLabelToStringConverter() {
        super(DeliverableAnalyzerReportLabel.class);
    }
}