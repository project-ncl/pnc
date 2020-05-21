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

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.facade.providers.api.ProductReleaseProvider;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.rest.api.endpoints.ProductVersionEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductVersionEndpointImpl implements ProductVersionEndpoint {

    @Inject
    private ProductVersionProvider productVersionProvider;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    @Inject
    private GroupConfigurationProvider groupConfigurationProvider;

    @Inject
    private ProductMilestoneProvider productMilestoneProvider;

    @Inject
    private ProductReleaseProvider productReleaseProvider;

    private EndpointHelper<Integer, ProductVersion, ProductVersionRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(ProductVersion.class, productVersionProvider);
    }

    @Override
    public ProductVersion createNew(ProductVersion productVersion) {
        return endpointHelper.create(productVersion);
    }

    @Override
    public ProductVersion getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, ProductVersion productVersion) {
        endpointHelper.update(id, productVersion);
    }

    @Override
    public ProductVersion patchSpecific(String id, ProductVersion productVersion) {
        return endpointHelper.update(id, productVersion);
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigs(String id, PageParameters pageParams) {

        return buildConfigurationProvider.getBuildConfigurationsForProductVersion(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public Page<GroupConfiguration> getGroupConfigs(String id, PageParameters pageParameters) {

        return groupConfigurationProvider.getGroupConfigurationsForProductVersion(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public Page<ProductMilestone> getMilestones(String id, PageParameters pageParameters) {

        return productMilestoneProvider.getProductMilestonesForProductVersion(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public Page<ProductRelease> getReleases(String id, PageParameters pageParameters) {

        return productReleaseProvider.getProductReleasesForProductVersion(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
