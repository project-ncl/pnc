/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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
        List<BuildRecord> buildRecords = queryAll();
        return getLatestSuccessfulBuildRecord(configurationId, buildRecords);
    }

    public static BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId, List<BuildRecord> buildRecords) {
        return buildRecords.stream()
                .filter(br -> br.getBuildConfigurationId().equals(configurationId))
                .filter(br -> br.getStatus().equals(BuildStatus.SUCCESS))
                .sorted(Comparator.comparing(BuildRecord::getId).reversed())
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
    public GraphWithMetadata<BuildRecord, Integer> getDependencyGraph(Integer buildRecordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(IdRev buildConfigurationAuditedIdRev) {
        return getLatestSuccessfulBuildRecord(buildConfigurationAuditedIdRev, data);
    }

    public static BuildRecord getLatestSuccessfulBuildRecord(IdRev buildConfigurationAuditedIdRev, List<BuildRecord> buildRecords) {
        Optional<BuildRecord> first = buildRecords.stream()
                .filter(buildRecord -> buildRecord.getStatus().equals(BuildStatus.SUCCESS))
                .filter(buildRecord -> buildRecord.getBuildConfigurationAuditedIdRev().equals(buildConfigurationAuditedIdRev))
                .sorted(Comparator.comparing(BuildRecord::getId).reversed())
                .findFirst();
        return first.orElse(null);
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