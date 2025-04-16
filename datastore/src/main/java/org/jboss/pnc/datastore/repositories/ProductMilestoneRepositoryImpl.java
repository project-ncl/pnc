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
package org.jboss.pnc.datastore.repositories;

import com.google.common.collect.Lists;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
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
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;

import javax.ejb.Stateless;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerReportPredicates.notFromScratchAnalysis;
import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerReportPredicates.notFromDeletedAnalysis;

@Stateless
public class ProductMilestoneRepositoryImpl extends AbstractRepository<ProductMilestone, Integer>
        implements ProductMilestoneRepository {

    public ProductMilestoneRepositoryImpl() {
        super(ProductMilestone.class, Integer.class);
    }

    @Override
    public long countBuiltArtifactsInMilestone(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<Artifact> artifacts = query.from(Artifact.class);
        Join<BuildRecord, org.jboss.pnc.model.ProductMilestone> builtArtifactsMilestones = artifacts
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone);

        query.select(cb.count(artifacts.get(Artifact_.id)));
        query.where(cb.equal(builtArtifactsMilestones.get(ProductMilestone_.id), id));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<Tuple> getArtifactsDeliveredInMilestones(List<Integer> milestoneIds) {
        if (milestoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<DeliverableAnalyzerOperation> deliverableAnalyzerOperations = query
                .from(DeliverableAnalyzerOperation.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestones = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableAnalyzerOperation, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.report);
        Join<DeliverableArtifact, Artifact> artifacts = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.artifacts)
                .join(DeliverableArtifact_.artifact);
        Join<Artifact, TargetRepository> targetRepositories = artifacts.join(Artifact_.targetRepository);

        List<Selection<?>> selects = Lists.newArrayList(
                artifacts.get(Artifact_.id),
                artifacts.get(Artifact_.deployPath),
                targetRepositories.get(TargetRepository_.repositoryType));
        selects.addAll(getArtifactPresenceInMilestonesCaseColumns(cb, productMilestones, milestoneIds));

        query.multiselect(selects);
        query.where(
                getMavenOrNpmArtifactsInMilestonesPredicate(
                        cb,
                        targetRepositories,
                        productMilestones,
                        deliverableAnalyzerReports,
                        milestoneIds));
        query.distinct(true);

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    private List<Selection<Boolean>> getArtifactPresenceInMilestonesCaseColumns(
            CriteriaBuilder cb,
            Path<ProductMilestone> productMilestones,
            List<Integer> milestoneIds) {
        // maps milestone IDs into columns with boolean value whether Artifact was delivered in that Milestone
        return milestoneIds.stream().map(milestoneId -> {
            CriteriaBuilder.SimpleCase<Integer, Boolean> simpleCase = cb
                    .selectCase(productMilestones.get(ProductMilestone_.id));
            simpleCase.when(milestoneId, true);
            simpleCase.otherwise(false);

            return simpleCase;
        }).collect(Collectors.toList());
    }

    private Predicate getMavenOrNpmArtifactsInMilestonesPredicate(
            CriteriaBuilder cb,
            Path<TargetRepository> targetRepositories,
            Path<ProductMilestone> productMilestones,
            Path<DeliverableAnalyzerReport> deliverableAnalyzerReports,
            List<Integer> milestoneIds) {
        Predicate isMavenOrNpmArtifact = targetRepositories.get(TargetRepository_.repositoryType)
                .in(RepositoryType.MAVEN, RepositoryType.NPM);

        return cb.and(
                isMavenOrNpmArtifact,
                productMilestones.get(ProductMilestone_.id).in(milestoneIds),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));
    }

    @Override
    public List<Tuple> getMilestonesSharingDeliveredArtifacts(Integer milestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<DeliverableAnalyzerOperation> deliverableAnalyzerOperationsOuter = query
                .from(DeliverableAnalyzerOperation.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestonesOuter = deliverableAnalyzerOperationsOuter
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableAnalyzerOperation, DeliverableAnalyzerReport> deliverableAnalyzerReportsOuter = deliverableAnalyzerOperationsOuter
                .join(DeliverableAnalyzerOperation_.report);
        Join<DeliverableArtifact, Artifact> artifactsOuter = deliverableAnalyzerReportsOuter
                .join(DeliverableAnalyzerReport_.artifacts)
                .join(DeliverableArtifact_.artifact);

        Subquery<Integer> subquery = query.subquery(Integer.class);

        Root<DeliverableAnalyzerOperation> deliverableAnalyzerOperationsInner = subquery
                .from(DeliverableAnalyzerOperation.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestonesInner = deliverableAnalyzerOperationsInner
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableAnalyzerOperation, DeliverableAnalyzerReport> deliverableAnalyzerReportsInner = deliverableAnalyzerOperationsInner
                .join(DeliverableAnalyzerOperation_.report);
        Join<DeliverableArtifact, Artifact> artifactsInner = deliverableAnalyzerReportsInner
                .join(DeliverableAnalyzerReport_.artifacts)
                .join(DeliverableArtifact_.artifact);

        subquery.select(artifactsInner.get(Artifact_.id)).distinct(true);
        subquery.where(
                cb.and(
                        cb.equal(productMilestonesInner.get(ProductMilestone_.id), milestoneId),
                        notFromScratchAnalysis(cb, deliverableAnalyzerReportsInner),
                        notFromDeletedAnalysis(cb, deliverableAnalyzerReportsInner)));

        query.multiselect(
                productMilestonesOuter.get(ProductMilestone_.id),
                cb.countDistinct(artifactsOuter.get(Artifact_.id)).as(Integer.class));
        query.where(
                cb.and(
                        cb.notEqual(productMilestonesOuter.get(ProductMilestone_.id), milestoneId),
                        artifactsOuter.get(Artifact_.id).in(subquery),
                        notFromScratchAnalysis(cb, deliverableAnalyzerReportsOuter),
                        notFromDeletedAnalysis(cb, deliverableAnalyzerReportsOuter)));
        query.groupBy(productMilestonesOuter.get(ProductMilestone_.id));

        return entityManager.createQuery(query).getResultList();
    }
}
