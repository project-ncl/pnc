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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.common.util.CollectionUtils.ofNullableCollection;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:04 PM
 */
public class BuildRecordRepositoryMock extends Base32LongIdRepositoryMock<BuildRecord>
        implements BuildRecordRepository {
    @Override
    public BuildRecord findByIdFetchAllProperties(Base32LongID id) {
        return queryById(id);
    }

    @Override
    public BuildRecord findByIdFetchProperties(Base32LongID id) {
        return queryById(id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(
            PageInfo pageInfo,
            SortInfo sortInfo,
            Predicate<BuildRecord>... predicates) {
        return null;
    }

    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(
            PageInfo pageInfo,
            SortInfo sortInfo,
            List<Predicate<BuildRecord>> andPredicates,
            List<Predicate<BuildRecord>> orPredicates) {
        return null;
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(
            Integer configurationId,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {
        List<BuildRecord> buildRecords = queryAll();
        return getLatestSuccessfulBuildRecord(configurationId, buildRecords, temporaryBuild, alignmentPreference);
    }

    public static BuildRecord getLatestSuccessfulBuildRecord(
            Integer configurationId,
            List<BuildRecord> buildRecords,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {

        // Get the latest successful persistent build record (of this build configuration)
        final BuildRecord latestPersistent = buildRecords.stream()
                .filter(record -> record.getBuildConfigurationId().equals(configurationId))
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                .filter(record -> !record.isTemporaryBuild())
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);

        if (!temporaryBuild) {
            // I need only the persistent builds
            return latestPersistent;
        }

        // Get the latest successful temporary build record (of this build configuration) with same alignment preference
        final BuildRecord latestTemporary = buildRecords.stream()
                .filter(record -> record.getBuildConfigurationId().equals(configurationId))
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                .filter(record -> record.isTemporaryBuild())
                .filter(record -> alignmentPreference.equals(record.getAlignmentPreference()))
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);

        if (AlignmentPreference.PREFER_PERSISTENT.equals(alignmentPreference)) {
            // Return the latest persistent if not null, otherwise the latest temporary
            return latestPersistent != null ? latestPersistent : latestTemporary;
        } else {
            // Return the latest temporary if not null, otherwise the latest persistent
            return latestTemporary != null ? latestTemporary : latestPersistent;
        }
    }

    @Override
    public List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId) {
        return data.stream()
                .filter(buildRecord -> buildRecord.getBuildConfigurationId().equals(configurationId))
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildRecord> findIndependentTemporaryBuildsOlderThan(Date date) {
        return null;
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(
            IdRev buildConfigurationAuditedIdRev,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {
        return getLatestSuccessfulBuildRecord(
                buildConfigurationAuditedIdRev,
                data,
                temporaryBuild,
                alignmentPreference);
    }

    public static BuildRecord getLatestSuccessfulBuildRecord(
            IdRev buildConfigurationAuditedIdRev,
            List<BuildRecord> buildRecords,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference) {

        // Get the latest successful persistent build record (of this build configuration revision)
        final BuildRecord latestPersistent = buildRecords.stream()
                .filter(record -> record.getBuildConfigurationAuditedIdRev().equals(buildConfigurationAuditedIdRev))
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                .filter(record -> !record.isTemporaryBuild())
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);

        if (!temporaryBuild) {
            // I need only the persistent builds
            return latestPersistent;
        }

        // Get the latest successful temporary build record (of this build configuration revision) with same alignment
        // preference
        final BuildRecord latestTemporary = buildRecords.stream()
                .filter(record -> record.getBuildConfigurationAuditedIdRev().equals(buildConfigurationAuditedIdRev))
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                .filter(record -> record.isTemporaryBuild())
                .filter(record -> alignmentPreference.equals(record.getAlignmentPreference()))
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);

        if (AlignmentPreference.PREFER_PERSISTENT.equals(alignmentPreference)) {
            // Return the latest persistent if not null, otherwise the latest temporary
            return latestPersistent != null ? latestPersistent : latestTemporary;
        } else {
            // Return the latest temporary if not null, otherwise the latest persistent
            return latestTemporary != null ? latestTemporary : latestPersistent;
        }
    }

    @Override
    public List<BuildRecord> getLatestBuildsForBuildConfigs(List<Integer> configIds) {
        return null;
    }

    @Override
    public BuildRecord save(BuildRecord entity) {
        return super.save(entity);
    }

    @Override
    public List<BuildRecord> queryAll() {
        return super.queryAll();
    }

    @Override
    public Set<BuildRecord> findByBuiltArtifacts(Set<Integer> artifactsId) {

        return data.stream().filter(buildRecord -> {
            Set<Integer> builtArtifactsId = ofNullableCollection(buildRecord.getBuiltArtifacts()).stream()
                    .map(Artifact::getId)
                    .collect(Collectors.toSet());

            // Get the build records which have any built artifact ids corresponding to a list of dependencies
            return !Collections.disjoint(artifactsId, builtArtifactsId);
        }).collect(Collectors.toSet());
    }

    @Override
    public List<BuildRecord> getBuildByCausingRecord(Base32LongID causingRecordId) {
        return null;
    }

    @Override
    public List<Object[]> getAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime, int pageSize, int offset) {
        return null;
    }

    @Override
    public int countAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime) {
        return 0;
    }

}
