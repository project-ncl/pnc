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
package org.jboss.pnc.rest.endpoints.internal;

import org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult;
import org.jboss.pnc.facade.deliverables.DeliverableAnalyzerManagerImpl;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.rest.endpoints.internal.api.DeliverableAnalysisEndpoint;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DeliverableAnalysisEndpointImpl implements DeliverableAnalysisEndpoint {

    @Inject
    private DeliverableAnalyzerManagerImpl resultProcessor;

    @Inject
    private ProductMilestoneMapper milestoneMapper;

    @Inject
    private DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;

    @Override
    public void completeAnalysis(AnalysisResult response) {
        resultProcessor.completeAnalysis(transformToModelAnalysisResult(response));
    }

    @Override
    public void clearAnalysis(String milestoneId) {
        int id = milestoneMapper.getIdMapper().toEntity(milestoneId);
        resultProcessor.clear(id);
    }

    private org.jboss.pnc.facade.deliverables.api.AnalysisResult transformToModelAnalysisResult(
            AnalysisResult analysisResult) {
        return org.jboss.pnc.facade.deliverables.api.AnalysisResult.builder()
                .deliverableAnalyzerOperationId(
                        deliverableAnalyzerOperationMapper.getIdMapper().toEntity(analysisResult.getOperationId()))
                .results(analysisResult.getResults())
                .wasRunAsScratchAnalysis(analysisResult.isScratch())
                .build();
    }

}
