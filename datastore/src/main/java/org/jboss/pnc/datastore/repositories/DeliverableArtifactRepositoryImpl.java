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

import org.jboss.pnc.common.Maps;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactPK;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactRepository;

import javax.ejb.Stateless;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import java.util.EnumMap;
import java.util.List;

@Stateless
public class DeliverableArtifactRepositoryImpl extends AbstractRepository<DeliverableArtifact, DeliverableArtifactPK>
        implements DeliverableArtifactRepository {

    public DeliverableArtifactRepositoryImpl() {
        super(DeliverableArtifact.class, DeliverableArtifactPK.class);
    }

    @Override
    public long countDeliveredArtifactsBuiltInThisMilestone(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Path<ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts.join(Artifact_.buildRecord)
                .get(BuildRecord_.productMilestone);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about delivered artifacts from the milestone given by id
                // 2) only built delivered artifacts built in this milestone
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.equal(builtDeliveredArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInOtherMilestones(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Path<ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts.get(Artifact_.buildRecord)
                .get(BuildRecord_.productMilestone);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about artifacts from the milestone given by id
                // 2) only built delivered artifacts from this product
                // 3) only built delivered artifacts *not* built in this milestone
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.equal(
                        builtDeliveredArtifactsMilestone.get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product),
                        deliverableArtifactsMilestone.get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product)),
                cb.notEqual(builtDeliveredArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltByOtherProducts(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Path<ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts.get(Artifact_.buildRecord)
                .get(BuildRecord_.productMilestone);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about artifacts from the milestone given by id
                // 2) only built delivered artifacts *not* from this product
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.notEqual(
                        builtDeliveredArtifactsMilestone.get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product),
                        deliverableArtifactsMilestone.get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInNoMilestone(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<Artifact, BuildRecord> deliveredArtifactsBuild = deliveredArtifacts.join(Artifact_.buildRecord);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about artifacts from the milestone given by id
                // 2) only built delivered artifacts with no milestone
                // TODO: Inspect SQL, get >> join, no?
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.isNull(deliveredArtifactsBuild.get(BuildRecord_.productMilestone)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsNotBuilt(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about artifacts from the milestone given by id
                // 2) only delivered artifacts which were *not* built
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.isNull(deliveredArtifacts.get(Artifact_.buildRecord)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public EnumMap<ArtifactQuality, Long> getArtifactQualitiesCounts(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);

        query.where(cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId));
        query.multiselect(
                deliveredArtifacts.get(Artifact_.artifactQuality),
                cb.count(deliveredArtifacts.get(Artifact_.artifactQuality)));
        query.groupBy(deliveredArtifacts.get(Artifact_.artifactQuality));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        return transformListTupleToEnumMap(tuples, ArtifactQuality.class);
    }

    @Override
    public EnumMap<RepositoryType, Long> getRepositoryTypesCounts(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<Artifact, TargetRepository> targetRepositories = deliveredArtifacts.join(Artifact_.targetRepository);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.report)
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);

        query.where(cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId));
        query.multiselect(
                targetRepositories.get(TargetRepository_.repositoryType),
                cb.count(targetRepositories.get(TargetRepository_.repositoryType)));
        query.groupBy(targetRepositories.get(TargetRepository_.repositoryType));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        return transformListTupleToEnumMap(tuples, RepositoryType.class);
    }

    private <K extends Enum<K>> EnumMap<K, Long> transformListTupleToEnumMap(List<Tuple> tuples, Class<K> keyType) {
        EnumMap<K, Long> enumMap = Maps.initEnumMapWithDefaultValue(keyType, 0L);

        for (var t : tuples) {
            enumMap.put(t.get(0, keyType), t.get(1, Long.class));
        }

        return enumMap;
    }
}
