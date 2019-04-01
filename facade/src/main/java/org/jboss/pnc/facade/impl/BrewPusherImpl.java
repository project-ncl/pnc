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
package org.jboss.pnc.facade.impl;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.facade.BrewPusher;
import org.jboss.pnc.managers.BuildResultPushManager;
import org.jboss.pnc.managers.Result;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@RequestScoped
public class BrewPusherImpl implements BrewPusher {

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildResultPushManager buildResultPushManager;

    @Inject
    private Configuration configuration;

    @Inject
    private AuthenticationProvider authenticationProvider;

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void pushGroup(int id, String tagPrefix) {

        List<BuildRecord> buildRecords = buildRecordRepository.queryWithPredicates(
                BuildRecordPredicates.withBuildConfigSetRecordId(id));

        Set<Integer> buildRecordsIds = buildRecords.stream()
                .map(BuildRecord::getId)
                .collect(Collectors.toSet());

        try {
            Set<Result> pushed = buildResultPushManager.push(
                    buildRecordsIds,
                    currentUserToken(),
                    getCompleteCallbackUrl(),
                    tagPrefix);
        } catch (ProcessException ex) {
            throw new RuntimeException(ex);
        }
    }

    private String currentUserToken() {
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        return currentUser.getTokenString();
    }

    private String getCompleteCallbackUrl() {
        try {
            String pncBaseUrl = StringUtils.stripEndingSlash(configuration.getGlobalConfig().getPncUrl());
            return pncBaseUrl + "/builds/%d/complete";
        } catch (ConfigurationParseException ex) {
            throw new IllegalStateException("Could not construct callback url.", ex);
        }
    }

}
