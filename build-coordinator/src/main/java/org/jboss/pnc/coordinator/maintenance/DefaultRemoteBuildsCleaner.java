/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.maintenance;

import java.text.MessageFormat;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.api.causeway.dto.untag.TaggedBuild;
import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;
import org.jboss.pnc.api.enums.OperationResult;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryRollbackRequest;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryRollbackResult;
import org.jboss.pnc.auth.ServiceAccountClient;
import org.jboss.pnc.causewayclient.CausewayClient;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushReport;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.predicates.OperationPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class DefaultRemoteBuildsCleaner implements RemoteBuildsCleaner {

    private Logger logger = LoggerFactory.getLogger(DefaultRemoteBuildsCleaner.class);

    private RepoDriverClient repoDriverClient;

    ServiceAccountClient serviceClient;

    CausewayClient causewayClient;

    private final BuildPushOperationRepository buildPushOperationRepository;

    private final IndyRepoDriverModuleConfig indyRepoDriverConfig;

    @Inject
    public DefaultRemoteBuildsCleaner(
            Configuration configuration,
            @RestClient RepoDriverClient repoDriverClient,
            ServiceAccountClient serviceClient,
            CausewayClient causewayClient,
            BuildPushOperationRepository buildPushOperationRepository) {
        this.repoDriverClient = repoDriverClient;
        this.serviceClient = serviceClient;
        this.causewayClient = causewayClient;
        this.buildPushOperationRepository = buildPushOperationRepository;

        try {
            this.indyRepoDriverConfig = configuration
                    .getModuleConfig(new PncConfigProvider<>(IndyRepoDriverModuleConfig.class));
        } catch (ConfigurationParseException e) {
            throw new IllegalStateException(
                    "Cannot read configuration for " + IndyRepoDriverModuleConfig.class.getName() + ".",
                    e);
        }
    }

    @Override
    public Result deleteRemoteBuilds(BuildRecord buildRecord) {
        Result result = deleteBuildsFromArtifactory(buildRecord);
        if (!result.isSuccess()) {
            return result;
        }
        // TODO
        // result = deleteLogsFromBifrost(buildRecord);
        // if (!result.isSuccess()) {
        //     return result;
        // }
        result = requestDeleteViaCauseway(buildRecord);
        if (!result.isSuccess()) {
            return result;
        }
        return new Result(BuildMapper.idMapper.toDto(buildRecord.getId()), ResultStatus.SUCCESS);
    }

    private Result requestDeleteViaCauseway(BuildRecord buildRecord) {
        List<BuildPushOperation> buildPushOperations = buildPushOperationRepository.queryWithPredicates(
                BuildPushPredicates.withBuild(buildRecord.getId()),
                OperationPredicates.withResult(OperationResult.SUCCESSFUL));
        String externalBuildId = BuildMapper.idMapper.toDto(buildRecord.getId());
        for (BuildPushOperation buildPushOperation : buildPushOperations) {
            BuildPushReport report = buildPushOperation.getReport();
            boolean success = causewayUntag(report.getOperation().getTagPrefix(), report.getBrewBuildId());
            if (!success) {
                logger.error(
                        "Failed to un-tag pushed build record. BuildRecord.id: {}; brewBuildId: {}; tagPrefix: {};",
                        buildRecord.getId(),
                        report.getBrewBuildId(),
                        report.getOperation().getTagPrefix());
                return new Result(externalBuildId, ResultStatus.FAILED, "Failed to un-tag pushed build record.");
            }
        }
        return new Result(externalBuildId, ResultStatus.SUCCESS);
    }

    private Result deleteBuildsFromArtifactory(BuildRecord buildRecord) {
        String buildContentId = buildRecord.getBuildContentId();

        Result result;
        if (buildContentId == null) {
            logger.debug("Build contentId is null. Nothing to be deleted from Artifactory.");
            return new Result(
                    buildContentId,
                    ResultStatus.SUCCESS,
                    "BuildContentId is null. Nothing to be deleted from Artifactory.");
        }

        try {
            // delete artifacts from consolidated repositories
            RepositoryRollbackRequest request = RepositoryRollbackRequest.builder()
                    .buildContentId(buildContentId)
                    .build();
            RepositoryRollbackResult rollbackResult = repoDriverClient.rollback(request);
            if (!rollbackResult.getStatus().isSuccess()) {
                String description = MessageFormat.format(
                        "Failed to rollback promoted artifacts identified by buildContentId {0}.",
                        buildContentId);
                result = new Result(buildContentId, ResultStatus.FAILED, description);
            }

            // mark the repository for deletion and delete the tracking report
            // TODO repoDriverClient.cleanup(buildContentId);
            result = new Result(buildContentId, ResultStatus.SUCCESS);
        } catch (RuntimeException e) {
            String description = MessageFormat.format(
                    "Failed to delete temporary hosted repository identified by buildContentId {0}.",
                    buildContentId);
            logger.error(description, e);
            result = new Result(buildContentId, ResultStatus.FAILED, description);
        }
        return result;
    }

    private boolean causewayUntag(String tagPrefix, int brewBuildId) {
        String authHeaderValue = serviceClient.getAuthHeaderValue();

        UntagRequest untagRequest = prepareUntagRequest(tagPrefix, brewBuildId);
        return causewayClient.untagBuild(untagRequest, authHeaderValue);
    }

    private UntagRequest prepareUntagRequest(String tagPrefix, int brewBuildId) {
        TaggedBuild taggedBuild = new TaggedBuild(tagPrefix, brewBuildId);
        return new UntagRequest(null, taggedBuild);
    }

}
