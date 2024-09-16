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
package org.jboss.pnc.facade.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.auth.KeycloakServiceClient;
import org.jboss.pnc.common.util.HttpUtils;
import org.jboss.pnc.coordinator.maintenance.TemporaryBuildsCleanerAsyncInvoker;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.GroupBuildPageInfo;
import org.jboss.pnc.facade.providers.api.GroupBuildProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.mapper.api.ResultMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigSetRecord_;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
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
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_ADMIN;
import static org.jboss.pnc.facade.providers.api.UserRoles.USERS_BUILD_ADMIN;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigSetRecordPredicates.withBuildConfigSetId;

@PermitAll
@Stateless
public class GroupBuildProviderImpl extends
        AbstractProvider<Base32LongID, BuildConfigSetRecord, GroupBuild, GroupBuildRef> implements GroupBuildProvider {

    private static final Logger logger = LoggerFactory.getLogger(GroupBuildProviderImpl.class);

    @Inject
    private BuildConfigurationSetRepository buildConfigSetRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    private TemporaryBuildsCleanerAsyncInvoker temporaryBuildsCleanerAsyncInvoker;

    @Inject
    private BuildCoordinator buildCoordinator;

    @Inject
    private GroupConfigurationMapper groupConfigurationMapper;

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    private KeycloakServiceClient keycloakServiceClient;

    private UserService userService;

    private ResultMapper resultMapper;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    @RolesAllowed({ USERS_BUILD_ADMIN, USERS_ADMIN })
    @Override
    public GroupBuild update(String id, GroupBuild restEntity) {
        return super.update(id, restEntity);
    }

    @Override
    public boolean delete(String id, String callback) {

        try {
            String accessToken = keycloakServiceClient.getAuthToken();
            return temporaryBuildsCleanerAsyncInvoker.deleteTemporaryBuildConfigSetRecord(
                    mapper.getIdMapper().toEntity(id),
                    accessToken,
                    notifyOnDeletionCompletion(callback, accessToken));
        } catch (ValidationException e) {
            throw new RepositoryViolationException(e);
        }
    }

    private Consumer<Result> notifyOnDeletionCompletion(String callbackUrl, String accessToken) {
        return (result) -> {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                try {
                    HttpUtils.performHttpPostRequest(
                            callbackUrl,
                            OBJECT_MAPPER.writeValueAsString(resultMapper.toDTO(result)),
                            accessToken);
                } catch (JsonProcessingException e) {
                    logger.error("Failed to perform a callback of BuildConfigSetRecord delete operation.", e);
                }
            }
        };
    }

    @Override
    public Page<GroupBuild> getGroupBuilds(GroupBuildPageInfo groupBuildPageInfo, String groupConfigId) {
        Integer groupConfigIdModel = groupConfigurationMapper.getIdMapper().toEntity(groupConfigId);
        if (groupBuildPageInfo.isLatest()) {
            PageInfo firstPageInfo = new DefaultPageInfo(0, 1);
            SortInfo<BuildConfigSetRecord> sortInfo = DefaultSortInfo.desc(BuildConfigSetRecord_.startTime);
            List<BuildConfigSetRecord> groupBuildsEntities = repository
                    .queryWithPredicates(firstPageInfo, sortInfo, withBuildConfigSetId(groupConfigIdModel));
            List<GroupBuild> groupBuilds = groupBuildsEntities.stream().map(mapper::toDTO).collect(Collectors.toList());
            return new Page<>(0, 1, groupBuilds.size(), groupBuilds.size(), groupBuilds);
        } else {
            return queryForCollection(
                    groupBuildPageInfo.getPageIndex(),
                    groupBuildPageInfo.getPageSize(),
                    groupBuildPageInfo.getSort(),
                    groupBuildPageInfo.getQ(),
                    withBuildConfigSetId(groupConfigIdModel));
        }
    }

    @Override
    public void cancel(String id) {
        try {
            buildCoordinator.cancelSet(mapper.getIdMapper().toEntity(id));
        } catch (CoreException e) {
            throw new RuntimeException("Error when canceling buildConfigSetRecord with id: " + id, e);
        }
    }
}
