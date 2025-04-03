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

import org.apache.commons.collections.CollectionUtils;
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jboss.pnc.model.TargetRepository_;

import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerReportPredicates.notFromDeletedAnalysis;
import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerReportPredicates.notFromScratchAnalysis;

/**
 * Predicates for {@link org.jboss.pnc.model.Artifact} entity.
 */
@SuppressWarnings("deprecation")
public class ArtifactPredicates {

    public static Predicate<Artifact> withArtifactQualityIn(Set<ArtifactQuality> qualities) {
        return (root, query, cb) -> root.get(Artifact_.artifactQuality).in(qualities);
    }

    public static Predicate<Artifact> withBuildCategoryIn(Set<BuildCategory> categories) {
        return (root, query, cb) -> root.get(Artifact_.buildCategory).in(categories);
    }

    public static Predicate<Artifact> withBuildRecordId(Base32LongID buildRecordId) {
        return (root, query, cb) -> cb.equal(root.join(Artifact_.buildRecord).get(BuildRecord_.id), buildRecordId);
    }

    public static Predicate<Artifact> withDependantBuildRecordId(Base32LongID buildRecordId) {
        return (root, query, cb) -> {
            Join<Artifact, BuildRecord> buildRecords = root.join(Artifact_.dependantBuildRecords);
            return cb.equal(buildRecords.get(BuildRecord_.id), buildRecordId);
        };
    }

    public static Predicate<Artifact> withDeliveredInProductMilestone(Integer productMilestoneId) {
        return (root, query, cb) -> {
            Root<DeliverableArtifact> deliverableArtifact = query.from(DeliverableArtifact.class);
            Join<DeliverableArtifact, DeliverableAnalyzerReport> report = deliverableArtifact
                    .join(DeliverableArtifact_.report);
            Join<DeliverableAnalyzerReport, DeliverableAnalyzerOperation> operation = report
                    .join(DeliverableAnalyzerReport_.operation);

            return cb.and(
                    DeliverableAnalyzerReportPredicates.notFromDeletedAnalysis(cb, report),
                    DeliverableAnalyzerReportPredicates.notFromScratchAnalysis(cb, report),
                    cb.equal(
                            operation.get(DeliverableAnalyzerOperation_.productMilestone).get(ProductMilestone_.id),
                            productMilestoneId),
                    cb.equal(
                            deliverableArtifact.get(DeliverableArtifact_.artifact).get(Artifact_.id),
                            root.get(Artifact_.id)));
        };
    }

    public static Predicate<Artifact> withTargetRepositoryId(Integer targetRepositoryId) {
        return (root, query, cb) -> cb
                .equal(root.join(Artifact_.targetRepository).get(TargetRepository_.id), targetRepositoryId);
    }

    public static Predicate<Artifact> withTargetRepositoryRepositoryType(RepositoryType repoType) {
        return (root, query, cb) -> cb
                .equal(root.join(Artifact_.targetRepository).get(TargetRepository_.repositoryType), repoType);
    }

    /**
     * @deprecated use defined checksum
     */
    @Deprecated
    public static Predicate<Artifact> withIdentifierAndChecksum(String identifier, String checksum) {
        return withIdentifierAndMd5(identifier, checksum);
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

    public static Predicate<Artifact> withIdentifierLike(String identifierPattern) {
        return (root, query, cb) -> cb.like(root.get(Artifact_.identifier), identifierPattern);
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
        if (ids.isEmpty()) {
            return (root, query, cb) -> cb.or();
        }
        return (root, query, cb) -> root.get(Artifact_.id).in(ids);
    }

    public static Predicate<Artifact> withOriginUrl(String originUrl) {
        return (root, query, cb) -> cb.equal(root.get(Artifact_.originUrl), originUrl);
    }

    public static Predicate<Artifact> withSha256(Optional<String> sha256) {
        return ((root, query, cb) -> sha256.isPresent() ? cb.equal(root.get(Artifact_.sha256), sha256.get())
                : cb.and());
    }

    public static Predicate<Artifact> withIdentifierAndSha256(Iterable<Artifact.IdentifierSha256> identifierSha256Set) {
        return ((root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            for (Artifact.IdentifierSha256 identifierSha256 : identifierSha256Set) {
                predicates.add(
                        cb.and(
                                cb.equal(root.get(Artifact_.identifier), identifierSha256.getIdentifier()),
                                cb.equal(root.get(Artifact_.sha256), identifierSha256.getSha256())));
            }
            return cb.or(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        });
    }

    public static Predicate<Artifact> withMd5(Optional<String> md5) {
        return ((root, query, cb) -> md5.isPresent() ? cb.equal(root.get(Artifact_.md5), md5.get()) : cb.and());
    }

    public static Predicate<Artifact> withSha1(Optional<String> sha1) {
        return ((root, query, cb) -> sha1.isPresent() ? cb.equal(root.get(Artifact_.sha1), sha1.get()) : cb.and());
    }

    public static Predicate<Artifact> withPurl(String purl) {
        return (root, query, cb) -> cb.equal(root.get(Artifact_.purl), purl);
    }

    public static javax.persistence.criteria.Predicate notProducedInBuild(
            CriteriaBuilder cb,
            Path<Artifact> artifacts) {
        // build record being NULL is not enough because of Brew builds
        return artifacts.get(Artifact_.artifactQuality).in(ArtifactQuality.IMPORTED, ArtifactQuality.DELETED);
    }

    public static Predicate<Artifact> deliveredInMilestones(Integer milestone1Id, Integer milestone2Id) {
        return (root, query, cb) -> {
            Subquery<Artifact> subquery = query.subquery(Artifact.class);

            Root<DeliverableAnalyzerOperation> deliverableAnalyzerOperations = subquery
                    .from(DeliverableAnalyzerOperation.class);
            Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestones = deliverableAnalyzerOperations
                    .join(DeliverableAnalyzerOperation_.productMilestone);
            Join<DeliverableAnalyzerOperation, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableAnalyzerOperations
                    .join(DeliverableAnalyzerOperation_.report);
            Join<DeliverableArtifact, Artifact> artifacts = deliverableAnalyzerReports
                    .join(DeliverableAnalyzerReport_.artifacts)
                    .join(DeliverableArtifact_.artifact);

            subquery.select(artifacts);
            subquery.where(
                    cb.and(
                            productMilestones.get(ProductMilestone_.id).in(List.of(milestone1Id, milestone2Id)),
                            notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                            notFromDeletedAnalysis(cb, deliverableAnalyzerReports)));
            subquery.groupBy(artifacts);
            // delivered in both milestones
            subquery.having(cb.equal(cb.countDistinct(productMilestones.get(ProductMilestone_.id)), cb.literal(2L)));

            return root.in(subquery);
        };
    }
}
