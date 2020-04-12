/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.mock.repository.IntIdRepositoryMock;
import org.jboss.pnc.mock.repository.UUIDRepositoryMock;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.mockito.Mockito;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang.RandomStringUtils.randomNumeric;
import static org.jboss.pnc.common.util.RandomUtils.randInt;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/29/16 Time: 6:34 PM
 */
public class MilestoneTestUtils {

    public static BuildRecord createBuildRecord(BuildRecordRepository repositoryMock) {
        BuildRecord record = new BuildRecord();
        record.setId(randInt(100000, 2000000));
        record.setScmRepoURL(randomAlphabetic(20));
        record.setScmRevision(randomAlphabetic(40));
        record.setScmTag(randomAlphabetic(5));

        BuildConfigurationAudited buildConfigurationAudited = Mockito.mock(BuildConfigurationAudited.class);
        Mockito.when(buildConfigurationAudited.getName()).thenReturn(randomAlphabetic(20));

        record.setBuildConfigurationAudited(buildConfigurationAudited);
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

    public static class ProductMilestoneRepositoryMock extends IntIdRepositoryMock<ProductMilestone>
            implements ProductMilestoneRepository {
    }

    public static class ProductMilestoneReleaseRepositoryMock extends UUIDRepositoryMock<ProductMilestoneRelease>
            implements ProductMilestoneReleaseRepository {
        @Override
        public ProductMilestoneRelease findLatestByMilestone(ProductMilestone milestone) {
            List<ProductMilestoneRelease> list = data.stream()
                    .filter(r -> Objects.equals(r.getMilestone().getId(), milestone.getId()))
                    .collect(Collectors.toList());
            int listSize = list.size();
            return listSize == 0 ? null : list.get(listSize - 1);
        }
    }
}
