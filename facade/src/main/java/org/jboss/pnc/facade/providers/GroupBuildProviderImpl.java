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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.GroupBuildProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.ResultMapper;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.function.Consumer;

import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigSetRecordPredicates.withBuildConfigSetId;

@PermitAll
@Stateless
public class GroupBuildProviderImpl extends AbstractIntIdProvider<BuildConfigSetRecord, GroupBuild, GroupBuildRef>
        implements GroupBuildProvider {

    private static final Logger logger = LoggerFactory.getLogger(GroupBuildProviderImpl.class);

    @Inject
    private BuildConfigurationSetRepository buildConfigSetRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    @Inject
    private BuildCoordinator buildCoordinator;

    @Context
    private HttpServletRequest httpServletRequest;

    private UserService userService;

    private ResultMapper resultMapper;

    @Inject
    public GroupBuildProviderImpl(
            BuildConfigSetRecordRepository repository,
            GroupBuildMapper mapper,
            UserService userService,
            ResultMapper resultMapper) {
        super(repository, mapper, BuildConfigSetRecord.class);
        this.userService = userService;
        this.resultMapper = resultMapper;
    }

    @Override
    public GroupBuild store(GroupBuild restEntity) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct GroupBuilds creation is not available.");
    }

    @RolesAllowed(SYSTEM_USER)
    @Override
    public GroupBuild update(String id, GroupBuild restEntity) {
        return super.update(id, restEntity);
    }

    @Override
    public boolean delete(String id, String callback) {
        User user = userService.currentUser();

        if (user == null) {
            throw new RuntimeException("Failed to load user metadata.");
        }
        try {
            return temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuildConfigSetRecord(
                    Integer.valueOf(id),
                    user.getLoginToken(),
                    notifyOnDeletionCompletion(callback));
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
    }

    private Consumer<Result> notifyOnDeletionCompletion(String callbackUrl) {
        return (result) -> {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                try {
                    HttpUtils.performHttpPostRequest(callbackUrl, resultMapper.toDTO(result));
                } catch (JsonProcessingException e) {
                    logger.error("Failed to perform a callback of BuildConfigSetRecord delete operation.", e);
                }
            }
        };
    }

    @Override
    public Page<GroupBuild> getGroupBuilds(
            int pageIndex,
            int pageSize,
            String sort,
            String q,
            String groupConfigurationId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sort,
                q,
                withBuildConfigSetId(Integer.valueOf(groupConfigurationId)));
    }

    @Override
    public void cancel(String id) {
        try {
            buildCoordinator.cancelSet(Integer.parseInt(id));
        } catch (CoreException e) {
            throw new RuntimeException("Error when canceling buildConfigSetRecord with id: " + id, e);
        }
    }
}
