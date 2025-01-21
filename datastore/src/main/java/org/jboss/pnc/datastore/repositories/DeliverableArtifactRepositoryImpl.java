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
import org.jboss.pnc.model.DeliverableAnalyzerReport;
import org.jboss.pnc.model.DeliverableAnalyzerReport_;
import org.jboss.pnc.model.DeliverableArtifact;
import org.jboss.pnc.model.DeliverableArtifactPK;
import org.jboss.pnc.model.DeliverableArtifact_;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
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
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerReportPredicates.notFromScratchAnalysis;
import static org.jboss.pnc.spi.datastore.predicates.DeliverableAnalyzerReportPredicates.notFromDeletedAnalysis;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.notProducedInBuild;

@Stateless
public class DeliverableArtifactRepositoryImpl extends AbstractRepository<DeliverableArtifact, DeliverableArtifactPK>
        implements DeliverableArtifactRepository {

    public DeliverableArtifactRepositoryImpl() {
        super(DeliverableArtifact.class, DeliverableArtifactPK.class);
    }

    @Override
    public long countMilestoneDeliveredArtifactsBuiltInThisMilestone(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
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
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.equal(builtDeliveredArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countMilestoneDeliveredArtifactsBuiltInOtherMilestones(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<ProductVersion, Product> deliverableArtifactsProduct = deliverableArtifactsMilestone
                .join(ProductMilestone_.productVersion)
                .join(ProductVersion_.product);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<BuildRecord, ProductMilestone> builtDeliveredArtifactsMilestone = deliveredArtifacts
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone);
        Join<ProductVersion, Product> builtDeliveredArtifactsProduct = builtDeliveredArtifactsMilestone
                .join(ProductMilestone_.productVersion)
                .join(ProductVersion_.product);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about delivered artifacts from the milestone given by id
                // 2) only built delivered artifacts from this product
                // 3) only built delivered artifacts *not* built in this milestone
                // 4) delivered artifacts *not* from scratch analysis
                // 5) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.equal(builtDeliveredArtifactsProduct.get(Product_.id), deliverableArtifactsProduct.get(Product_.id)),
                cb.notEqual(builtDeliveredArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countMilestoneDeliveredArtifactsBuiltByOtherProducts(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableArtifactsMilestone
                .join(ProductMilestone_.productVersion);
        Path<ProductVersion> builtDeliveredArtifactsVersion = deliveredArtifacts.join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone)
                .join(ProductMilestone_.productVersion);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about delivered artifacts from the milestone given by id
                // 2) only built delivered artifacts *not* from this product
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.notEqual(
                        builtDeliveredArtifactsVersion.get(ProductVersion_.product),
                        deliverableArtifactsProductVersion.get(ProductVersion_.product)),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countMilestoneDeliveredArtifactsBuiltInNoMilestone(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Join<Artifact, BuildRecord> deliveredArtifactsBuild = deliveredArtifacts.join(Artifact_.buildRecord);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about delivered artifacts from the milestone given by id
                // 2) only built delivered artifacts with no milestone
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                cb.isNull(deliveredArtifactsBuild.get(BuildRecord_.productMilestone)),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countMilestoneDeliveredArtifactsNotBuilt(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) we care about artifacts from the milestone given by id
                // 2) only delivered artifacts which were *not* built
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                notProducedInBuild(cb, deliveredArtifacts),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public EnumMap<ArtifactQuality, Long> getArtifactQualitiesCounts(Integer productMilestoneId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);

        query.multiselect(
                deliveredArtifacts.get(Artifact_.artifactQuality),
                cb.count(deliveredArtifacts.get(Artifact_.artifactQuality)));
        query.where(
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));
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
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);

        query.multiselect(
                targetRepositories.get(TargetRepository_.repositoryType),
                cb.count(targetRepositories.get(TargetRepository_.repositoryType)));
        query.where(
                cb.equal(deliverableArtifactsMilestone.get(ProductMilestone_.id), productMilestoneId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));
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

    @Override
    public long countVersionDeliveredArtifactsProductDependencies(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);
        Join<ProductVersion, Product> builtDeliveredArtifactProduct = deliverableArtifacts
                .join(DeliverableArtifact_.artifact)
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone)
                .join(ProductMilestone_.productVersion)
                .join(ProductVersion_.product);

        query.select(
                // Take distinct products, since one product can build more than one delivered artifact
                cb.countDistinct(builtDeliveredArtifactProduct.get(Product_.id)));
        query.where(
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countVersionDeliveredArtifactsMilestoneDependencies(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<BuildRecord, ProductMilestone> builtDeliveredArtifactMilestone = deliverableArtifacts
                .join(DeliverableArtifact_.artifact)
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);

        query.select(
                // Take distinct milestones, since one milestone can build more than one delivered artifact
                cb.countDistinct(builtDeliveredArtifactMilestone.get(ProductMilestone_.id)));
        query.where(
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countVersionDeliveredArtifactsBuiltInThisVersion(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Path<Integer> builtDeliveredArtifactProductVersionId = deliveredArtifacts.join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone)
                .join(ProductMilestone_.productVersion)
                .get(ProductVersion_.id);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) only delivered artifacts from this version
                // 2) delivered built artifacts, which were built in this version
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                cb.equal(builtDeliveredArtifactProductVersionId, productVersionId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countVersionDeliveredArtifactsBuiltInOtherVersions(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<ProductMilestone, ProductVersion> deliveredArtifactsBuildProductVersion = deliveredArtifacts
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone)
                .join(ProductMilestone_.productVersion);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) only delivered artifacts from this version
                // 2) delivered built artifacts, which are of the same product
                // 3) delivered built artifacts, which are from other version
                // 4) delivered artifacts *not* from scratch analysis
                // 5) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                cb.equal(
                        deliveredArtifactsBuildProductVersion.get(ProductVersion_.product),
                        deliverableArtifactsProductVersion.get(ProductVersion_.product)),
                cb.notEqual(deliveredArtifactsBuildProductVersion.get(ProductVersion_.id), productVersionId),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countVersionDeliveredArtifactsBuiltByOtherProducts(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<ProductMilestone, ProductVersion> deliveredArtifactsBuildProductVersion = deliveredArtifacts
                .join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone)
                .join(ProductMilestone_.productVersion);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) only artifacts from this version
                // 2) delivered built artifacts, which were built by other products
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                cb.notEqual(
                        deliveredArtifactsBuildProductVersion.get(ProductVersion_.product),
                        deliverableArtifactsProductVersion.get(ProductVersion_.product)),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countVersionDeliveredArtifactsBuiltInNoMilestone(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);
        Join<Artifact, BuildRecord> deliveredArtifactsBuild = deliveredArtifacts.join(Artifact_.buildRecord);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) only artifacts from this version
                // 2) delivered built artifacts, which were built in no milestone
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                cb.isNull(deliveredArtifactsBuild.get(BuildRecord_.productMilestone)),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countVersionDeliveredArtifactsNotBuilt(Integer productVersionId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<ProductMilestone, ProductVersion> deliverableArtifactsProductVersion = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone)
                .join(ProductMilestone_.productVersion);
        Join<DeliverableArtifact, Artifact> deliveredArtifacts = deliverableArtifacts
                .join(DeliverableArtifact_.artifact);

        query.select(cb.count(deliveredArtifacts.get(Artifact_.id)));
        query.where(
                // 1) only artifacts from this version
                // 2) delivered artifacts, which were not built
                // 3) delivered artifacts *not* from scratch analysis
                // 4) delivered artifacts *not* from deleted analysis
                cb.equal(deliverableArtifactsProductVersion.get(ProductVersion_.id), productVersionId),
                notProducedInBuild(cb, deliveredArtifacts),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<Tuple> getArtifactQualityStatistics(Set<Integer> milestoneIds) {
        // In case ids = {}, we want to return [], since GROUP BY () is SQL error
        if (milestoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);

        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Path<ArtifactQuality> artifactQuality = deliverableArtifacts.join(DeliverableArtifact_.artifact)
                .get(Artifact_.artifactQuality);

        query.multiselect(
                deliverableArtifactsMilestone.get(ProductMilestone_.id),
                artifactQuality,
                cb.count(artifactQuality));
        query.where(
                deliverableArtifactsMilestone.get(ProductMilestone_.id).in(milestoneIds),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));
        query.groupBy(deliverableArtifactsMilestone.get(ProductMilestone_.id), artifactQuality);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<Tuple> getRepositoryTypesStatistics(Set<Integer> milestoneIds) {
        // In case ids = {}, we want to return [], since GROUP BY () is SQL error
        if (milestoneIds.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<DeliverableArtifact> deliverableArtifacts = query.from(DeliverableArtifact.class);
        Join<DeliverableArtifact, DeliverableAnalyzerReport> deliverableAnalyzerReports = deliverableArtifacts
                .join(DeliverableArtifact_.report);
        Join<DeliverableAnalyzerOperation, ProductMilestone> deliverableArtifactsMilestone = deliverableAnalyzerReports
                .join(DeliverableAnalyzerReport_.operation)
                .join(DeliverableAnalyzerOperation_.productMilestone);
        Path<RepositoryType> repositoryType = deliverableArtifacts.join(DeliverableArtifact_.artifact)
                .join(Artifact_.targetRepository)
                .get(TargetRepository_.repositoryType);

        query.multiselect(
                deliverableArtifactsMilestone.get(ProductMilestone_.id),
                repositoryType,
                cb.count(repositoryType));
        query.where(
                deliverableArtifactsMilestone.get(ProductMilestone_.id).in(milestoneIds),
                notFromScratchAnalysis(cb, deliverableAnalyzerReports),
                notFromDeletedAnalysis(cb, deliverableAnalyzerReports));
        query.groupBy(deliverableArtifactsMilestone.get(ProductMilestone_.id), repositoryType);

        return entityManager.createQuery(query).getResultList();
    }
}
