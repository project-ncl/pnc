/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.util.graph.Graph;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 9/22/16
 * Time: 12:04 PM
 */
public class BuildRecordRepositoryMock extends RepositoryMock<BuildRecord> implements BuildRecordRepository {
    @Override
    public BuildRecord findByIdFetchAllProperties(Integer id) {
        return queryById(id);
    }

    @Override
    public BuildRecord findByIdFetchProperties(Integer id) {
        return queryById(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildRecord>... predicates) {
        return null;
    }

    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, List<Predicate<BuildRecord>> andPredicates, List<Predicate<BuildRecord>> orPredicates) {
        return null;
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId) {
        return queryAll().stream()
                .filter(br -> br.getBuildConfigurationId().equals(configurationId))
                .filter(br -> br.getStatus().equals(BuildStatus.SUCCESS))
                .findFirst().orElse(null);
    }

    @Override
    public List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId) {
        return data.stream()
                .filter(buildRecord -> buildRecord.getBuildConfigurationId().equals(configurationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildRecord> findTemporaryBuildsOlderThan(Date date) {
        return null;
    }

    @Override
    public Graph<BuildRecord> getDependencyGraph(Integer buildRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BuildRecord save(BuildRecord entity) {
        return super.save(entity);
    }

    @Override
    public List<BuildRecord> queryAll() {
        return super.queryAll();
    }
}