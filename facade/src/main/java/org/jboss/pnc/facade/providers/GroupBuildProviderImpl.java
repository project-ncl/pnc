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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.LoggedInUser;
import org.jboss.pnc.coordinator.maintenance.Result;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.facade.providers.api.GroupBuildProvider;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ValidationException;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.function.Consumer;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigSetRecordPredicates.withBuildConfigSetId;

@PermitAll
@Stateless
public class GroupBuildProviderImpl
        extends AbstractIntIdProvider<BuildConfigSetRecord, GroupBuild, GroupBuildRef>
        implements GroupBuildProvider {

    @Inject
    private BuildConfigurationSetRepository buildConfigSetRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;
    
    @Inject
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    //@Inject // TODO enable once notifier is ported from old module
    //private Notifier notifier;

    @Inject
    private BuildCoordinator buildCoordinator;

    @Inject
    private AuthenticationProvider authenticationProvider;

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    public GroupBuildProviderImpl(BuildConfigSetRecordRepository repository, GroupBuildMapper mapper) {
        super(repository, mapper, BuildConfigSetRecord.class);
    }

    @Override
    public void delete(String id) {
        Consumer<Result> onComplete = (result) -> {
            //notifier.sendToSubscribers(result.isSuccess(), Notifier.Topic.BUILD_CONFIG_SET_RECORDS_DELETE.getId(), result.getId());
        };

        try {
            temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuildConfigSetRecord(Integer.valueOf(id), currentUserToken(), onComplete);
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
    }

    @Override
    public Page<GroupBuild> getGroupBuilds(int pageIndex, int pageSize, String sort, String q, String groupConfigurationId) {
        return queryForCollection(pageIndex, pageSize, sort, q, withBuildConfigSetId(Integer.valueOf(groupConfigurationId)));
    }

    @Override
    public void cancel(String id) {
        try {
            buildCoordinator.cancelSet(Integer.parseInt(id));
        } catch (CoreException e) {
            throw new RuntimeException("Error when canceling buildConfigSetRecord with id: " + id, e);
        }
    }
    
    private String currentUserToken() {
        LoggedInUser currentUser = authenticationProvider.getLoggedInUser(httpServletRequest);
        return currentUser.getTokenString();
    }

}
