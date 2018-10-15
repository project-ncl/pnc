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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigSetRecord_;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildConfigurationSet_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.SetJoin;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Predicates for {@link org.jboss.pnc.model.BuildRecord} entity.
 */
public class BuildRecordPredicates {

    private static Logger logger = LoggerFactory.getLogger(BuildRecordPredicates.class);

    public static Predicate<BuildRecord> withBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> cb.equal(root.get(org.jboss.pnc.model.BuildRecord_.id), buildRecordId);
    }

    public static Predicate<BuildRecord> withBuildConfigurationId(Integer configurationId) {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.buildConfigurationId), configurationId);
    }

    public static Predicate<BuildRecord> withBuildConfigurationIds(Set<Integer> configurationIds) {
        return (root, query, cb) -> root.get(BuildRecord_.buildConfigurationId).in(configurationIds);
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdRev(IdRev idRev) {
        return (root, query, cb) ->
                cb.and(cb.equal(root.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                       cb.equal(root.get(BuildRecord_.buildConfigurationRev), idRev.getRev()));
    }

    public static Predicate<BuildRecord> withSuccess() {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.status), BuildStatus.SUCCESS);
    }

    public static Predicate<BuildRecord> withBuildConfigSetId(Integer buildConfigSetId) {
        return (root, query, cb) -> {

            Join<BuildRecord, BuildConfigSetRecord> builtConfigSetRecord = root.join(BuildRecord_.buildConfigSetRecord);

            Join<BuildConfigSetRecord, BuildConfigurationSet> buildConfigSet = builtConfigSetRecord.join(BuildConfigSetRecord_.buildConfigurationSet);

            return cb.equal(buildConfigSet.get(BuildConfigurationSet_.id), buildConfigSetId);
        };
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdInSet(Collection<Integer> buildConfigurationIds) {
        if (buildConfigurationIds.isEmpty()) {
            // return an always false predicate if there are no build config ids
            return (root, query, cb) -> cb.disjunction();
        } else {
            return (root, query, cb) -> root.get(BuildRecord_.buildConfigurationId).in(buildConfigurationIds);
        }
    }

    public static Predicate<BuildRecord> withBuildConfigSetRecordId(Integer buildConfigSetRecordId) {
        return (root, query, cb) -> {
            Join<BuildRecord, BuildConfigSetRecord> joinedConfigSetRecord = root.join(BuildRecord_.buildConfigSetRecord);
            return cb.equal(joinedConfigSetRecord.get(BuildConfigSetRecord_.id), buildConfigSetRecordId);
        };
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdRev(List<IdRev> buildConfigurationsWithIdRevs) {
        if (buildConfigurationsWithIdRevs.isEmpty()) {
            return Predicate.nonMatching();
        }

        List<String> idRevs = buildConfigurationsWithIdRevs.stream()
                .map(idRev -> idRev.getId() + "-" + idRev.getRev()).collect(Collectors.toList());

        return (root, query, cb) -> {
            Expression<String> concat = cb.concat(root.get(BuildRecord_.buildConfigurationId).as(String.class), "-");
            Expression<String> buildRecordIdRev = cb.concat(concat, root.get(BuildRecord_.buildConfigurationRev).as(String.class));
            logger.debug("Searching for BuildRecords with {}", idRevs);
            return buildRecordIdRev.in(idRevs);
        };
    }

    public static Predicate<BuildRecord> withArtifactDistributedInMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            SetJoin<BuildRecord, Artifact> builtArtifacts = root.join(BuildRecord_.builtArtifacts);
            SetJoin<Artifact, ProductMilestone> productMilestones = builtArtifacts.join(Artifact_.distributedInProductMilestones);
            return cb.equal(productMilestones.get(ProductMilestone_.id), productMilestoneId);
        };
    }

    public static Predicate<BuildRecord> withPerformedInMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            Join<BuildRecord, ProductMilestone> productMilestone = root.join(BuildRecord_.productMilestone);
            return cb.equal(productMilestone.get(org.jboss.pnc.model.ProductMilestone_.id), productMilestoneId);
        };
    }

    public static Predicate<BuildRecord> withUserId(Integer userId) {
        return (root, query, cb) -> {
            Join<BuildRecord, User> buildRecordJoinedUsers = root.join(BuildRecord_.user);
            return cb.equal(buildRecordJoinedUsers.get(org.jboss.pnc.model.User_.id), userId);
        };
    }

    public static Predicate<BuildRecord> withAttribute(String key, String value) {
        return (root, query, cb) -> {
            MapJoin<Object, Object, Object> mapJoinAttributes = root.joinMap(BuildRecord_.attributes.getName());
            return query.where(cb.and(cb.equal(mapJoinAttributes.key(), key), cb.equal(mapJoinAttributes.value(), value))).getRestriction();
        };
    }

    public static Predicate<BuildRecord> buildFinishedBefore(Date date) {
        return (root, query, cb) -> cb.lessThan(root.get(BuildRecord_.endTime), date);
    }

    public static Predicate<BuildRecord> temporaryBuild() {
        return (root, query, cb) -> cb.isTrue(root.get(BuildRecord_.temporaryBuild));
    }
}
