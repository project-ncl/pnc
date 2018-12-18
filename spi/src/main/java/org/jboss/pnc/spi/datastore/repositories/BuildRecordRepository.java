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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.Date;
import java.util.List;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildRecord} entity.
 */
public interface BuildRecordRepository extends Repository<BuildRecord, Integer> {

    BuildRecord findByIdFetchAllProperties(Integer id);

    /**
     * @return null if record is not found.
     */
    BuildRecord findByIdFetchProperties(Integer id);

    List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildRecord>... predicates);

    List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, List<Predicate<BuildRecord>> andPredicates,
                                                            List<Predicate<BuildRecord>> orPredicates);

    BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId, boolean buildTemporary);

    default BuildRecord getLatestSuccessfulBuildRecord(List<BuildRecord> buildRecords, boolean buildTemporary) {
        return buildRecords.stream()
                .filter(record -> record.getStatus() == BuildStatus.SUCCESS)
                /*
                 * filter out the temporary records if we are building persistent(temporaryBuild == false)
                 * For clarification:
                 *  |     temporaryBuild     | !record.isTemporaryBuild() |  CONDITION IN FILTER  |
                 *  |         TRUE           |          TRUE              |        TRUE           |
                 *  |         TRUE           |          FALSE             |        TRUE           |
                 *  |         FALSE          |          TRUE              |        TRUE           |
                 *  |         FALSE          |          FALSE             |        FALSE          |
                 */
                .filter(record -> buildTemporary || !record.isTemporaryBuild() )
                .sorted((o1, o2) -> -o1.getId().compareTo(o2.getId()))
                .findFirst().orElse(null);
    }

    List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId);

    List<BuildRecord> findTemporaryBuildsOlderThan(Date date);

    GraphWithMetadata<BuildRecord, Integer> getDependencyGraph(Integer buildRecordId);

    BuildRecord getLatestSuccessfulBuildRecord(IdRev buildConfigurationAuditedIdRev, boolean temporaryBuild);
}
