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

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.Join;
import java.util.Optional;
import java.util.Set;

/**
 * Predicates for {@link org.jboss.pnc.model.Artifact} entity.
 */
public class ArtifactPredicates {

    public static Predicate<Artifact> withBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecords = root.join(Artifact_.buildRecords);
            return cb.equal(buildRecords.get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<Artifact> withBuildRecordIdMinimized(Integer buildRecordId) {
        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecords = root.join(Artifact_.buildRecords);
            // Needs to have a corresponding contructor in Artifact entity
            // See https://stackoverflow.com/a/12702437
            query.multiselect(root.get(Artifact_.id),
                    root.get(Artifact_.identifier),
                    root.get(Artifact_.artifactQuality),
                    root.get(Artifact_.targetRepository),
                    root.get(Artifact_.md5),
                    root.get(Artifact_.sha1),
                    root.get(Artifact_.sha256),
                    root.get(Artifact_.filename),
                    root.get(Artifact_.deployPath),
                    root.get(Artifact_.importDate),
                    root.get(Artifact_.originUrl),
                    root.get(Artifact_.size)
                    );
            return cb.equal(buildRecords.get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<Artifact> withDependantBuildRecordId(Integer buildRecordId) {
        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecords = root.join(Artifact_.dependantBuildRecords);
            return cb.equal(buildRecords.get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<Artifact> withDependantBuildRecordIdMinimized(Integer buildRecordId) {

        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecords = root.join(Artifact_.dependantBuildRecords);
            // Needs to have a corresponding contructor in Artifact entity
            // See https://stackoverflow.com/a/12702437
            query.multiselect(root.get(Artifact_.id),
                    root.get(Artifact_.identifier),
                    root.get(Artifact_.artifactQuality),
                    root.get(Artifact_.targetRepository),
                    root.get(Artifact_.md5),
                    root.get(Artifact_.sha1),
                    root.get(Artifact_.sha256),
                    root.get(Artifact_.filename),
                    root.get(Artifact_.deployPath),
                    root.get(Artifact_.importDate),
                    root.get(Artifact_.originUrl),
                    root.get(Artifact_.size)
                    );
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

    public static Predicate<Artifact> withIdentifierAndMd5(String identifier, String md5) {
        return (root, query, cb) -> cb.and(cb.equal(root.get(Artifact_.identifier), identifier),
                cb.equal(root.get(Artifact_.md5), md5));
    }

    public static Predicate<Artifact> withIdentifierAndSha1(String identifier, String sha1) {
        return (root, query, cb) -> cb.and(cb.equal(root.get(Artifact_.identifier), identifier),
                cb.equal(root.get(Artifact_.sha1), sha1));
    }

    public static Predicate<Artifact> withIdentifierAndSha256(String identifier, String sha256) {
        return (root, query, cb) -> cb.and(cb.equal(root.get(Artifact_.identifier), identifier),
                cb.equal(root.get(Artifact_.sha256), sha256));
    }

    public static Predicate<Artifact> withSha256In(Set<String> sha256s) {
        return (root, query, cb) -> root.get(Artifact_.sha256).in(sha256s);
    }

    public static Predicate<Artifact> withOriginUrl(String originUrl) {
        return (root, query, cb) -> cb.equal(root.get(Artifact_.originUrl), originUrl);
    }

    public static Predicate<Artifact> withSha256(Optional<String> sha256) {
        return ((root, query, cb) -> sha256.isPresent() ? cb.equal(root.get(Artifact_.sha256), sha256.get()) : cb.and());
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
