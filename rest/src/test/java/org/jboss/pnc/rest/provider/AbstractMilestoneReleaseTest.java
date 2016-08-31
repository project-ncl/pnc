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

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.managers.ProductMilestoneReleaseManager;
import org.jboss.pnc.rest.endpoint.BpmEndpoint;
import org.jboss.pnc.rest.endpoint.ProductMilestoneEndpoint;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ArtifactRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.BuildRecordRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ProductMilestoneReleaseRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ProductMilestoneRepositoryMock;
import org.jboss.pnc.rest.utils.mock.BpmMock;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.exception.CoreException;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/31/16
 * Time: 9:17 AM
 */
public class AbstractMilestoneReleaseTest {
    ArtifactRepository artifactRepository = new ArtifactRepositoryMock();
    BuildRecordRepository buildRecordRepository = new BuildRecordRepositoryMock();
    ProductMilestoneRepositoryMock productMilestoneRepository = new ProductMilestoneRepositoryMock();
    ProductMilestoneReleaseRepository releaseRepository = new ProductMilestoneReleaseRepositoryMock();

    BpmMock bpmMock;
    @Mock
    RSQLPredicateProducer rsqlPredicateProducer;
    @Mock
    SortInfoProducer sortInfoProducer;
    @Mock
    PageInfoProducer pageInfoProducer;
    @Mock
    ArtifactProvider artifactProvider;
    @Mock
    BuildRecordProvider buildRecordProvider;
    @Mock
    ProductMilestoneReleaseProvider milestoneReleaseProvider;

    ProductMilestoneEndpoint milestoneEndpoint;
    ProductMilestoneReleaseManager releaseManager;
    BpmEndpoint bpmEndpoint;

    @Before
    public void setUp() throws CoreException, ConfigurationParseException {
        bpmMock = new BpmMock();
        MockitoAnnotations.initMocks(this);
        releaseManager = new ProductMilestoneReleaseManager(releaseRepository, bpmMock, artifactRepository, buildRecordRepository, productMilestoneRepository);
        ProductMilestoneProvider milestoneProvider = new ProductMilestoneProvider(productMilestoneRepository,
                artifactRepository,
                releaseManager,
                rsqlPredicateProducer,
                sortInfoProducer,
                pageInfoProducer);
        milestoneEndpoint = new ProductMilestoneEndpoint(milestoneProvider, artifactProvider, buildRecordProvider, milestoneReleaseProvider);
        bpmEndpoint = new BpmEndpoint(bpmMock);
        bpmMock.setUp();
    }
}
