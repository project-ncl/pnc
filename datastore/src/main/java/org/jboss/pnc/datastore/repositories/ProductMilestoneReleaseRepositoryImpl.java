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
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductMilestoneRelease_;
import org.jboss.pnc.spi.datastore.predicates.ProductMilestoneReleasePredicates;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;

import javax.ejb.Stateless;
import java.util.List;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/30/16 Time: 1:50 PM
 */
@Stateless
public class ProductMilestoneReleaseRepositoryImpl extends AbstractRepository<ProductMilestoneRelease, Long>
        implements ProductMilestoneReleaseRepository {

    public ProductMilestoneReleaseRepositoryImpl() {
        super(ProductMilestoneRelease.class, Long.class);
    }

    @Override
    public ProductMilestoneRelease findLatestByMilestone(ProductMilestone milestone) {
        List<ProductMilestoneRelease> productMilestoneReleases = queryWithPredicates(
                new DefaultPageInfo(0, 1),
                new DefaultSortInfo(SortInfo.SortingDirection.DESC, ProductMilestoneRelease_.startingDate.getName()),
                ProductMilestoneReleasePredicates.withMilestoneId(milestone.getId()));
        return productMilestoneReleases.size() > 0 ? productMilestoneReleases.get(0) : null;
    }
}
