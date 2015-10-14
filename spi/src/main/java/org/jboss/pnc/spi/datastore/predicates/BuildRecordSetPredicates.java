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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.BuildRecordSet_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import java.util.Set;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildRecordSet} entity.
 */
public class BuildRecordSetPredicates {

    public static Predicate<BuildRecordSet> withPerformedInProductMilestoneId(Integer performedInProductMilestoneId) {
        return (root, query, cb) -> {
            Join<BuildRecordSet, ProductMilestone> performedInProductMilestone = root.join(BuildRecordSet_.performedInProductMilestone);
            return cb.equal(performedInProductMilestone.get(org.jboss.pnc.model.ProductMilestone_.id), performedInProductMilestoneId);
        };
    }

    public static Predicate<BuildRecordSet> withBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> {
            Join<BuildRecordSet, BuildRecord> buildRecords = root.join(BuildRecordSet_.buildRecords);
            return cb.equal(buildRecords.get(org.jboss.pnc.model.BuildRecord_.id), buildRecordId);
        };
    }

    /**
     * Find the BuildRecordSets with an ID contained in the given set.
     * @param buildRecordSetIds The set of IDs to search
     * @return The collection of matching BuildRecordSets
     */
    public static Predicate<BuildRecordSet> withBuildRecordSetIdInSet(Set<Integer> buildRecordSetIds) {
        if (buildRecordSetIds == null || buildRecordSetIds.isEmpty()) {
            // return an always false predicate if there are no build config ids
            return (root, query, cb) -> cb.disjunction();
        } else {
            return (root, query, cb) -> root.get(BuildRecordSet_.id).in(buildRecordSetIds);
        }
    }
}
