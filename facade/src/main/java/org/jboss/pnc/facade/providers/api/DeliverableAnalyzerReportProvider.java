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
