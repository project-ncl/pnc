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
package org.jboss.pnc.facade;

import org.jboss.pnc.api.deliverablesanalyzer.dto.Build;
import org.jboss.pnc.api.deliverablesanalyzer.dto.FinderResult;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.User;

import java.util.Collection;
import java.util.List;

public interface DeliverableAnalyzerManager {
    /**
     * Start an analysis of deliverables for given milestones. The deliverables are provided as links to archives.
     * 
     * @param id The milestone id.
     * @param deliverablesUrls List of URLs to deliverable archives.
     * @return Operation started for the analysis.
     */
    DeliverableAnalyzerOperation analyzeDeliverables(String id, List<String> deliverablesUrls);

    /**
     * Processes the result of anylysis of delivarables and stores the artifacts as distributed artifacts of Product
     * Milestone.
     *
     * @param milestoneId Id of the milestone to which the distributed artifact will be stored.
     * @param results List of the build finder results.
     */
    void completeAnalysis(int milestoneId, List<FinderResult> results);

    /**
     * Clear the milestone of all delivered artifacts.
     *
     * @param milestoneId Id of the milestone which should be cleared.
     */
    void clear(int milestoneId);
}
