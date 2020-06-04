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

import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.bpm.causeway.ProductMilestoneReleaseManager;
import org.jboss.pnc.mock.repository.ArtifactRepositoryMock;
import org.jboss.pnc.mock.repository.BuildRecordRepositoryMock;
import org.jboss.pnc.mock.repository.ProductVersionRepositoryMock;
import org.jboss.pnc.mock.repository.SequenceHandlerRepositoryMock;
import org.jboss.pnc.rest.endpoint.BpmEndpoint;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ProductMilestoneReleaseRepositoryMock;
import org.jboss.pnc.rest.provider.MilestoneTestUtils.ProductMilestoneRepositoryMock;
import org.jboss.pnc.rest.utils.mock.BpmPushMock;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneReleaseRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/31/16 Time: 9:17 AM
 */
public class AbstractMilestoneReleaseTest {
    ArtifactRepository artifactRepository = new ArtifactRepositoryMock();
    ProductVersionRepository productVersionRepository = new ProductVersionRepositoryMock();
    BuildRecordRepository buildRecordRepository = new BuildRecordRepositoryMock();
    ProductMilestoneRepositoryMock productMilestoneRepository = new ProductMilestoneRepositoryMock();
    ProductMilestoneReleaseRepository releaseRepository = new ProductMilestoneReleaseRepositoryMock();

    BpmPushMock bpmMock;
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
    AuthenticationProviderFactory authenticationProviderFactory;
    @Mock
    private RepositoryConfigurationProvider repositoryConfigurationProvider;
    @Mock
    private RepositoryConfigurationRepository repositoryConfigurationRepository;
    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;
    @Mock
    private Notifier notifier;
    @Mock
    private BuildConfigurationSetProvider bcSetProvider;
    @Mock
    private ScmModuleConfig scmModuleConfig;
    @Mock
    Configuration pncConfiguration;

    @Mock
    private BuildRecordPushResultRepository buildRecordPushResultRepository;

    ProductMilestoneReleaseManager releaseManager;
    BpmEndpoint bpmEndpoint;

    @Before
    public void setUp() throws CoreException, ConfigurationParseException, IOException {
        bpmMock = new BpmPushMock();
        MockitoAnnotations.initMocks(this);
        releaseManager = new ProductMilestoneReleaseManager(
                releaseRepository,
                bpmMock,
                productVersionRepository,
                buildRecordRepository,
                productMilestoneRepository,
                buildRecordPushResultRepository);
        ProductMilestoneProvider milestoneProvider = new ProductMilestoneProvider(
                productMilestoneRepository,
                artifactRepository,
                releaseManager,
                rsqlPredicateProducer,
                sortInfoProducer,
                pageInfoProducer);

        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("git@github.com:22");
        when(pncConfiguration.getModuleConfig(new PncConfigProvider<>(ScmModuleConfig.class)))
                .thenReturn(scmModuleConfig);

        bpmEndpoint = new BpmEndpoint(
                bpmMock,
                bcSetProvider,
                authenticationProviderFactory,
                notifier,
                repositoryConfigurationRepository,
                repositoryConfigurationProvider,
                buildConfigurationRepository,
                new SequenceHandlerRepositoryMock(),
                pncConfiguration);
    }
}
