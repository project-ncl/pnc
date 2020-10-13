/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.apache.commons.collections.CollectionUtils;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;

import java.util.Optional;
import java.util.Set;

/**
 * Predicates for {@link org.jboss.pnc.model.Artifact} entity.
 */
public class ArtifactPredicates {

    public static Predicate<Artifact> withBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> cb.equal(root.join(Artifact_.buildRecord).get(BuildRecord_.id), buildRecordId);
    }

    public static Predicate<Artifact> withDependantBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecords = root.join(Artifact_.dependantBuildRecords);
            return cb.equal(buildRecords.get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<Artifact> withDistributedInProductMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            Join<Artifact, ProductMilestone> productMilestones = root.join(Artifact_.distributedInProductMilestones);
            return cb.equal(productMilestones.get(ProductMilestone_.id), productMilestoneId);
        };
    }

    /**
     * @deprecated use defined checksum
     */
    @Deprecated
    public static Predicate<Artifact> withIdentifierAndChecksum(String identifier, String checksum) {
        return withIdentifierAndMd5(identifier, checksum);
    }

    public static Predicate<Artifact> withIdentifierLike(Optional<String> identifierPattern) {
        return (root, query, cb) -> identifierPattern.isPresent()
                ? cb.like(root.get(Artifact_.identifier), "%" + identifierPattern.get().replace("*", "%") + "%")
                : cb.and(); // always true
    }

    public static Predicate<Artifact> withQualityIn(Optional<Set<ArtifactQuality>> qualities) {
        return (root, query, cb) -> qualities.isPresent() && !qualities.get().isEmpty()
                ? root.get(Artifact_.ARTIFACT_QUALITY).in(qualities.get())
                : cb.and();
    }

    public static Predicate<Artifact> withRepositoryType(Optional<RepositoryType> repoType) {
        return (root, query, cb) -> repoType.isPresent()
                ? cb.equal(root.join(Artifact_.targetRepository).get(TargetRepository_.REPOSITORY_TYPE), repoType.get())
                : cb.and();
    }

    public static Predicate<Artifact> withIdentifierAndMd5(String identifier, String md5) {
        return (root, query, cb) -> cb
                .and(cb.equal(root.get(Artifact_.identifier), identifier), cb.equal(root.get(Artifact_.md5), md5));
    }

    public static Predicate<Artifact> withIdentifierAndSha1(String identifier, String sha1) {
        return (root, query, cb) -> cb
                .and(cb.equal(root.get(Artifact_.identifier), identifier), cb.equal(root.get(Artifact_.sha1), sha1));
    }

    public static Predicate<Artifact> withIdentifierAndSha256(String identifier, String sha256) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get(Artifact_.identifier), identifier),
                cb.equal(root.get(Artifact_.sha256), sha256));
    }

    public static Predicate<Artifact> withIdentifierInAndBuilt(Set<String> identifiers) {
        if (CollectionUtils.isEmpty(identifiers)) {
            return Predicate.nonMatching();
        } else {
            return (root, query, cb) -> cb
                    .and(root.get(Artifact_.buildRecord).isNotNull(), root.get(Artifact_.identifier).in(identifiers));
        }
    }

    public static Predicate<Artifact> withSha256In(Set<String> sha256s) {
        return (root, query, cb) -> root.get(Artifact_.sha256).in(sha256s);
    }

    public static Predicate<Artifact> withSha256InAndBuilt(Set<String> sha256s) {
        if (CollectionUtils.isEmpty(sha256s)) {
            return Predicate.nonMatching();
        } else {
            return (root, query, cb) -> cb
                    .and(root.get(Artifact_.buildRecord).isNotNull(), root.get(Artifact_.sha256).in(sha256s));
        }
    }

    public static Predicate<Artifact> withIds(Set<Integer> ids) {
        return (root, query, cb) -> root.get(Artifact_.id).in(ids);
    }

    public static Predicate<Artifact> withOriginUrl(String originUrl) {
        return (root, query, cb) -> cb.equal(root.get(Artifact_.originUrl), originUrl);
    }

    public static Predicate<Artifact> withSha256(Optional<String> sha256) {
        return ((root, query, cb) -> sha256.isPresent() ? cb.equal(root.get(Artifact_.sha256), sha256.get())
                : cb.and());
    }

    public static Predicate<Artifact> withMd5(Optional<String> md5) {
        return ((root, query, cb) -> md5.isPresent() ? cb.equal(root.get(Artifact_.md5), md5.get()) : cb.and());
    }

    public static Predicate<Artifact> withSha1(Optional<String> sha1) {
        return ((root, query, cb) -> sha1.isPresent() ? cb.equal(root.get(Artifact_.sha1), sha1.get()) : cb.and());
    }

    public static Predicate<Artifact> withDistributedInMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            Join<Artifact, ProductMilestone> productMilestone = root.join(Artifact_.distributedInProductMilestones);
            return cb.equal(productMilestone.get(org.jboss.pnc.model.ProductMilestone_.id), productMilestoneId);
        };
    }

}
