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

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Graph;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.mapper.api.BuildMapper;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withProjectId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIds;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withPerformedInMilestone;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withUserId;

import org.jboss.pnc.facade.providers.api.BuildPageInfo;
import org.jboss.pnc.facade.providers.api.BuildProvider;

@Stateless
public class BuildProviderImpl extends AbstractProvider<BuildRecord, Build, BuildRef> implements BuildProvider {

    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    public BuildProviderImpl(BuildRecordRepository repository, BuildMapper mapper, BuildConfigurationRepository buildConfigurationRepository) {
        super(repository, mapper, BuildRecord.class);

        this.buildConfigurationRepository = buildConfigurationRepository;
    }

    @Override
    public Page<Build> getPerformedBuildsForMilestone(int pageIndex,
                                                      int pageSize,
                                                      String sortingRsql,
                                                      String query,
                                                      Integer milestoneId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withPerformedInMilestone(milestoneId));
    }

    @Override
    public Page<Build> getBuildsForProject(int pageIndex,
                                           int pageSize,
                                           String sortingRsql,
                                           String query,
                                           Integer projectId) {

        @SuppressWarnings("unchecked")
        Set<Integer> buildConfigIds = buildConfigurationRepository
                .queryWithPredicates(withProjectId(projectId))
                .stream()
                .map(BuildConfiguration::getId)
                .collect(Collectors.toSet());

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationIds(buildConfigIds));
    }

    @Override
    public Page<Build> getBuildsForBuildConfiguration(int pageIndex,
                                                      int pageSize,
                                                      String sortingRsql,
                                                      String query,
                                                      Integer buildConfigurationId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationId(buildConfigurationId));
    }

    @Override
    public Page<Build> getBuildsForUser(int pageIndex,
                                        int pageSize,
                                        String sortingRsql,
                                        String query,
                                        Integer userId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withUserId(userId));
    }

    @Override
    public Page<Build> getBuildsForGroupConfiguration(BuildPageInfo pageInfo, int groupConfigurationId) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public Page<Build> getBuildsForGroupBuild(BuildPageInfo pageInfo, int groupBuildId) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

    @Override
    public Graph<Build> getGroupBuildGraph(int id) {
        throw new UnsupportedOperationException("Not supported yet."); // TODO
    }

}
