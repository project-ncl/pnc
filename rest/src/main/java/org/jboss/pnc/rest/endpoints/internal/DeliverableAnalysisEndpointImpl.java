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
import org.jboss.pnc.api.dto.ExceptionResolution;
import org.jboss.pnc.api.dto.Result;
import org.jboss.pnc.api.enums.ResultStatus;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.common.http.HttpUtils;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.facade.deliverables.DeliverableAnalyzerManagerImpl;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.rest.endpoints.internal.api.DeliverableAnalysisEndpoint;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class DeliverableAnalysisEndpointImpl implements DeliverableAnalysisEndpoint {

    @Inject
    private DeliverableAnalyzerManagerImpl resultProcessor;

    @Inject
    private DeliverableAnalyzerOperationMapper deliverableAnalyzerOperationMapper;

    @Inject
    private ServiceAccountClient serviceAccountClient;

    @Inject
    private ManagedExecutorService executorService;

    @Override
    public void completeAnalysis(AnalysisResult response) {
        executorService.execute(() -> {
            ResultStatus resultStatus;
            ExceptionResolution exceptionResolution = null;
            try {
                MDCUtils.addProcessContext(response.getOperationId());
                resultProcessor.completeAnalysis(transformToModelAnalysisResult(response));
                resultStatus = ResultStatus.SUCCESS;
            } catch (RuntimeException e) {
                final String errorId = UUID.randomUUID().toString();
                log.error(
                        "ErrorId={} Storing results of deliverable operation with id={} failed: ",
                        errorId,
                        response.getOperationId(),
                        e);
                resultStatus = ResultStatus.SYSTEM_ERROR;
                exceptionResolution = ExceptionResolution.builder()
                        .reason(
                                String.format(
                                        "Storing results of deliverable operation with id=%s failed",
                                        response.getOperationId()))
                        .proposal(
                                String.format(
                                        "There is an internal system error, please contact PNC team at #forum-pnc-users (with the following ID: %s)",
                                        errorId))
                        .build();
            } finally {
                MDCUtils.removeProcessContext();
            }

            HttpUtils.performHttpRequest(
                    response.getCallback(),
                    new Result(resultStatus, exceptionResolution),
                    Optional.of(serviceAccountClient.getAuthHeaderValue()));
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
