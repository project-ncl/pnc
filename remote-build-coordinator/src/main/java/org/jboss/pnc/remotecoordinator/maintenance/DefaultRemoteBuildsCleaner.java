/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.maintenance;

import java.text.MessageFormat;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryRollbackRequest;
import org.jboss.pnc.api.repositorydriver.dto.RepositoryRollbackResult;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.IndyRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class DefaultRemoteBuildsCleaner implements RemoteBuildsCleaner {

    private final Logger logger = LoggerFactory.getLogger(DefaultRemoteBuildsCleaner.class);

    private RepoDriverClient repoDriverClient;

    private final IndyRepoDriverModuleConfig indyRepoDriverConfig;

    @Inject
    public DefaultRemoteBuildsCleaner(
            Configuration configuration,
            @RestClient RepoDriverClient repoDriverClient) {
        this.repoDriverClient = repoDriverClient;

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
        return new Result(BuildMapper.idMapper.toDto(buildRecord.getId()), ResultStatus.SUCCESS);
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
            } else {
                // mark the repository for deletion and delete the tracking report
                // TODO repoDriverClient.cleanup(buildContentId);
                result = new Result(buildContentId, ResultStatus.SUCCESS);
            }
        } catch (RuntimeException e) {
            String description = MessageFormat.format(
                    "Failed to delete temporary hosted repository identified by buildContentId {0}.",
                    buildContentId);
            logger.error(description, e);
            result = new Result(buildContentId, ResultStatus.FAILED, description);
        }
        return result;
    }

}
