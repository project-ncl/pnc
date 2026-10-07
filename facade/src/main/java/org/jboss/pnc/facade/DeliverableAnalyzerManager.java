/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade;

import java.util.List;

import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.facade.deliverables.api.AnalysisResult;

public interface DeliverableAnalyzerManager {
    /**
     * Start an analysis of deliverables for given milestones. The deliverables are provided as links to archives.
     * 
     * @param id The milestone id.
     * @param deliverablesUrls List of URLs to deliverable archives.
     * @param runAsScratchAnalysis Boolean flag whether the analysis should be run as "scratch".
     * @return Operation started for the analysis.
     */
    DeliverableAnalyzerOperation analyzeDeliverables(
            String id,
            List<String> deliverablesUrls,
            boolean runAsScratchAnalysis);

    /**
     * Processes the result of anylysis of delivarables and stores the artifacts as distributed artifacts of Product
     * Milestone.
     *
     * @param analysisResult The result of the deliverable analyzer operation.
     */
    void completeAnalysis(AnalysisResult analysisResult);
}
