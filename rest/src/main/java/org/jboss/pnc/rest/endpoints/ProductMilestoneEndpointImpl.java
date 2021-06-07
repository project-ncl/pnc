/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneCloseResult;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.requests.DeliverablesAnalysisRequest;
import org.jboss.pnc.dto.requests.validation.VersionValidationRequest;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.ValidationResponse;
import org.jboss.pnc.facade.DeliverablesAnalyzerInvoker;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneCloseResultProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.rest.api.endpoints.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.pnc.rest.api.parameters.ProductMilestoneCloseParameters;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.Collections;

@ApplicationScoped
public class ProductMilestoneEndpointImpl implements ProductMilestoneEndpoint {

    @Inject
    private ProductMilestoneProvider productMilestoneProvider;

    @Inject
    private ProductMilestoneCloseResultProvider productMilestoneCloseResultProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private AuthenticationProvider authenticationProvider;

    @Inject
    private DeliverablesAnalyzerInvoker analyzerInvoker;

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
        BuildPageInfo pageInfo = BuildEndpointImpl.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForMilestone(pageInfo, id);
    }

    @Override
    public ProductMilestoneCloseResult closeMilestone(String id) {
        return productMilestoneProvider.closeMilestone(id);
    }

    @Override
    public void cancelMilestoneClose(String id) {
        productMilestoneProvider.cancelMilestoneCloseProcess(id);
    }

    @Override
    public Page<ProductMilestoneCloseResult> getCloseResults(
            String id,
            PageParameters pageParams,
            ProductMilestoneCloseParameters filterParams) {
        if (filterParams != null && filterParams.isLatest()) {
            ProductMilestoneCloseResult latestProductMilestoneCloseResult = productMilestoneCloseResultProvider
                    .getLatestProductMilestoneCloseResult(Integer.parseInt(id));
            return new Page<>(0, 1, 1, Collections.singletonList(latestProductMilestoneCloseResult));
        } else {
            return productMilestoneCloseResultProvider.getProductMilestoneCloseResults(
                    pageParams.getPageIndex(),
                    pageParams.getPageSize(),
                    pageParams.getSort(),
                    pageParams.getQ(),
                    Integer.parseInt(id),
                    filterParams.isLatest(),
                    filterParams.isRunning());
        }
    }

    @Override
    public ValidationResponse validateVersion(VersionValidationRequest versionRequest) {
        return productMilestoneProvider
                .validateVersion(versionRequest.getProductVersionId(), versionRequest.getVersion());
    }

    @Override
    public void analyzeDeliverables(String id, DeliverablesAnalysisRequest request) {
        MDCUtils.addProcessContext(String.valueOf(Sequence.nextId()));
        analyzerInvoker.startAnalysis(id, request);
    }
}
