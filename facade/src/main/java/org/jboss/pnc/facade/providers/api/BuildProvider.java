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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RunningBuildCount;
import org.jboss.pnc.dto.response.SSHCredentials;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.model.Base32LongID;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BuildProvider extends Provider<Base32LongID, org.jboss.pnc.model.BuildRecord, Build, BuildRef> {

    /**
     * Get the internal scm archive link for a build record. If the scm revision is not specified in the build record
     * due to a failure, it will return null
     *
     * @param buildId
     *
     * @return Uri of the internal scm archive link to download
     */
    URI getInternalScmArchiveLink(String buildId);

    void addAttribute(String buildId, String key, String value);

    void removeAttribute(String buildId, String key);

    BuildConfigurationRevision getBuildConfigurationRevision(String buildId);

    boolean delete(String buildId, String callback);

    SSHCredentials getSshCredentials(String buildId);

    Page<Build> getAllIndependentTemporaryOlderThanTimestamp(
            int pageIndex,
            int pageSize,
            String sort,
            String q,
            long timestamp);

    Page<Build> getBuilds(BuildPageInfo pageInfo);

    Page<Build> getBuildsForMilestone(BuildPageInfo pageInfo, String milestoneId);

    Page<Build> getBuildsForProject(BuildPageInfo pageInfo, String projectId);

    Page<Build> getBuildsForBuildConfiguration(BuildPageInfo pageInfo, String buildConfigurationId);

    Page<Build> getBuildsForUser(BuildPageInfo pageInfo, String userId);

    Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, String groupConfigurationId);

    Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, String groupBuildId);

    Page<Build> getBuildsForArtifact(int pageIndex, int pageSize, String sortingRsql, String query, String artifactId);

    Page<Build> getDependantBuildsForArtifact(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String artifactId);

    Graph<Build> getBuildGraphForGroupBuild(String id);

    /**
     *
     * @param buildId
     * @return
     * @throws EmptyEntityException when there is no record for given id
     */
    Graph<Build> getDependencyGraph(String buildId);

    void setBuiltArtifacts(String buildId, List<String> artifactIds);

    void setDependentArtifacts(String buildId, List<String> artifactIds);

    Page<Build> getByAttribute(BuildPageInfo pageInfo, Map<String, String> attributeConstraints);

    /**
     * Return count of current running builds
     *
     * @return count
     */
    RunningBuildCount getRunningCount();

    Set<String> getBuiltArtifactIds(String buildId);

    Page<BuildRecordInsights> getAllBuildRecordInsightsNewerThanTimestamp(
            int pageIndex,
            int pageSize,
            Date lastupdatetime);

    Graph<Build> getImplicitDependencyGraph(String buildId, Integer depthLimit);
}
