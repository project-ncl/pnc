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

import org.jboss.pnc.dto.DeliverableAnalyzerLabelEntry;
import org.jboss.pnc.dto.DeliverableAnalyzerReport;
import org.jboss.pnc.dto.requests.labels.DeliverableAnalyzerReportLabelRequest;
import org.jboss.pnc.dto.response.AnalyzedArtifact;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerReportProvider;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.rest.api.endpoints.DeliverableAnalyzerReportEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class DeliverableAnalyzerReportEndpointImpl implements DeliverableAnalyzerReportEndpoint {

    @Inject
    private DeliverableAnalyzerReportProvider provider;

    private EndpointHelper<Base32LongID, DeliverableAnalyzerReport, DeliverableAnalyzerReport> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(DeliverableAnalyzerReport.class, provider);
    }

    @Override
    public Page<DeliverableAnalyzerReport> getAll(PageParameters pageParameters) {
        return endpointHelper.getAll(pageParameters);
    }

    @Override
    public DeliverableAnalyzerReport getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public Page<AnalyzedArtifact> getAnalyzedArtifacts(String id, PageParameters pageParameters) {
        return provider.getAnalyzedArtifacts(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getQ(),
                pageParameters.getSort(),
                id);
    }

    @Override
    public void addLabel(String id, DeliverableAnalyzerReportLabelRequest request) {
        provider.addLabel(id, request);
    }

    @Override
    public void removeLabel(String id, DeliverableAnalyzerReportLabelRequest request) {
        provider.removeLabel(id, request);
    }

    @Override
    public Page<DeliverableAnalyzerLabelEntry> getLabelHistory(String id, PageParameters pageParameters) {
        return provider.getLabelHistory(
                id,
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ());
    }
}
