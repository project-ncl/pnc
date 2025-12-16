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
package org.jboss.pnc.rest.endpoints;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jboss.pnc.api.dto.OperationOutcome;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.OperationRef;
import org.jboss.pnc.dto.requests.ScratchDeliverablesAnalysisRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.DeliverableAnalyzerManager;
import org.jboss.pnc.facade.OperationsManager;
import org.jboss.pnc.facade.providers.api.BuildPushOperationProvider;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerOperationProvider;
import org.jboss.pnc.mapper.api.OperationMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.rest.api.endpoints.OperationEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OperationEndpointImpl implements OperationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OperationEndpointImpl.class);

    @Inject
    private OperationsManager operationsManager;

    @Inject
    private DeliverableAnalyzerOperationProvider delAnalyzerOperationProvider;

    @Inject
    private BuildPushOperationProvider buildPushOperationProvider;

    @Inject
    private DeliverableAnalyzerManager deliverableAnalyzerManager;

    private EndpointHelper<Base32LongID, DeliverableAnalyzerOperation, OperationRef> delAnalyzerEndpointHelper;
    private EndpointHelper<Base32LongID, BuildPushOperation, OperationRef> buildPushEndpointHelper;

    @PostConstruct
    public void init() {
        delAnalyzerEndpointHelper = new EndpointHelper<>(
                DeliverableAnalyzerOperation.class,
                delAnalyzerOperationProvider);
        buildPushEndpointHelper = new EndpointHelper<>(BuildPushOperation.class, buildPushOperationProvider);
    }

    @Override
    public void finish(String id, OperationOutcome operationOutcome) {
        operationsManager.setResult(OperationMapper.idMapper.toEntity(id), operationOutcome);
    }

    @Override
    public DeliverableAnalyzerOperation updateDeliverableAnalyzer(
            String id,
            @NotNull DeliverableAnalyzerOperation operation) {
        return delAnalyzerEndpointHelper.update(id, operation);
    }

    @Override
    public DeliverableAnalyzerOperation getSpecificDeliverableAnalyzer(String id) {
        return delAnalyzerEndpointHelper.getSpecific(id);
    }

    @Override
    public Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperation(@Valid PageParameters pageParams) {
        logger.debug("Retrieving deliverable analyzer operations with these " + pageParams.toString());
        return delAnalyzerEndpointHelper.getAll(pageParams);
    }

    @Override
    public DeliverableAnalyzerOperation startScratchDeliverableAnalysis(
            ScratchDeliverablesAnalysisRequest scratchDeliverablesAnalysisRequest) {
        return deliverableAnalyzerManager
                .analyzeDeliverables(null, scratchDeliverablesAnalysisRequest.getDeliverablesUrls(), true);
    }

    @Override
    public BuildPushOperation getSpecificBuildPush(String id) {
        return buildPushEndpointHelper.getSpecific(id);
    }

    @Override
    public Page<BuildPushOperation> getAllBuildPushOperation(PageParameters pageParams) {
        return buildPushEndpointHelper.getAll(pageParams);
    }
}
