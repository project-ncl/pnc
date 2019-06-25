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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.enums.BuildStatus;

import java.net.URI;
import java.util.List;

public interface BuildProvider extends Provider<org.jboss.pnc.model.BuildRecord, Build, BuildRef> {

    /**
     * Get the internal scm archive link for a build record. If the scm revision is not specified in the build record
     * due to a failure, it will return null
     *
     * @param id build id
     *
     * @return Uri of the internal scm archive link to download
     */
    URI getInternalScmArchiveLink(int id);

    void addAttribute(int id, String key, String value);

    void removeAttribute(int id, String key);

    BuildConfigurationRevision getBuildConfigurationRevision(Integer id);

    String getRepourLog(Integer id);

    String getBuildLog(Integer id);

    SSHCredentials getSshCredentials(Integer id);

    Page<Build> getBuilds(BuildPageInfo pageInfo);

    Page<Build> getBuildsForMilestone(BuildPageInfo pageInfo, int milestoneId);

    Page<Build> getBuildsForProject(BuildPageInfo pageInfo, int projectId);

    Page<Build> getBuildsForBuildConfiguration(BuildPageInfo pageInfo, int buildConfigurationId);

    Page<Build> getBuildsForUser(BuildPageInfo pageInfo, int userId);

    Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, int groupConfigurationId);

    Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, int groupBuildId);

    Graph<Build> getGroupBuildGraph(int id);

    Page<Build> getAllByStatusAndLogContaining(int pageIndex,
                                               int pageSize,
                                               String sortingRsql,
                                               String query,
                                               BuildStatus status,
                                               String search);

    void setBuiltArtifacts(int id, List<Integer> artifactIds);

    void setDependentArtifacts(int id, List<Integer> artifactIds);
}
