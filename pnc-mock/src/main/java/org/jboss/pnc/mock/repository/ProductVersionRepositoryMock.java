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
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.persistence.Tuple;
import java.util.List;
import java.util.Set;

@Alternative
@ApplicationScoped
public class ProductVersionRepositoryMock extends IntIdRepositoryMock<ProductVersion>
        implements ProductVersionRepository {

    @Override
    public long countMilestonesInVersion(Integer id) {
        return 0;
    }

    @Override
    public long countProductDependenciesInVersion(Integer id) {
        return 0;
    }

    @Override
    public long countMilestoneDependenciesInVersion(Integer id) {
        return 0;
    }

    @Override
    public long countBuiltArtifactsInVersion(Integer id) {
        return 0;
    }

    @Override
    public long countDeliveredArtifactsBuiltInThisVersion(Integer id) {
        return 0;
    }

    @Override
    public long countDeliveredArtifactsBuiltInOtherVersions(Integer id) {
        return 0;
    }

    @Override
    public long countDeliveredArtifactsBuiltByOtherProducts(Integer id) {
        return 0;
    }

    @Override
    public long countDeliveredArtifactsBuiltInNoMilestone(Integer id) {
        return 0;
    }

    @Override
    public long countDeliveredArtifactsNotBuilt(Integer id) {
        return 0;
    }

    @Override
    public List<Tuple> getArtifactQualityStatistics(Set<Integer> ids) {
        return null;
    }

    @Override
    public List<Tuple> getRepositoryTypesStatistics(Set<Integer> ids) {
        return null;
    }
}
