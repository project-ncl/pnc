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

import org.assertj.core.api.AbstractCharSequenceAssert;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductMilestoneRelease;
import org.jboss.pnc.rest.endpoint.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ArtifactRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.BuildRecordRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ProductMilestoneReleaseRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ProductMilestoneRepositoryMock;
import org.jboss.pnc.rest.restmodel.ProductMilestoneRest;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.rest.provider.MilestoneTestUtils.prepareMilestone;

/**
 * To use the test: place pnc-config.json (e.g. from pnc-orch-master-autodeploy) in src/test/resources and run the test.
 *
 *
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/25/16
 * Time: 3:21 PM
 */
@Category({ DebugTest.class })
public class MilestoneReleaseLiveTest {

    private ArtifactRepository artifactRepository = new ArtifactRepositoryMock();
    private BuildRecordRepository buildRecordRepository = new BuildRecordRepositoryMock();
    private ProductMilestoneRepositoryMock repoMock = new ProductMilestoneRepositoryMock();
    private ProductMilestoneReleaseRepository productMilestoneReleaseRepository = new ProductMilestoneReleaseRepositoryMock();

    private Configuration configuration = new Configuration();

    private BpmManager bpmManager = new BpmManager(configuration);
    @Mock
    private RSQLPredicateProducer rsqlPredicateProducer;
    @Mock
    private SortInfoProducer sortInfoProducer;
    @Mock
    private PageInfoProducer pageInfoProducer;
    @Mock
    private ArtifactProvider artifactProvider;
    @Mock
    private BuildRecordProvider buildRecordProvider;
    @Mock
    private ProductMilestoneReleaseProvider milestoneReleaseProvider;

    private ProductMilestoneEndpoint milestoneEndpoint;

    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        ProductMilestoneProvider milestoneProvider = new ProductMilestoneProvider(repoMock,
                bpmManager,
                artifactRepository,
                buildRecordRepository,
                productMilestoneReleaseRepository,
                rsqlPredicateProducer,
                sortInfoProducer,
                pageInfoProducer);
        milestoneEndpoint = new ProductMilestoneEndpoint(milestoneProvider, artifactProvider, buildRecordProvider, milestoneReleaseProvider);
        bpmManager.init();
    }

    @Test
    public void shouldProcessSuccessfulCallback() throws Exception {
        MilestoneTestUtils.createBuildRecord(buildRecordRepository);
        createAndReleaseMilestone();
    }

    private ProductMilestone createAndReleaseMilestone() throws Exception {
        ProductMilestone milestone = prepareMilestone(repoMock);

        triggerMilestoneRelease(milestone);

        assertLog(milestone).contains("Brew push task started");
        return milestone;
    }

    private AbstractCharSequenceAssert<?, String> assertLog(ProductMilestone milestone) {
        ProductMilestone productMilestone = repoMock.queryById(milestone.getId());
        ProductMilestoneRelease release = productMilestoneReleaseRepository.findLatestByMilestone(productMilestone);
        return assertThat(release.getLog());
    }

    private void triggerMilestoneRelease(ProductMilestone milestone) throws ValidationException {
        ProductMilestoneRest restEntity = new ProductMilestoneRest(milestone);
        restEntity.setEndDate(new Date());
        milestoneEndpoint.update(milestone.getId(), restEntity);
    }
}
