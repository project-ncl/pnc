/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.utils.mock.RepositoryMock;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.jboss.pnc.common.util.RandomUtils.randInt;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/29/16
 * Time: 6:34 PM
 */
public class MilestoneTestUtils {

    public static BuildRecord createBuildRecord(BuildRecordRepository repositoryMock) {
        BuildRecord record = new BuildRecord();
        record.setId(randInt(100000, 2000000));
        record.setScmRepoURL(randomAlphabetic(20));
        record.setScmRevision(randomAlphabetic(5));
        BuildConfigurationAudited config = new BuildConfigurationAudited();
        config.setName(randomAlphabetic(20));
        record.setBuildConfigurationAudited(config);
        repositoryMock.save(record);

        return record;
    }

    public static ProductMilestone prepareMilestone(ProductMilestoneRepository milestoneRepository) {
        ProductMilestone milestone = new ProductMilestone();
        milestone.setId(randInt(1000, 100000));
        ProductVersion productVersion = new ProductVersion();
        productVersion.setId(randInt(1000, 100000));
        milestone.setProductVersion(productVersion);
        milestone.setVersion(randomNumeric(10));
        milestoneRepository.save(milestone);

        return milestone;
    }

    public static class ProductMilestoneRepositoryMock extends RepositoryMock<ProductMilestone, Integer> implements ProductMilestoneRepository {
    }

    public static class ArtifactRepositoryMock extends RepositoryMock<Artifact, Integer> implements ArtifactRepository {
    }

    public static class BuildRecordRepositoryMock extends RepositoryMock<BuildRecord, Integer> implements BuildRecordRepository {
        @Override
        public BuildRecord findByIdFetchAllProperties(Integer id) {
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildRecord>... predicates) {
            return null;
        }
    }
}
