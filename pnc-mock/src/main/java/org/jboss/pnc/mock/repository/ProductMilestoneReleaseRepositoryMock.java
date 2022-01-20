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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;

import java.util.Comparator;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 12/1/16 Time: 3:27 PM
 */
public class ProductMilestoneReleaseRepositoryMock extends LongIdRepositoryMock<ProductMilestoneRelease>
        implements ProductMilestoneReleaseRepository {
    @Override
    public ProductMilestoneRelease findLatestByMilestone(ProductMilestone milestone) {
        return data.stream()
                .filter(milestoneRelease -> milestoneRelease.getMilestone().equals(milestone))
                .sorted(Comparator.comparing(ProductMilestoneRelease::getId).reversed())
                .findFirst()
                .orElse(null);
    }
}
