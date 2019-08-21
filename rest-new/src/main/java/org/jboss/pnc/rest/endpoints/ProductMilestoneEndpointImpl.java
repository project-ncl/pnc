/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.rest.api.endpoints.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

@Stateless
public class ProductMilestoneEndpointImpl implements ProductMilestoneEndpoint {

    @Inject
    private ProductMilestoneProvider productMilestoneProvider;
    
    @Inject
    private BuildProvider buildProvider;
    
    @Inject
    private AuthenticationProvider authenticationProvider;

    @Context
    private HttpServletRequest httpServletRequest;

    private EndpointHelper<ProductMilestone, ProductMilestoneRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(ProductMilestone.class, productMilestoneProvider);
    }

    @Override
    public ProductMilestone createNew(ProductMilestone productMilestone) {
        return endpointHelper.create(productMilestone);
    }

    @Override
    public ProductMilestone getSpecific(int id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(int id, ProductMilestone productMilestone) {
        endpointHelper.update(id, productMilestone);
    }

    @Override
    public ProductMilestone patchSpecific(int id, ProductMilestone productMilestone) {
        return endpointHelper.update(id, productMilestone);
    }

    @Override
    public Page<Build> getBuilds(int id, PageParameters page, BuildsFilterParameters filter) {
        BuildPageInfo pageInfo = BuildEndpointImpl.toBuildPageInfo(page, filter);
        return buildProvider.getBuildsForMilestone(pageInfo, id);
    }

    @Override
    public void closeMilestone(int id, ProductMilestone productMilestone) {
        if (httpServletRequest != null) {
            LoggedInUser loginInUser = authenticationProvider.getLoggedInUser(httpServletRequest);
            productMilestoneProvider.closeMilestone(id, productMilestone, loginInUser.getTokenString());
        } else {
            productMilestoneProvider.closeMilestone(id, productMilestone);
        }
    }

    @Override
    public void cancelMilestoneClose(int id) {
        productMilestoneProvider.cancelMilestoneCloseProcess(id);
    }
}
