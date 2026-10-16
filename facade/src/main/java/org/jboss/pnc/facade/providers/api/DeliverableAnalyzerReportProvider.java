/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.dto.DeliverableAnalyzerReport;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.model.Base32LongID;

public interface DeliverableAnalyzerReportProvider extends
        Provider<Base32LongID, org.jboss.pnc.model.DeliverableAnalyzerReport, DeliverableAnalyzerReport, DeliverableAnalyzerReport> {

    Page<AnalyzedArtifact> getAnalyzedArtifacts(int pageIndex, int pageSize, String query, String sort, String id);

    void addLabel(String id, DeliverableAnalyzerReportLabelRequest request);

    void removeLabel(String id, DeliverableAnalyzerReportLabelRequest request);

    Page<DeliverableAnalyzerLabelEntry> getLabelHistory(
            String id,
            int pageIndex,
            int pageSize,
            String sort,
            String query);
}
