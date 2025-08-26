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

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.response.DeliveredArtifactInMilestones;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.requests.DeliverablesAnalysisRequest;
import org.jboss.pnc.dto.requests.MilestoneCloseRequest;
import org.jboss.pnc.dto.requests.validation.VersionValidationRequest;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneStatistics;
import org.jboss.pnc.facade.DeliverableAnalyzerManager;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.BuildPushOperationProvider;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerOperationProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.rest.api.endpoints.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

@ApplicationScoped
public class ProductMilestoneEndpointImpl implements ProductMilestoneEndpoint {

    @Inject
    private ProductMilestoneProvider productMilestoneProvider;

    @Inject
    private ArtifactProvider artifactProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private AuthenticationProvider authenticationProvider;

    @Inject
    private DeliverableAnalyzerManager deliverableAnalyzerManager;

    @Inject
    private DeliverableAnalyzerOperationProvider delAnalyzerProvider;

    @Inject
    private BuildPushOperationProvider buildPushOperationProvider;

    @Context
    private HttpServletRequest httpServletRequest;

    private EndpointHelper<Integer, ProductMilestone, ProductMilestoneRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(ProductMilestone.class, productMilestoneProvider);
    }

    @Override
    public ProductMilestone createNew(ProductMilestone productMilestone) {
        return endpointHelper.create(productMilestone);
    }

    @Override
    public ProductMilestone getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, ProductMilestone productMilestone) {
        endpointHelper.update(id, productMilestone);
    }

    @Override
    public ProductMilestone patchSpecific(String id, ProductMilestone productMilestone) {
        return endpointHelper.update(id, productMilestone);
    }

    @Override
    public Page<Build> getBuilds(String id, PageParameters page, BuildsFilterParameters filter) {
        BuildPageInfo pageInfo = BuildPageInfo.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForMilestone(pageInfo, id);
    }

    @Override
    public void closeMilestone(String id, MilestoneCloseRequest closeRequest) {
        boolean skipPush = closeRequest != null && Boolean.TRUE.equals(closeRequest.getSkipBrewPush());
        productMilestoneProvider.closeMilestone(id, skipPush);
    }

    @Override
    public void cancelMilestoneClose(String id) {
        productMilestoneProvider.cancelMilestoneCloseProcess(id);
    }

    @Override
    public Page<BuildPushOperation> getPushOperations(String id, boolean latest, PageParameters pageParameters) {
        return buildPushOperationProvider.getOperationsForMilestone(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                latest,
                id);
    }

    @Override
    public Page<Artifact> getDeliveredArtifacts(String id, PageParameters pageParams) {
        return artifactProvider.getDeliveredArtifactsForMilestone(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperations(
            String id,
            PageParameters pageParams) {
        return delAnalyzerProvider.getAllDeliverableAnalyzerOperationsForMilestone(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public ProductMilestoneStatistics getStatistics(String id) {
        return productMilestoneProvider.getStatistics(id);
    }

    @Override
    public ValidationResponse validateVersion(VersionValidationRequest versionRequest) {
        return productMilestoneProvider
                .validateVersion(versionRequest.getProductVersionId(), versionRequest.getVersion());
    }

    @Override
    public DeliverableAnalyzerOperation analyzeDeliverables(String id, DeliverablesAnalysisRequest request) {
        return deliverableAnalyzerManager
                .analyzeDeliverables(id, request.getDeliverablesUrls(), request.isRunAsScratchAnalysis());
    }

    @Override
    public List<DeliveredArtifactInMilestones> compareArtifactVersionsDeliveredInMilestones(List<String> milestoneIds) {
        return productMilestoneProvider.getArtifactsDeliveredInMilestonesGroupedByPrefix(milestoneIds);
    }

    @Override
    public Graph<ProductMilestone> getMilestonesSharingDeliveredArtifactsGraph(String milestoneId, Integer depthLimit) {
        return productMilestoneProvider.getMilestonesSharingDeliveredArtifactsGraph(milestoneId, depthLimit);
    }

    @Override
    public Page<Artifact> getDeliveredArtifactsSharedInMilestones(
            PageParameters pageParams,
            String milestone1Id,
            String milestone2Id) {
        return artifactProvider.getDeliveredArtifactsSharedInMilestones(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                milestone1Id,
                milestone2Id);
    }
}
