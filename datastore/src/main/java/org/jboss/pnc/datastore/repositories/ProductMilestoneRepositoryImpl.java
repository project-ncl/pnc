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
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;

import javax.ejb.Stateless;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<Tuple> getArtifactsDeliveredInMilestonesGroupedByPrefix(PageInfo pageInfo, List<Integer> milestoneIds) {
        if (milestoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createQuery(Tuple.class);

        Root<DeliverableAnalyzerOperation> deliverableAnalyzerOperations = query
                .from(DeliverableAnalyzerOperation.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestones = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableArtifact, Artifact> artifacts = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.report)
                .join(DeliverableAnalyzerReport_.artifacts)
                .join(DeliverableArtifact_.artifact);

        Expression<String> artifactIdentifierPrefix = getArtifactIdentifierPrefixCriteriaExpression(cb, artifacts);

        List<Selection<?>> selects = Lists.newArrayList(artifactIdentifierPrefix);
        List<Selection<String>> artifactVersionsInMilestones = (milestoneIds.stream().map(milestoneId -> {
            Expression<String> artifactId = artifacts.get(Artifact_.id).as(String.class);
            Expression<String> artifactIdentifier = artifacts.get(Artifact_.identifier);

            Expression<String> artifactVersion = cb
                    .substring(artifactIdentifier, cb.locate(artifactIdentifier, "redhat-"));
            Expression<String> artifactVersionWithId = cb.concat(artifactId, cb.concat(":", artifactVersion));

            CriteriaBuilder.SimpleCase<Integer, String> simpleCase = cb
                    .selectCase(productMilestones.get(ProductMilestone_.id));
            simpleCase.when(milestoneId, artifactVersionWithId);
            simpleCase.otherwise("");

            return cb.function("string_agg", String.class, cb.nullif(simpleCase, cb.literal("")));
        }).collect(Collectors.toList()));
        selects.addAll(artifactVersionsInMilestones);

        query.multiselect(selects);
        query.where(getArtifactsDeliveredInMilestonesCriteriaPredicate(cb, artifacts, productMilestones, milestoneIds));
        query.groupBy(artifactIdentifierPrefix);
        query.having(
                cb.greaterThanOrEqualTo(cb.countDistinct(productMilestones.get(ProductMilestone_.id)), cb.literal(2L)));

        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        if (pageInfo != null) {
            typedQuery.setFirstResult(pageInfo.getElementOffset());
            typedQuery.setMaxResults(pageInfo.getPageSize());
        }

        return typedQuery.getResultList();
    }

    @Override
    public int countDeliveredArtifactPrefixesInMilestones(List<Integer> milestoneIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);

        Root<DeliverableAnalyzerOperation> deliverableAnalyzerOperations = query
                .from(DeliverableAnalyzerOperation.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> productMilestones = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableArtifact, Artifact> artifacts = deliverableAnalyzerOperations
                .join(DeliverableAnalyzerOperation_.report)
                .join(DeliverableAnalyzerReport_.artifacts)
                .join(DeliverableArtifact_.artifact);

        Expression<String> artifactIdentifierPrefix = getArtifactIdentifierPrefixCriteriaExpression(cb, artifacts);

        query.select(artifactIdentifierPrefix);
        query.where(getArtifactsDeliveredInMilestonesCriteriaPredicate(cb, artifacts, productMilestones, milestoneIds));
        query.groupBy(artifactIdentifierPrefix);
        query.having(
                cb.greaterThanOrEqualTo(cb.countDistinct(productMilestones.get(ProductMilestone_.id)), cb.literal(2L)));

        return entityManager.createQuery(query).getResultList().size();
    }

    private Expression<String> getArtifactIdentifierPrefixCriteriaExpression(
            CriteriaBuilder cb,
            Path<Artifact> artifacts) {
        Expression<String> artifactIdentifier = artifacts.get(Artifact_.identifier);
        Expression<Integer> prefixStartIndex = cb.literal(1);
        Expression<Integer> redhatSubstringPosition = cb.function("redhat-locate", Integer.class, artifactIdentifier);
        Expression<Integer> prefixEndIndex = cb.diff(redhatSubstringPosition, cb.literal(1));

        return cb.substring(artifactIdentifier, prefixStartIndex, prefixEndIndex);
    }

    private javax.persistence.criteria.Predicate getArtifactsDeliveredInMilestonesCriteriaPredicate(
            CriteriaBuilder cb,
            Path<Artifact> artifacts,
            Path<ProductMilestone> productMilestones,
            List<Integer> milestoneIds) {
        return cb.and(
                cb.like(artifacts.get(Artifact_.identifier), "%:%:%:%.redhat-%"),
                cb.notLike(artifacts.get(Artifact_.identifier), "%:%:%:%:%"),
                productMilestones.get(ProductMilestone_.id).in(milestoneIds));
    }
};
