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
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.model.TargetRepository_;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;

import javax.ejb.Stateless;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import java.util.EnumMap;
import java.util.List;

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
        Join<Artifact, BuildRecord> builtArtifacts = artifacts.join(Artifact_.buildRecord);
        Join<BuildRecord, org.jboss.pnc.model.ProductMilestone> builtArtifactsMilestones = builtArtifacts
                .join(BuildRecord_.productMilestone);

        query.where(cb.equal(builtArtifactsMilestones.get(ProductMilestone_.id), id));
        query.select(cb.count(artifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInThisMilestone(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);
        SetJoin<ProductMilestone, Artifact> deliveredArtifacts = milestones.join(ProductMilestone_.deliveredArtifacts);
        Path<ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts.join(Artifact_.buildRecord)
                .get(BuildRecord_.productMilestone);

        // 1) we care about artifacts from the milestone given by id
        // 2) only built delivered artifacts built in this milestone => implies built by this product
        query.where(
                cb.equal(milestones.get(ProductMilestone_.id), id),
                cb.equal(builtDeliveredArtifactsMilestone.get(ProductMilestone_.id), id));
        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInOtherMilestones(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);
        SetJoin<ProductMilestone, Artifact> deliveredArtifacts = milestones.join(ProductMilestone_.deliveredArtifacts);
        Path<ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts.get(Artifact_.buildRecord)
                .get(BuildRecord_.productMilestone);

        // 1) we care about artifacts from the milestone given by id
        // 2) only built delivered artifacts from this product
        // 3) only built delivered artifacts *not* built in this milestone
        query.where(
                cb.equal(milestones.get(ProductMilestone_.id), id),
                cb.equal(
                        builtDeliveredArtifactsMilestone.get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product)
                                .get(Product_.id),
                        milestones.get(ProductMilestone_.productVersion).get(ProductVersion_.product).get(Product_.id)),
                cb.notEqual(builtDeliveredArtifactsMilestone.get(ProductMilestone_.id), id));
        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltByOtherProducts(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);
        SetJoin<ProductMilestone, Artifact> deliveredArtifacts = milestones.join(ProductMilestone_.deliveredArtifacts);
        Path<ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts.get(Artifact_.buildRecord)
                .get(BuildRecord_.productMilestone);

        // 1) we care about artifacts from the milestone given by id
        // 2) only built delivered artifacts *not* from this product
        query.where(
                cb.equal(milestones.get(ProductMilestone_.id), id),
                cb.notEqual(
                        builtDeliveredArtifactsMilestone.get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product)
                                .get(Product_.id),
                        milestones.get(ProductMilestone_.productVersion).get(ProductVersion_.product).get(Product_.id)));
        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInNoMilestone(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);
        SetJoin<ProductMilestone, Artifact> deliveredArtifacts = milestones.join(ProductMilestone_.deliveredArtifacts);
        Join<Artifact, BuildRecord> deliveredArtifactsBuild = deliveredArtifacts.join(Artifact_.buildRecord);

        // 1) we care about artifacts from the milestone given by id
        // 2) only built delivered artifacts with no milestone
        query.where(
                cb.equal(milestones.get(ProductMilestone_.id), id),
                cb.isNull(deliveredArtifactsBuild.get(BuildRecord_.productMilestone)));
        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsNotBuilt(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);
        SetJoin<ProductMilestone, Artifact> deliveredArtifacts = milestones.join(ProductMilestone_.deliveredArtifacts);

        // 1) we care about artifacts from the milestone given by id
        // 2) only delivered artifacts which were *not* built
        query.where(
                cb.equal(milestones.get(ProductMilestone_.id), id),
                cb.isNull(deliveredArtifacts.get(Artifact_.buildRecord)));
        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public EnumMap<ArtifactQuality, Long> getArtifactQualitiesCounts(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> productMilestones = query
                .from(org.jboss.pnc.model.ProductMilestone.class);
        SetJoin<org.jboss.pnc.model.ProductMilestone, Artifact> deliveredArtifacts = productMilestones
                .join(ProductMilestone_.deliveredArtifacts);

        query.where(cb.equal(productMilestones.get(ProductMilestone_.id), id));
        query.multiselect(
                deliveredArtifacts.get(Artifact_.artifactQuality),
                cb.count(deliveredArtifacts.get(Artifact_.artifactQuality)));
        query.groupBy(deliveredArtifacts.get(Artifact_.artifactQuality));

        List<Tuple> tuples = entityManager.createQuery(query).getResultList();
        return transformListTupleToEnumMap(tuples, ArtifactQuality.class);
    }

    @Override
    public EnumMap<RepositoryType, Long> getRepositoryTypesCounts(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<org.jboss.pnc.model.ProductMilestone> productMilestones = query
                .from(org.jboss.pnc.model.ProductMilestone.class);
        SetJoin<org.jboss.pnc.model.ProductMilestone, Artifact> deliveredArtifacts = productMilestones
                .join(ProductMilestone_.deliveredArtifacts);
        Join<Artifact, TargetRepository> targetRepositories = deliveredArtifacts.join(Artifact_.targetRepository);

        query.where(cb.equal(productMilestones.get(ProductMilestone_.id), id));
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
