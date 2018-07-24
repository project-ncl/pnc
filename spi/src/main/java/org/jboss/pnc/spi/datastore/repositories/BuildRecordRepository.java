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
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.util.graph.Graph;

import java.util.Date;
import java.util.List;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildRecord} entity.
 */
public interface BuildRecordRepository extends Repository<BuildRecord, Integer> {

    BuildRecord findByIdFetchAllProperties(Integer id);

    BuildRecord findByIdFetchProperties(Integer id);

    List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildRecord>... predicates);

    List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, List<Predicate<BuildRecord>> andPredicates,
                                                            List<Predicate<BuildRecord>> orPredicates);

    BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId);

    default BuildRecord getLatestSuccessfulBuildRecord(List<BuildRecord> buildRecords) {
        return buildRecords.stream()
                .filter(b -> b.getStatus() == BuildStatus.SUCCESS)
                .sorted((o1, o2) -> -o1.getId().compareTo(o2.getId()))
                .findFirst().orElse(null);
    }

    List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId);

    List<BuildRecord> findTemporaryBuildsOlderThan(Date date);

    Graph<BuildRecord> getDependencyGraph(Integer buildRecordId);
}
