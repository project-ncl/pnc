/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

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

import lombok.extern.slf4j.Slf4j;

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
                                        "There is an internal system error (ID: %s), please contact PNC team at #forum-pnc-users",
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
