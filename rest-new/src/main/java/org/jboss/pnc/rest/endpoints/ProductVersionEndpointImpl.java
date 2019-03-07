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

import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ProductVersionEndpointImpl
        extends AbstractEndpoint<ProductVersion, ProductVersionRef>
        implements ProductVersionEndpoint {

    private ProductVersionProvider productVersionProvider;
    private BuildConfigurationProvider buildConfigurationProvider;
    private GroupConfigurationProvider groupConfigurationProvider;
    private ProductMilestoneProvider productMilestoneProvider;
    private ProductReleaseProvider productReleaseProvider;

    public ProductVersionEndpointImpl() {
        super(ProductVersion.class);
    }

    @Inject
    public ProductVersionEndpointImpl(ProductVersionProvider productVersionProvider,
                                      BuildConfigurationProvider buildConfigurationProvider,
                                      GroupConfigurationProvider groupConfigurationProvider,
                                      ProductMilestoneProvider productMilestoneProvider,
                                      ProductReleaseProvider productReleaseProvider) {

        super(productVersionProvider, ProductVersion.class);

        this.productVersionProvider = productVersionProvider;
        this.buildConfigurationProvider = buildConfigurationProvider;
        this.groupConfigurationProvider = groupConfigurationProvider;
        this.productMilestoneProvider = productMilestoneProvider;
        this.productReleaseProvider = productReleaseProvider;
    }

    @Override
    public ProductVersion createNewProductVersion(ProductVersion productVersion) {
        return super.create(productVersion);
    }

    @Override
    public ProductVersion getSpecific(int id) {
        return super.getSpecific(id);
    }

    @Override
    public void update(int id, ProductVersion productVersion) {
        super.update(id, productVersion);
    }

    @Override
    public Page<BuildConfiguration> getBuildConfigurations(int id, PageParameters pageParams) {

        return buildConfigurationProvider.getBuildConfigurationsForProductVersion(
                pageParams.getPageIndex(),
                pageParams.getPageSize(),
                pageParams.getSort(),
                pageParams.getQ(),
                id);
    }

    @Override
    public Page<GroupConfiguration> getGroupConfigurations(int id, PageParameters pageParameters) {

        return groupConfigurationProvider.getGroupConfigurationsForProductVersion(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public Page<ProductMilestone> getMilestones(int id, PageParameters pageParameters) {

        return productMilestoneProvider.getProductMilestonesForProductVersion(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public Page<ProductRelease> getReleases(int id, PageParameters pageParameters) {

        return productReleaseProvider.getProductReleasesForProductVersion(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
