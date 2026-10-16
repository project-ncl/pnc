/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.Optional;

import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.model.RepositoryConfiguration;

import lombok.Data;

public interface SCMRepositoryProvider
        extends Provider<Integer, RepositoryConfiguration, SCMRepository, SCMRepository> {

    Page<SCMRepository> getAllWithMatchAndSearchUrl(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String matchUrl,
            String searchUrl);

    /**
     * Starts the task of creating SCMRepository. If the SCM URL is external, the task creates new internal repository
     * and does inital synchronization.
     *
     * @param scmUrl The URL of the SCM repository.
     * @param preBuildSyncEnabled If the SCM URL is external, this parameter specifies wheather the external repository
     *        should be synchronized into the internal one before build.
     * @return id of the created
     */
    RepositoryCreationResponse createSCMRepository(String scmUrl, Boolean preBuildSyncEnabled);

    /**
     * Starts the task of creating SCMRepository.If the SCM URL is external, the task creates new internal repository
     * and does initial synchronization.
     *
     * @param scmUrl The URL of the SCM repository.
     * @param preBuildSyncEnabled If the SCM URL is external, this parameter specifies whether the external repository
     *        should be synchronized into the internal one before build.
     * @param jobType Type of the job that requested the SCM repository creation (for notification purposes). repository
     *        id as a parameter.
     * @return id of the created
     */
    RepositoryCreationResponse createSCMRepository(
            String scmUrl,
            Boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Optional<BuildConfiguration> buildConfiguration);

    /**
     * Starts the task of creating SCMRepository.If the SCM URL is external, the task creates new internal repository
     * and does initial synchronization.
     *
     * @param scmUrl The URL of the SCM repository.
     * @param revision Revision to sync between the external and internal SCM repository
     * @param preBuildSyncEnabled If the SCM URL is external, this parameter specifies whether the external repository
     *        should be synchronized into the internal one before build.
     * @param jobType Type of the job that requested the SCM repository creation (for notification purposes). repository
     *        id as a parameter.
     * @return id of the created
     */
    RepositoryCreationResponse createSCMRepository(
            String scmUrl,
            String revision,
            Boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Optional<BuildConfiguration> buildConfiguration);

    void repositoryCreationCompleted(RepositoryCreationResult repositoryCreationResult);

    @Data
    public static class RepositoryCreated {
        private final Long taskId;
        private final int repositoryId;
    }
}
