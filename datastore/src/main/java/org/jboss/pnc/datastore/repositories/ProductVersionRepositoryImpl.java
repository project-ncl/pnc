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
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestone_;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.ProductVersion_;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Stateless
public class ProductVersionRepositoryImpl extends AbstractRepository<ProductVersion, Integer>
        implements ProductVersionRepository {

    @Inject
    public ProductVersionRepositoryImpl() {
        super(ProductVersion.class, Integer.class);
    }

    @Override
    public long countMilestones(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductMilestone> milestones = query.from(ProductMilestone.class);

        query.select(cb.count(milestones));
        query.where(cb.equal(milestones.get(ProductMilestone_.productVersion).get(ProductVersion_.id), id));

        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public long countBuiltArtifacts(Integer id) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);

        Root<ProductVersion> versions = query.from(ProductVersion.class);

        query.select(
                cb.count(
                        versions.join(ProductVersion_.productMilestones)
                                .join(ProductMilestone_.performedBuilds)
                                .join(BuildRecord_.builtArtifacts)));
        query.where(cb.equal(versions.get(ProductVersion_.id), id));

        return entityManager.createQuery(query).getSingleResult();
    }
}
