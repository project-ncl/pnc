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
package org.jboss.pnc.rest.endpoint;

import org.jboss.pnc.auth.AuthenticationProvider;
import org.jboss.pnc.auth.AuthenticationProviderFactory;
import org.jboss.pnc.auth.NoAuthLoggedInUser;
import org.jboss.pnc.bpm.BpmManager;
import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.provider.RepositoryConfigurationProvider;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationRest;
import org.jboss.pnc.rest.restmodel.mock.RepositoryCreationRestMockBuilder;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 12/7/16
 * Time: 4:03 PM
 */
public class BpmEndpointTest {

    private static final String INTERNAL_SCM_URL_WO_NAME = "git+ssh://git-repo-user@git-repo.devvm.devcloud.example.com:12839";
    private static final String VALID_INTERNAL_SCM_URL = INTERNAL_SCM_URL_WO_NAME + "/rock-a-teens/woo-hoo.git";
    private static final String VALID_EXTERNAL_SCM_URL = "git+ssh://github.com/project-ncl/pnc.git";
    private static final String EXISTING_INTERNAL_SCM_URL = INTERNAL_SCM_URL_WO_NAME + "/i-do/exist.git";
    @Mock
    private AuthenticationProviderFactory authProviderFactory;
    @Mock
    private AuthenticationProvider authProvider;
    @Mock
    private BuildConfigurationRepository configurationRepository;
    @Mock
    private BuildConfigurationAuditedRepository configurationAuditedRepository;
    @Mock
    private ProductVersionRepository versionRepository;
    @Mock
    private RepositoryConfigurationProvider repositoryConfigurationProvider;
    @Mock
    private Configuration configuration;
    @Mock
    private ScmModuleConfig scmModuleConfig;
    @Mock
    private BpmManager bpmManager;

    private BpmEndpoint bpmEndpoint;

    @Before
    public void setUp() throws ConfigurationParseException {
        MockitoAnnotations.initMocks(this);

        RepositoryConfiguration existingRepositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
                .internalScmRepoUrl(EXISTING_INTERNAL_SCM_URL)
                .externalScmRepoUrl("")
                .preBuildSyncEnabled(true)
                .build();

        when(authProviderFactory.getProvider()).thenReturn(authProvider);
        when(authProvider.getLoggedInUser(any())).thenReturn(new NoAuthLoggedInUser());
        when(configuration.getModuleConfig(any())).thenReturn(scmModuleConfig);
        when(repositoryConfigurationProvider.getSpecificByInternalScm(Matchers.eq(EXISTING_INTERNAL_SCM_URL)))
                .thenReturn(new RepositoryConfigurationRest(existingRepositoryConfiguration));

        BuildConfigurationProvider configurationProvider = new BuildConfigurationProvider(configurationRepository, configurationAuditedRepository,
                null, null, null, versionRepository, configuration);

        bpmEndpoint = new BpmEndpoint(bpmManager, null, authProviderFactory, configurationProvider, null, repositoryConfigurationProvider);

        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("git-repo-user@git-repo.devvm.devcloud.example.com:12839");
    }

    @Test
    public void shouldNotStartRCCreateTaskWithInternalURLWORepoName() throws Exception {
        RepositoryCreationRest configuration = configuration("shouldNotStartBCCreateTaskWithInternalURLWORepoName", "git+ssh://github.com/project-ncl/pnc.git");
        assertThrows(() -> bpmEndpoint.startRCreationTask(configuration, null), InvalidEntityException.class);
    }
    @Test
    public void shouldNotStartRCCreateTaskWithInvalidInternalURL() throws Exception {
        RepositoryCreationRest configuration = configuration("shouldNotStartBCCreateTaskWithInvalidInternalURL", INTERNAL_SCM_URL_WO_NAME);
        assertThrows(() -> bpmEndpoint.startRCreationTask(configuration, null), InvalidEntityException.class);
    }

    @Test
    public void shouldStartRCCreateTaskWithValidInternalURL() throws Exception {
        RepositoryCreationRest configuration = configuration("shouldStartBCCreateTaskWithValidInternalURL", VALID_INTERNAL_SCM_URL);
        Response response = bpmEndpoint.startRCreationTask(configuration, null);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldNotStartRCCreateTaskWithExistingInternalURL() throws Exception {
        RepositoryCreationRest configuration = configuration("shouldStartBCCreateTaskWithValidInternalURL", EXISTING_INTERNAL_SCM_URL);
        Response response = bpmEndpoint.startRCreationTask(configuration, null);
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_MODIFIED.getStatusCode());
    }

    private RepositoryCreationRest configuration(String name, String scmRepoUrl) {
        return RepositoryCreationRestMockBuilder.mock(name, "mvn clean deploy", scmRepoUrl);
    }

    private <E extends Exception> void assertThrows(ThrowingRunnable runnable, Class<E> exceptionClass) {
        try {
            runnable.run();
        } catch (Exception e) {
            if (exceptionClass.isInstance(e)) {
                return;
            }
            fail("Unexpected exception thrown. Expected: " + exceptionClass + ", actual: " + e);
        }
        fail("Expected exception not thrown. Expected: " + exceptionClass + ".");

    }

    interface ThrowingRunnable {
        void run() throws Exception;
    }

}