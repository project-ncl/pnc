/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import java.util.EnumSet;

import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;

@FunctionalInterface
public interface DeliverableAnalyzerReportLabelUpdateFunction {

    void accept(DeliverableAnalyzerReportLabel label, EnumSet<DeliverableAnalyzerReportLabel> activeLabels);
}
