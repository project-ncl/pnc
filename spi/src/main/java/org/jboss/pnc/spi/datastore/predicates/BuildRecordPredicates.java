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

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jboss.pnc.api.enums.AlignmentPreference;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigSetRecord_;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildConfigurationSet_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordAttribute;
import org.jboss.pnc.model.BuildRecordAttribute_;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
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

    public static Predicate<BuildRecord> withBuildConfigurationId(Integer configurationId) {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.buildConfigurationId), configurationId);
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdAndStatusExecuted(Integer configurationId) {
        return (root, query, cb) -> cb.and(
                withBuildConfigurationId(configurationId).apply(root, query, cb),
                cb.notEqual(root.get(BuildRecord_.status), BuildStatus.NO_REBUILD_REQUIRED) // TODO 2.0 add CANCEL
        );
    }

    public static Predicate<BuildRecord> withStatus(BuildStatus status) {
        return (root, query, cb) -> (cb.equal(root.get(BuildRecord_.status), status));
    }

    public static Predicate<BuildRecord> withBuildConfigurationIds(Set<Integer> configurationIds) {
        return (root, query, cb) -> root.get(BuildRecord_.buildConfigurationId).in(configurationIds);
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdRev(IdRev idRev) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                cb.equal(root.get(BuildRecord_.buildConfigurationRev), idRev.getRev()));
    }

    public static Predicate<BuildRecord> withSubmitTime(Date submitTime) {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.submitTime), submitTime);
    }

    public static Predicate<BuildRecord> withStartTime(Date startTime) {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.startTime), startTime);
    }

    public static Predicate<BuildRecord> withEndTime(Date endTime) {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.endTime), endTime);
    }

    public static Predicate<BuildRecord> withSuccess() {
        return (root, query, cb) -> cb.equal(root.get(BuildRecord_.status), BuildStatus.SUCCESS);
    }

    /**
     * (related to NCL-5192, NCL-5351)
     *
     * When (re)building a temporary build: - if there are existing *successful* temporary builds having the same idRev,
     * ignore persistent builds - if there are no existing temporary builds having the same idRev, include also
     * persistent builds having the same idRev
     *
     * When (re)building a persistent build: - include only existing persistent builds having the same idRev
     *
     * @param idRev the revision of the build to (re)build
     * @param temporary if requested (re)build is temporary
     * @return Predicate that filters out builds according to description
     */
    public static Predicate<BuildRecord> includeTemporary(IdRev idRev, boolean temporary) {
        return (root, query, cb) -> {
            if (temporary) {
                // Create subquery that counts number of temporary builds for the same BuildConfigurationAudited.idRev
                Subquery<Long> temporaryCount = query.subquery(Long.class);
                Root<BuildRecord> subRoot = temporaryCount.from(BuildRecord.class);
                temporaryCount.select(cb.count(subRoot.get(BuildRecord_.id)));
                temporaryCount.where(
                        cb.and(
                                cb.isTrue(subRoot.get(BuildRecord_.temporaryBuild)),
                                cb.equal(subRoot.get(BuildRecord_.status), BuildStatus.SUCCESS),
                                cb.equal(subRoot.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                                cb.equal(subRoot.get(BuildRecord_.buildConfigurationRev), idRev.getRev())));

                // only false if build is persistent and there are temporaryBuilds present
                return cb
                        .or(cb.isTrue(root.get(BuildRecord_.temporaryBuild)), cb.lessThanOrEqualTo(temporaryCount, 0L));
            }
            return cb.isFalse(root.get(BuildRecord_.temporaryBuild));
        };
    }

    /**
     * (related to NCL-6957)
     *
     * When (re)building a temporary build: include either persistent and temporary builds
     * 
     * When (re)building a persistent build: include only persistent builds
     * 
     * @param temporary if requested (re)build is temporary
     * @return Predicate that filters out builds according to description
     */
    public static Predicate<BuildRecord> includeTemporary(boolean temporary) {
        return (root, query, cb) -> (temporary) ? cb.and() : cb.isFalse(root.get(BuildRecord_.temporaryBuild));
    }

    /**
     * Changed behavior after NCL-6895 Dry-Run implementation
     *
     * When (re)building a persistent build: - include only existing persistent builds having the same idRev
     *
     * When (re)building a temporary PREFER_PERSISTENT build: if there are existing successful persistent builds having
     * the same idRev, ignore temporary builds, otherwise include also successful temporary builds having the same idRev
     * 
     * When (re)building a temporary PREFER_TEMPORARY build: if there are existing successful temporary builds having
     * the same idRev, ignore persistent builds, otherwise include also successful persistent builds having the same
     * idRev
     *
     * @param idRev the revision of the build to (re)build
     * @param temporary if requested (re)build is temporary
     * @param alignmentPreference the alignmentPreference specified in case of temporary builds
     *
     * @return Predicate that filters out builds according to description
     */
    public static Predicate<BuildRecord> includeTemporary(
            IdRev idRev,
            boolean temporary,
            AlignmentPreference alignmentPreference) {
        return (root, query, cb) -> {
            if (temporary) {
                if (AlignmentPreference.PREFER_TEMPORARY.equals(alignmentPreference)) {
                    Subquery<Long> temporaryCount = query.subquery(Long.class);
                    Root<BuildRecord> subRoot = temporaryCount.from(BuildRecord.class);
                    temporaryCount.select(cb.count(subRoot.get(BuildRecord_.id)));
                    temporaryCount.where(
                            cb.and(
                                    cb.isTrue(subRoot.get(BuildRecord_.temporaryBuild)),
                                    cb.equal(subRoot.get(BuildRecord_.status), BuildStatus.SUCCESS),
                                    cb.equal(subRoot.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                                    cb.equal(subRoot.get(BuildRecord_.buildConfigurationRev), idRev.getRev())));

                    return cb.or(
                            cb.isTrue(root.get(BuildRecord_.temporaryBuild)),
                            cb.lessThanOrEqualTo(temporaryCount, 0L));

                } else {
                    Subquery<Long> persistentCount = query.subquery(Long.class);
                    Root<BuildRecord> subRoot = persistentCount.from(BuildRecord.class);
                    persistentCount.select(cb.count(subRoot.get(BuildRecord_.id)));
                    persistentCount.where(
                            cb.and(
                                    cb.isFalse(subRoot.get(BuildRecord_.temporaryBuild)),
                                    cb.equal(subRoot.get(BuildRecord_.status), BuildStatus.SUCCESS),
                                    cb.equal(subRoot.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                                    cb.equal(subRoot.get(BuildRecord_.buildConfigurationRev), idRev.getRev())));

                    return cb.or(
                            cb.isFalse(root.get(BuildRecord_.temporaryBuild)),
                            cb.lessThanOrEqualTo(persistentCount, 0L));
                }
            }
            // Include only persistent builds
            return cb.isFalse(root.get(BuildRecord_.temporaryBuild));
        };
    }

    /**
     * Changed behavior after NCL-6895 Dry-Run implementation
     *
     * When (re)building a persistent build: - include only existing persistent builds having the same build config
     *
     * When (re)building a temporary PREFER_PERSISTENT build: if there are existing successful persistent builds having
     * the same build config, ignore temporary builds, otherwise include also successful temporary builds having the
     * same build config
     * 
     * When (re)building a temporary PREFER_TEMPORARY build: if there are existing successful temporary builds having
     * the same build config, ignore persistent builds, otherwise include also successful persistent builds having the
     * same build config
     *
     * @param configurationId the build config of the build to (re)build
     * @param temporary if requested (re)build is temporary
     * @param alignmentPreference the alignmentPreference specified in case of temporary builds
     *
     * @return Predicate that filters out builds according to description
     */
    public static Predicate<BuildRecord> includeTemporary(
            Integer configurationId,
            boolean temporary,
            AlignmentPreference alignmentPreference) {
        return (root, query, cb) -> {
            if (temporary) {
                if (AlignmentPreference.PREFER_TEMPORARY.equals(alignmentPreference)) {
                    Subquery<Long> temporaryCount = query.subquery(Long.class);
                    Root<BuildRecord> subRoot = temporaryCount.from(BuildRecord.class);
                    temporaryCount.select(cb.count(subRoot.get(BuildRecord_.id)));
                    temporaryCount.where(
                            cb.and(
                                    cb.isTrue(subRoot.get(BuildRecord_.temporaryBuild)),
                                    cb.equal(subRoot.get(BuildRecord_.status), BuildStatus.SUCCESS),
                                    cb.equal(subRoot.get(BuildRecord_.buildConfigurationId), configurationId)));

                    return cb.or(
                            cb.isTrue(root.get(BuildRecord_.temporaryBuild)),
                            cb.lessThanOrEqualTo(temporaryCount, 0L));

                } else {
                    Subquery<Long> persistentCount = query.subquery(Long.class);
                    Root<BuildRecord> subRoot = persistentCount.from(BuildRecord.class);
                    persistentCount.select(cb.count(subRoot.get(BuildRecord_.id)));
                    persistentCount.where(
                            cb.and(
                                    cb.isFalse(subRoot.get(BuildRecord_.temporaryBuild)),
                                    cb.equal(subRoot.get(BuildRecord_.status), BuildStatus.SUCCESS),
                                    cb.equal(subRoot.get(BuildRecord_.buildConfigurationId), configurationId)));

                    return cb.or(
                            cb.isFalse(root.get(BuildRecord_.temporaryBuild)),
                            cb.lessThanOrEqualTo(persistentCount, 0L));
                }
            }
            // Include only persistent builds
            return cb.isFalse(root.get(BuildRecord_.temporaryBuild));
        };
    }

    public static Predicate<BuildRecord> withBuildConfigSetId(Integer buildConfigSetId) {
        return (root, query, cb) -> {

            Join<BuildRecord, BuildConfigSetRecord> builtConfigSetRecord = root.join(BuildRecord_.buildConfigSetRecord);

            Join<BuildConfigSetRecord, BuildConfigurationSet> buildConfigSet = builtConfigSetRecord
                    .join(BuildConfigSetRecord_.buildConfigurationSet);

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

    public static Predicate<BuildRecord> withBuildConfigSetRecordId(Base32LongID buildConfigSetRecordId) {
        return (root, query, cb) -> {
            Join<BuildRecord, BuildConfigSetRecord> joinedConfigSetRecord = root
                    .join(BuildRecord_.buildConfigSetRecord);
            return cb.equal(joinedConfigSetRecord.get(BuildConfigSetRecord_.id), buildConfigSetRecordId);
        };
    }

    public static Predicate<BuildRecord> withBuildConfigurationIdRev(List<IdRev> buildConfigurationsWithIdRevs) {
        if (buildConfigurationsWithIdRevs.isEmpty()) {
            return Predicate.nonMatching();
        }

        List<String> idRevs = buildConfigurationsWithIdRevs.stream()
                .map(idRev -> idRev.getId() + "-" + idRev.getRev())
                .collect(Collectors.toList());

        return (root, query, cb) -> {
            Expression<String> concat = cb.concat(root.get(BuildRecord_.buildConfigurationId).as(String.class), "-");
            Expression<String> buildRecordIdRev = cb
                    .concat(concat, root.get(BuildRecord_.buildConfigurationRev).as(String.class));
            logger.debug("Searching for BuildRecords with {}", idRevs);
            return buildRecordIdRev.in(idRevs);
        };
    }

    public static Predicate<BuildRecord> exceptBuildConfigurationIdRev(List<IdRev> buildConfigurationsWithIdRevs) {
        return (root, query, cb) -> cb
                .not(withBuildConfigurationIdRev(buildConfigurationsWithIdRevs).apply(root, query, cb));
    }

    public static Predicate<BuildRecord> withArtifactDistributedInMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            SetJoin<BuildRecord, Artifact> builtArtifacts = root.join(BuildRecord_.builtArtifacts);
            SetJoin<Artifact, ProductMilestone> productMilestones = builtArtifacts
                    .join(Artifact_.deliveredInProductMilestones);
            return cb.equal(productMilestones.get(ProductMilestone_.id), productMilestoneId);
        };
    }

    public static Predicate<BuildRecord> withArtifactProduced(Integer artifactId) {
        return (root, query, cb) -> {
            SetJoin<BuildRecord, Artifact> builtArtifacts = root.join(BuildRecord_.builtArtifacts);
            return cb.equal(builtArtifacts.get(Artifact_.id), artifactId);
        };
    }

    public static Predicate<BuildRecord> withArtifactDependency(Integer artifactId) {
        return (root, query, cb) -> {
            SetJoin<BuildRecord, Artifact> dependencies = root.join(BuildRecord_.dependencies);
            return cb.equal(dependencies.get(Artifact_.id), artifactId);
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
            SetJoin<BuildRecord, BuildRecordAttribute> joinAttributer = root.join(BuildRecord_.attributes);
            return query
                    .where(
                            cb.and(cb.equal(joinAttributer.get(BuildRecordAttribute_.key), key)),
                            cb.equal(joinAttributer.get(BuildRecordAttribute_.value), value))
                    .getRestriction();
        };
    }

    public static Predicate<BuildRecord> withOneOfCombinationOfAttributes(Set<Set<KV<String, String>>> attributes) {
        return (root, query, cb) -> {
            SetJoin<BuildRecord, BuildRecordAttribute> attributeSet = root.join(BuildRecord_.attributes);

            return attributes.stream()
                    .map(
                            combination -> combination.stream()
                                    .map(
                                            attribute -> cb.and(
                                                    cb.equal(
                                                            attributeSet.get(BuildRecordAttribute_.key),
                                                            attribute.getKey()),
                                                    cb.equal(
                                                            attributeSet.get(BuildRecordAttribute_.value),
                                                            attribute.getValue())))
                                    .reduce(cb::and)
                                    .orElseThrow(() -> new RuntimeException("Attributes are empty or incorrect.")))
                    .reduce(cb::or)
                    .orElseThrow(() -> new RuntimeException("Attributes are empty or incorrect."));
        };
    }

    @AllArgsConstructor
    public static class KV<K, V> {
        private final @Getter K key;
        private final @Getter V value;
    }

    public static Predicate<BuildRecord> withoutAttribute(String key) {
        return (root, query, cb) -> {
            Subquery<String> subquery = query.subquery(String.class);
            Root<BuildRecordAttribute> subRoot = subquery.from(BuildRecordAttribute.class);
            subquery.select(subRoot.get(BuildRecordAttribute_.key));
            subquery.where(
                    cb.and(
                            cb.equal(subRoot.get(BuildRecordAttribute_.key), key),
                            cb.equal(root.get(BuildRecord_.id), subRoot.get(BuildRecordAttribute_.buildRecord))));
            return query.where(cb.not(cb.exists(subquery))).getRestriction();
        };
    }

    public static Predicate<BuildRecord> withoutImplicitDependants() {
        return (root, query, cb) -> {
            final SetJoin<BuildRecord, Artifact> builtArtifacts = root.join(BuildRecord_.builtArtifacts, JoinType.LEFT);
            final SetJoin<Artifact, BuildRecord> buildRecordImplicitDependants = builtArtifacts
                    .join(Artifact_.dependantBuildRecords, JoinType.LEFT);

            query.groupBy(root.get(BuildRecord_.id));

            query.having(
                    cb.and(
                            // avoid overwriting having clauses from previous predicates
                            query.getGroupRestriction() == null ? cb.and() : query.getGroupRestriction(),
                            // All entries in a column have to be NULL which is equivalent to count being 0 (aggregation
                            // functions ignore NULLs)
                            cb.equal(cb.count(buildRecordImplicitDependants.get(BuildRecord_.id)), 0)));
            return cb.or(
                    // build can depend on itself
                    cb.notEqual(root.get(BuildRecord_.id), buildRecordImplicitDependants.get(BuildRecord_.id)),
                    cb.isNull(buildRecordImplicitDependants.get(BuildRecord_.id)));
        };
    }

    public static Predicate<BuildRecord> buildFinishedBefore(Date date) {
        return (root, query, cb) -> cb.lessThan(root.get(BuildRecord_.endTime), date);
    }

    public static Predicate<BuildRecord> temporaryBuild() {
        return (root, query, cb) -> cb.isTrue(root.get(BuildRecord_.temporaryBuild));
    }

    public static Predicate<BuildRecord> withCausingBuildRecordId(Base32LongID buildRecordId) {
        return (root, query, cb) -> {
            Join<BuildRecord, BuildRecord> join = root.join(BuildRecord_.noRebuildCause);
            return cb.equal(join.get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<BuildRecord> withoutLinkedNRRRecordOlderThanTimestamp(Date date) {
        return (root, query, cb) -> {
            // subquery returns amount of records older than date
            Subquery<Long> newNRRRecordCount = query.subquery(Long.class);
            Root<BuildRecord> subRoot = newNRRRecordCount.from(BuildRecord.class);
            newNRRRecordCount.select(cb.count(subRoot.get(BuildRecord_.id)));
            newNRRRecordCount.where(
                    cb.and(
                            cb.equal(subRoot.get(BuildRecord_.noRebuildCause), root.get(BuildRecord_.id)),
                            cb.greaterThan(subRoot.get(BuildRecord_.endTime), date)));

            return cb.equal(newNRRRecordCount, 0);
        };
    }

    public static Predicate<BuildRecord> withIds(Set<Base32LongID> ids) {
        if (ids.isEmpty()) {
            return (root, query, cb) -> cb.or();
        }
        return (root, query, cb) -> root.get(BuildRecord_.id).in(ids);
    }
}
