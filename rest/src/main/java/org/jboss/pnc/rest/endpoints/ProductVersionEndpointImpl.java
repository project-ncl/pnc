/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductVersion;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneArtifactQualityStatistics;
import org.jboss.pnc.dto.response.statistics.ProductMilestoneRepositoryTypeStatistics;
import org.jboss.pnc.dto.response.statistics.ProductVersionStatistics;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.facade.providers.api.ProductMilestoneProvider;
import org.jboss.pnc.facade.providers.api.ProductReleaseProvider;
import org.jboss.pnc.facade.providers.api.ProductVersionProvider;
import org.jboss.pnc.rest.api.endpoints.ProductVersionEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

@ApplicationScoped
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

    @Override
    public ProductVersionStatistics getStatistics(String id) {
        return productVersionProvider.getStatistics(id);
    }

    @Override
    public Page<ProductMilestoneArtifactQualityStatistics> getArtifactQualitiesStatistics(
            String id,
            PageParameters pageParameters) {
        return productVersionProvider.getArtifactQualitiesStatistics(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }

    @Override
    public Page<ProductMilestoneRepositoryTypeStatistics> getRepositoryTypesStatistics(
            String id,
            PageParameters pageParameters) {
        return productVersionProvider.getRepositoryTypesStatistics(
                pageParameters.getPageIndex(),
                pageParameters.getPageSize(),
                pageParameters.getSort(),
                pageParameters.getQ(),
                id);
    }
}
