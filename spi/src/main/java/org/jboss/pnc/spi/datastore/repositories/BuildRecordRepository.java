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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.dto.insights.BuildRecordInsights;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildRecord} entity.
 */
public interface BuildRecordRepository extends Repository<BuildRecord, Base32LongID> {

    BuildRecord findByIdFetchAllProperties(Base32LongID id);

    /**
     * @return null if record is not found.
     */
    BuildRecord findByIdFetchProperties(Base32LongID id);

    BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId, boolean buildTemporary);

    default BuildRecord getLatestSuccessfulBuildRecord(List<BuildRecord> buildRecords, boolean buildTemporary) {
        final boolean containsTemporary = buildRecords.stream()
                .anyMatch(record -> (record.isTemporaryBuild() && record.getStatus() == BuildStatus.SUCCESS));

        // Include temporary builds if you are building temporary and don't if building persistent
        final boolean includeTemporary = buildTemporary;
        // NCL-5192
        // Exclude persistent builds if you are building temporary and there are some temporary builds built
        final boolean excludePersistent = buildTemporary && containsTemporary;
        final boolean includePersistent = !excludePersistent;

        return buildRecords.stream()
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                // First part includes temporary BRs and second part includes persistent BRs
                .filter(
                        record -> (includeTemporary && record.isTemporaryBuild())
                                || (includePersistent && !record.isTemporaryBuild()))
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);
    }

    List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId);

    List<BuildRecord> queryWithBuildConfigurationSetRecordId(Long bcsrId);

    List<BuildRecord> findIndependentTemporaryBuildsOlderThan(Date date);

    BuildRecord getLatestSuccessfulBuildRecord(IdRev buildConfigurationAuditedIdRev, boolean temporaryBuild);

    // Used in Step 1 in DefaultDataStore -> requiresRebuild
    BuildRecord getAnyLatestSuccessfulBuildRecordWithRevision(
            IdRev buildConfigurationAuditedIdRev,
            boolean temporaryBuild);

    // Used in Step 2 in DefaultDataStore -> requiresRebuild
    BuildRecord getAnyLatestSuccessfulBuildRecordWithBuildConfig(Integer configurationId, boolean temporaryBuild);

    BuildRecord getPreferredLatestSuccessfulBuildRecordWithRevision(
            IdRev buildConfigurationAuditedIdRev,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference);

    // Used in Steps 3 and 4 in DefaultDataStore -> requiresRebuild
    BuildRecord getPreferredLatestSuccessfulBuildRecordWithBuildConfig(
            Integer configurationId,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference);

    List<BuildRecord> getLatestBuildsForBuildConfigs(List<Integer> configIds);

    Set<BuildRecord> findByBuiltArtifacts(Set<Integer> artifactsId);

    List<BuildRecord> getBuildByCausingRecord(Base32LongID causingRecordId);

    List<Object[]> getAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime, int pageSize, int offset);

    int countAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime);
}
