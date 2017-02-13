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
import org.jboss.pnc.rest.provider.BuildConfigurationProvider;
import org.jboss.pnc.rest.restmodel.bpm.BpmBuildConfigurationCreationRest;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;
import org.junit.Before;
import org.junit.Test;
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
    private Configuration configuration;
    @Mock
    private ScmModuleConfig scmModuleConfig;
    @Mock
    private BpmManager bpmManager;

    private BpmEndpoint bpmEndpoint;

    @Before
    public void setUp() throws ConfigurationParseException {
        MockitoAnnotations.initMocks(this);

        when(authProviderFactory.getProvider()).thenReturn(authProvider);
        when(authProvider.getLoggedInUser(any())).thenReturn(new NoAuthLoggedInUser());
        when(configuration.getModuleConfig(any())).thenReturn(scmModuleConfig);

        BuildConfigurationProvider configurationProvider = new BuildConfigurationProvider(configurationRepository, configurationAuditedRepository,
                null, null, null, versionRepository, configuration);

        bpmEndpoint = new BpmEndpoint(bpmManager, null, authProviderFactory, configurationProvider, null);

        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("git-repo-user@git-repo.devvm.devcloud.example.com:12839");
    }

    @Test
    public void shouldNotStartBCCreateTaskWithInternalURLWORepoName() throws Exception {
        BpmBuildConfigurationCreationRest configuration = configuration("shouldNotStartBCCreateTaskWithInternalURLWORepoName");
        configuration.setScmRepoURL("git+ssh://github.com/project-ncl/pnc.git");
        assertThrows(() -> bpmEndpoint.startBCCreationTask(configuration, null), InvalidEntityException.class);
    }
    @Test
    public void shouldNotStartBCCreateTaskWithInvalidInternalURL() throws Exception {
        BpmBuildConfigurationCreationRest configuration = configuration("shouldNotStartBCCreateTaskWithInvalidInternalURL");
        configuration.setScmRepoURL(INTERNAL_SCM_URL_WO_NAME);
        assertThrows(() -> bpmEndpoint.startBCCreationTask(configuration, null), InvalidEntityException.class);
    }

    @Test
    public void shouldStartBCCreateTaskWithValidInternalURL() throws Exception {
        BpmBuildConfigurationCreationRest configuration = configuration("shouldStartBCCreateTaskWithValidInternalURL");
        configuration.setScmRepoURL(VALID_INTERNAL_SCM_URL);
        Response response = bpmEndpoint.startBCCreationTask(configuration, null);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldStartBCCreateTaskWithValidExternalURL() throws Exception {
        BpmBuildConfigurationCreationRest configuration = configuration("shouldStartBCCreateTaskWithValidExternalURL");
        configuration.setScmExternalRepoURL("git+ssh://github.com/project-ncl/pnc.git");

        Response response = bpmEndpoint.startBCCreationTask(configuration, null);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void shouldNotStartBCCreateTaskWithoutURL() throws Exception {
        BpmBuildConfigurationCreationRest configuration = configuration("shouldNotStartBCCreateTaskWithoutURL");
        assertThrows(() -> bpmEndpoint.startBCCreationTask(configuration, null), InvalidEntityException.class);
    }

    @Test
    public void shouldNotStartBCCreateTaskWithBothURLs() throws Exception {
        BpmBuildConfigurationCreationRest configuration = configuration("shouldNotStartBCCreateTaskWithBothURLs");
        configuration.setScmExternalRepoURL(VALID_EXTERNAL_SCM_URL);
        configuration.setScmRepoURL(VALID_INTERNAL_SCM_URL);
        assertThrows(() -> bpmEndpoint.startBCCreationTask(configuration, null), InvalidEntityException.class);
    }

    private BpmBuildConfigurationCreationRest configuration(String name) {
        BpmBuildConfigurationCreationRest configuration = new BpmBuildConfigurationCreationRest();
        configuration.setName(name);

        configuration.setBuildEnvironmentId(1);
        configuration.setProjectId(1);
        configuration.setBuildScript("mvn clean deploy");
        return configuration;
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