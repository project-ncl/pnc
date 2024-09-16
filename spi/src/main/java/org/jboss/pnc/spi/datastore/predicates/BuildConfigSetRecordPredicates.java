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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigSetRecord_;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildConfigurationSet_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.Date;
import java.util.EnumSet;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildConfigSetRecord} entity.
 */
public class BuildConfigSetRecordPredicates {

    public static Predicate<BuildConfigSetRecord> withBuildConfigSetId(Integer buildConfigSetId) {
        return (root, query, cb) -> {
            Join<BuildConfigSetRecord, BuildConfigurationSet> buildConfigurationSet = root
                    .join(BuildConfigSetRecord_.buildConfigurationSet);
            return cb.equal(buildConfigurationSet.get(BuildConfigurationSet_.id), buildConfigSetId);
        };
    }

    public static Predicate<BuildConfigSetRecord> buildFinishedBefore(Date date) {
        return (root, query, cb) -> cb.and(
                cb.isNotNull(root.get(BuildConfigSetRecord_.endTime)),
                cb.lessThan(root.get(BuildConfigSetRecord_.endTime), date));
    }

    public static Predicate<BuildConfigSetRecord> temporaryBuild() {
        return (root, query, cb) -> cb.isTrue(root.get(BuildConfigSetRecord_.temporaryBuild));
    }

    public static Predicate<BuildConfigSetRecord> inStates(EnumSet<BuildStatus> inProgressStates) {
        return (root, query, cb) -> root.get(BuildConfigSetRecord_.status).in(inProgressStates);
    }
}
