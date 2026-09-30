/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import static org.jboss.pnc.rest.endpoints.BuildEndpointImpl.toBuildPageInfo;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.requests.GroupBuildPushRequest;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.facade.providers.api.BuildConfigurationProvider;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.providers.api.GroupBuildProvider;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.rest.api.endpoints.GroupBuildEndpoint;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class GroupBuildEndpointImpl implements GroupBuildEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(GroupBuildEndpointImpl.class);

    @Inject
    private GroupBuildProvider provider;

    @Inject
    private BuildConfigurationProvider buildConfigurationProvider;

    @Inject
    private BuildProvider buildProvider;

    @Inject
    private BrewPusher brewPusher;

    private EndpointHelper<Base32LongID, GroupBuild, GroupBuildRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(GroupBuild.class, provider);
    }

    @Override
    public Page<GroupBuild> getAll(PageParameters pageParameters) {
        return endpointHelper.getAll(pageParameters);
    }

    @Override
    public GroupBuild getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void delete(String id, String callback) {
        if (!provider.delete(id, callback)) {
            throw new NotFoundException("Temporary GroupBuild with id: " + id + " was not found.");
        }
    }

    @Override
    public Page<Build> getBuilds(String id, PageParameters pageParams, BuildsFilterParameters filterParams) {
        return buildProvider.getBuildsForGroupBuild(toBuildPageInfo(pageParams, filterParams), id);
    }

    @Override
    public void brewPush(String id, GroupBuildPushRequest buildConfigSetRecordPushRequest) {
        // TODO progress updates
        brewPusher.pushGroup(id, buildConfigSetRecordPushRequest.getTagPrefix());
    }

    @Override
    public void cancel(String id) {
        logger.debug("Received cancel request fot Group Build {}.", id);
        if (provider.getSpecific(id) == null) {
            throw new NotFoundException("Unable to find Group Build {}." + id);
        }
        provider.cancel(id);
    }

    @Override
    public Graph<Build> getDependencyGraph(String id) {
        return buildProvider.getBuildGraphForGroupBuild(id);
    }

}
