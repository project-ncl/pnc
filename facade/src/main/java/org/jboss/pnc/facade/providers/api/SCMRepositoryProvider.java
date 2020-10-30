/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers.api;

import lombok.Data;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;
import org.jboss.pnc.dto.tasks.RepositoryCreationResult;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.model.RepositoryConfiguration;

import java.util.function.Consumer;

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
     * and does inital synchronization.
     *
     * @param scmUrl The URL of the SCM repository.
     * @param preBuildSyncEnabled If the SCM URL is external, this parameter specifies wheather the external repository
     *        should be synchronized into the internal one before build.
     * @param jobType Type of the job that requested the SCM repository creation (for notification purposes).
     * @param consumer Callback function that is called when SCM repository is created. The callback function takes SCM
     *        repository id as a parameter.
     * @return id of the created
     */
    RepositoryCreationResponse createSCMRepository(
            String scmUrl,
            Boolean preBuildSyncEnabled,
            JobNotificationType jobType,
            Consumer<RepositoryCreated> consumer);

    void repositoryCreationCompleted(RepositoryCreationResult repositoryCreationResult);

    @Data
    public static class RepositoryCreated {
        private final Integer taskId;
        private final int repositoryId;
    }
}
