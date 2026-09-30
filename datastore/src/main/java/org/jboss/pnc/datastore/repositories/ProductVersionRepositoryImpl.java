/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.Artifact_;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

@Stateless
public class ProductVersionRepositoryImpl extends AbstractRepository<ProductVersion, Integer>
        implements ProductVersionRepository {

    @Inject
    public ProductVersionRepositoryImpl() {
        super(ProductVersion.class, Integer.class);
    }

    @Override
    public long countMilestonesInThisVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);

        query.select(cb.count(milestones));
        query.where(cb.equal(milestones.get(ProductMilestone_.productVersion).get(ProductVersion_.id), id));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countBuiltArtifactsInThisVersion(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<Artifact> artifacts = query.from(Artifact.class);
        Join<ProductMilestone, ProductVersion> builtArtifactsProductVersion = artifacts.join(Artifact_.buildRecord)
                .join(BuildRecord_.productMilestone)
                .join(ProductMilestone_.productVersion);

        query.select(cb.count(artifacts));
        query.where(cb.equal(builtArtifactsProductVersion.get(ProductVersion_.id), id));

        return entityManager.createQuery(query).getSingleResult();
    }
}
