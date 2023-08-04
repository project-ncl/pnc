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

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.Product;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.model.Product_;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

@Stateless
public class ProductVersionRepositoryImpl extends AbstractRepository<ProductVersion, Integer>
        implements ProductVersionRepository {

    @Inject
    public ProductVersionRepositoryImpl() {
        super(ProductVersion.class, Integer.class);
    }

    @Override
    public long countMilestonesInVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<ProductVersion, ProductMilestone> versionsMilestones = versions.join(ProductVersion_.productMilestones);

        query.where(cb.equal(versions.get(ProductVersion_.id), id));
        query.select(cb.count(versionsMilestones.get(ProductMilestone_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countProductDependenciesInVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        Path<BuildRecord> versionDeliveredArtifactsBuild = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts)
                .join(Artifact_.buildRecord);

        query.where(cb.equal(versions.get(ProductVersion_.id), id));
        query.select(
                cb.countDistinct(
                        versionDeliveredArtifactsBuild.get(BuildRecord_.productMilestone)
                                .get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countMilestoneDependenciesInVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        Path<BuildRecord> versionDeliveredArtifactsBuild = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts)
                .join(Artifact_.buildRecord);

        query.where(cb.equal(versions.get(ProductVersion_.id), id));
        query.select(cb.countDistinct(versionDeliveredArtifactsBuild.get(BuildRecord_.productMilestone)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countBuiltArtifactsInVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<BuildRecord, Artifact> versionBuiltArtifacts = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.performedBuilds)
                .join(BuildRecord_.builtArtifacts);

        query.where(cb.equal(versions.get(ProductVersion_.id), id));
        query.select(cb.count(versionBuiltArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInThisVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<ProductMilestone, Artifact> versionDeliveredArtifacts = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts);
        Path<BuildRecord> deliveredArtifactsBuild = versionDeliveredArtifacts.join(Artifact_.buildRecord);

        // 1) only artifacts from this version
        // 2) delivered built artifacts, which were built in this version
        // Note: Same version id implies same product
        query.where(
                cb.equal(versions.get(ProductVersion_.id), id),
                cb.equal(
                        deliveredArtifactsBuild.get(BuildRecord_.productMilestone)
                                .get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.id),
                        id));
        query.select(cb.count(versionDeliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInOtherVersions(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<ProductMilestone, Artifact> versionDeliveredArtifacts = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts);
        Path<BuildRecord> deliveredArtifactsBuild = versionDeliveredArtifacts.join(Artifact_.buildRecord);

        // 1) only artifacts from this version
        // 2) delivered built artifacts, which are of the same product
        // 3) delivered built artifacts, which are from other version
        query.where(
                cb.equal(versions.get(ProductVersion_.id), id),
                cb.equal(
                        deliveredArtifactsBuild.get(BuildRecord_.productMilestone)
                                .get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product),
                        versions.get(ProductVersion_.product)),
                cb.notEqual(
                        deliveredArtifactsBuild.get(BuildRecord_.productMilestone)
                                .get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.id),
                        id));
        query.select(cb.count(versionDeliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltByOtherProducts(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<ProductMilestone, Artifact> versionDeliveredArtifacts = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts);
        Path<BuildRecord> deliveredArtifactBuild = versionDeliveredArtifacts.join(Artifact_.buildRecord);

        // 1) only artifacts from this version
        // 2) delivered built artifacts, which were built by other products
        query.where(
                cb.equal(versions.get(ProductVersion_.id), id),
                cb.notEqual(
                        deliveredArtifactBuild.get(BuildRecord_.productMilestone)
                                .get(ProductMilestone_.productVersion)
                                .get(ProductVersion_.product),
                        versions.get(ProductVersion_.product)));
        query.select(cb.count(versionDeliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsBuiltInNoMilestone(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<ProductMilestone, Artifact> versionDeliveredArtifacts = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts);
        Path<BuildRecord> deliveredArtifactBuild = versionDeliveredArtifacts.join(Artifact_.buildRecord);

        // 1) only artifacts from this version
        // 2) delivered built artifacts, which were built in no milestone
        query.where(
                cb.equal(versions.get(ProductVersion_.id), id),
                cb.isNull(deliveredArtifactBuild.get(BuildRecord_.productMilestone)));
        query.select(cb.count(versionDeliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countDeliveredArtifactsNotBuilt(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);
        SetJoin<ProductMilestone, Artifact> versionDeliveredArtifacts = versions.join(ProductVersion_.productMilestones)
                .join(ProductMilestone_.deliveredArtifacts);

        // 1) only artifacts from this version
        // 2) delivered artifacts, which were not built
        query.where(
                cb.equal(versions.get(ProductVersion_.id), id),
                cb.isNull(versionDeliveredArtifacts.get(Artifact_.buildRecord)));
        query.select(cb.count(versionDeliveredArtifacts.get(Artifact_.id)));

        return entityManager.createQuery(query).getSingleResult();
    }
}
