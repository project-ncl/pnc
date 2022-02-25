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

    List<BuildRecord> queryWithPredicatesUsingCursor(
            PageInfo pageInfo,
            SortInfo sortInfo,
            Predicate<BuildRecord>... predicates);

    List<BuildRecord> queryWithPredicatesUsingCursor(
            PageInfo pageInfo,
            SortInfo sortInfo,
            List<Predicate<BuildRecord>> andPredicates,
            List<Predicate<BuildRecord>> orPredicates);

    BuildRecord getLatestSuccessfulBuildRecord(
            Integer configurationId,
            boolean buildTemporary,
            AlignmentPreference alignmentPreference);

    default BuildRecord getLatestSuccessfulBuildRecord(
            List<BuildRecord> buildRecords,
            boolean buildTemporary,
            AlignmentPreference alignmentPreference) {

        // Get the latest successful persistent build record (of this build configuration)
        final BuildRecord latestPersistent = buildRecords.stream()
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                .filter(record -> !record.isTemporaryBuild())
                .max(Comparator.comparing(BuildRecord::getSubmitTime))
                .orElse(null);

        if (!buildTemporary) {
            // I need only the persistent builds
            return latestPersistent;
        }

        // Get the latest successful temporary build record (of this build configuration) with same alignment preference
        final BuildRecord latestTemporary = buildRecords.stream()
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

    List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId);

    List<BuildRecord> findIndependentTemporaryBuildsOlderThan(Date date);

    BuildRecord getLatestSuccessfulBuildRecord(
            IdRev buildConfigurationAuditedIdRev,
            boolean temporaryBuild,
            AlignmentPreference alignmentPreference);

    List<BuildRecord> getLatestBuildsForBuildConfigs(List<Integer> configIds);

    Set<BuildRecord> findByBuiltArtifacts(Set<Integer> artifactsId);

    List<BuildRecord> getBuildByCausingRecord(Base32LongID causingRecordId);

    List<Object[]> getAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime, int pageSize, int offset);

    int countAllBuildRecordInsightsNewerThanTimestamp(Date lastupdatetime);
}
