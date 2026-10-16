/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import java.util.EnumSet;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.model.utils.DeliverableAnalyzerReportLabelToStringConverter;
import org.junit.Test;

public class DeliverableAnalyzerReportLabelToStringConverterTest extends AbstractModelTest {

    private DeliverableAnalyzerReportLabelToStringConverter converter = new DeliverableAnalyzerReportLabelToStringConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        EnumSet<DeliverableAnalyzerReportLabel> entityLabels = EnumSet
                .of(DeliverableAnalyzerReportLabel.RELEASED, DeliverableAnalyzerReportLabel.SCRATCH);

        var dbLabels = converter.convertToDatabaseColumn(entityLabels);

        Assertions.assertThat(dbLabels).isEqualTo("SCRATCH,RELEASED");
    }

    @Test
    public void testConvertToEntityAttribute() {
        String dbLabels = "SCRATCH,DELETED";

        EnumSet<DeliverableAnalyzerReportLabel> entityLabels = converter.convertToEntityAttribute(dbLabels);

        Assertions.assertThat(entityLabels)
                .isEqualTo(EnumSet.of(DeliverableAnalyzerReportLabel.DELETED, DeliverableAnalyzerReportLabel.SCRATCH));
    }
}