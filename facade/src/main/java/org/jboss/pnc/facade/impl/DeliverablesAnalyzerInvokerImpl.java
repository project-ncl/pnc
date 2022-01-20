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
package org.jboss.pnc.facade.impl;

import org.jboss.pnc.api.constants.HttpHeaders;
import org.jboss.pnc.api.constants.MDCHeaderKeys;
import org.jboss.pnc.api.dto.Request;
import org.jboss.pnc.bpm.RestConnector;
import org.jboss.pnc.bpm.model.AnalyzeDeliverablesBpmRequest;
import org.jboss.pnc.bpm.task.AnalyzeDeliverablesTask;
import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.BpmModuleConfig;
import org.jboss.pnc.dto.requests.DeliverablesAnalysisRequest;
import org.jboss.pnc.enums.AnalysisStatus;
import org.jboss.pnc.facade.DeliverablesAnalyzerInvoker;
import org.jboss.pnc.facade.deliverables.DefaultAnalysisStatusChangedEvent;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.deliverables.AnalysisStatusChangedEvent;
import org.jboss.pnc.spi.exception.ProcessManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DeliverablesAnalyzerInvokerImpl implements DeliverablesAnalyzerInvoker {

    private static final Logger log = LoggerFactory.getLogger(DeliverablesAnalyzerInvokerImpl.class);

    private UserService userService;

    private GlobalModuleGroup globalConfig;

    private BpmModuleConfig bpmConfig;

    private String callbackUrlTemplate = "%s/deliverable-analysis/complete";

    private final Event<AnalysisStatusChangedEvent> analysisStatusChangedEventNotifier;

    @Inject
    public DeliverablesAnalyzerInvokerImpl(
            UserService userService,
            GlobalModuleGroup globalConfig,
            BpmModuleConfig bpmConfig,
            Event<AnalysisStatusChangedEvent> analysisStatusChangedEventNotifier) {
        this.userService = userService;
        this.globalConfig = globalConfig;
        this.bpmConfig = bpmConfig;
        this.analysisStatusChangedEventNotifier = analysisStatusChangedEventNotifier;
    }

    @Override
    public void startAnalysis(String milestoneId, DeliverablesAnalysisRequest request) {
        String accessToken = userService.currentUserToken();

        String actualEndpoint = String.format(callbackUrlTemplate, globalConfig.getPncUrl());
        URI callbackURI = URI.create(actualEndpoint);

        List<Request.Header> headers = new ArrayList<>();
        addCommonHeaders(headers, accessToken);
        addMDCHeaders(headers);

        Request callback = new Request(Request.Method.POST, callbackURI, headers);

        try (RestConnector restConnector = new RestConnector(bpmConfig)) {
            AnalyzeDeliverablesBpmRequest bpmRequest = new AnalyzeDeliverablesBpmRequest(
                    milestoneId,
                    request.getSourcesLink(),
                    null);
            AnalyzeDeliverablesTask analyzeTask = new AnalyzeDeliverablesTask(
                    bpmRequest,
                    callback,
                    globalConfig.getDelAnalUrl());

            restConnector.startProcess(bpmConfig.getAnalyzeDeliverablesBpmProcessId(), analyzeTask, accessToken);

            AnalysisStatusChangedEvent analysisStatusChanged = new DefaultAnalysisStatusChangedEvent(
                    AnalysisStatus.STARTED,
                    milestoneId,
                    request.getSourcesLink());
            analysisStatusChangedEventNotifier.fire(analysisStatusChanged);
        } catch (ProcessManagerException e) {
            log.error("Error trying to start analysis of deliverables task for milestone: {}", milestoneId, e);
            throw new RuntimeException(e);
        }
    }

    private void addMDCHeaders(List<Request.Header> headers) {
        headersFromMdc(headers, MDCHeaderKeys.REQUEST_CONTEXT);
        headersFromMdc(headers, MDCHeaderKeys.PROCESS_CONTEXT);
    }

    private void addCommonHeaders(List<Request.Header> headers, String accessToken) {
        headers.add(new Request.Header(HttpHeaders.CONTENT_TYPE_STRING, MediaType.APPLICATION_JSON));
        if (accessToken != null) {
            headers.add(new Request.Header(HttpHeaders.AUTHORIZATION_STRING, "Bearer " + accessToken));
        }
    }

    private void headersFromMdc(List<Request.Header> headers, MDCHeaderKeys headerKey) {
        String mdcValue = MDC.get(headerKey.getMdcKey());
        if (mdcValue != null && mdcValue.isEmpty()) {
            headers.add(new Request.Header(headerKey.getHeaderName(), mdcValue.trim()));
        }
    }
}
