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

import lombok.extern.slf4j.Slf4j;
import org.jboss.pnc.api.deliverablesanalyzer.dto.AnalysisResult;
import org.jboss.pnc.api.dto.Result;
import org.jboss.pnc.api.enums.ResultStatus;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.facade.deliverables.DeliverableAnalyzerManagerImpl;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.rest.endpoints.internal.api.DeliverableAnalysisEndpoint;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

@ApplicationScoped
@Slf4j
public class DeliverableAnalysisEndpointImpl implements DeliverableAnalysisEndpoint {

    @Inject
    private DeliverableAnalyzerManagerImpl resultProcessor;

    @Inject
    private DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    @Resource
    private ManagedExecutorService executorService;

    @Override
    public void completeAnalysis(AnalysisResult response) {
        executorService.execute(() -> {
            ResultStatus result;
            try {
                resultProcessor.completeAnalysis(transformToModelAnalysisResult(response));
                result = ResultStatus.SUCCESS;
            } catch (RuntimeException e) {
                log.error("Storing results of deliverable operation with id={} failed: ", response.getOperationId(), e);
                result = ResultStatus.SYSTEM_ERROR;
            }

            HttpUtils.performHttpRequest(
                    response.getCallback(),
                    new Result(result),
                    Optional.of(keycloakServiceClient.getAuthToken()));
        });
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
