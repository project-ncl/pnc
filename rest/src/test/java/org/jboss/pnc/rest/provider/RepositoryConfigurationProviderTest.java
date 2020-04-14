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

import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * @author Jakub Bartecek
 */
public class RepositoryConfigurationProviderTest {
    private static final int EXISTING_ID = 1243;
    private static final String URL_WITHOUT_SUFFIX = "git+ssh://git@github.com/project-ncl/pnc";
    private static final String VALID_URL = URL_WITHOUT_SUFFIX + ".git";
    private static final String INVALID_URL = "invalid url";

    @Mock
    private RepositoryConfigurationRepository repository;

    @Mock
    private ScmModuleConfig scmModuleConfig;

    @InjectMocks
    private RepositoryConfigurationProvider repositoryConfigurationProvider = new RepositoryConfigurationProvider();

    @Before
    public void setUp() throws ConfigurationParseException {
        MockitoAnnotations.initMocks(this);
        when(repository.queryById(EXISTING_ID)).thenReturn(RepositoryConfiguration.Builder.newBuilder().build());
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("git@github.com");
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldFailOnInvalidGitUrl() throws RestValidationException {
        RepositoryConfigurationRest configuration = createValidConfiguration();
        configuration.setInternalUrl("git+ssh://git@github.com/");
        repositoryConfigurationProvider.validateBeforeSaving(configuration);
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldFailOnValidGitUrlWithoutDotGit() throws RestValidationException {
        RepositoryConfigurationRest configuration = createValidConfiguration();
        configuration.setInternalUrl(URL_WITHOUT_SUFFIX);
        repositoryConfigurationProvider.validateBeforeSaving(configuration);
    }

    @Test(expected = InvalidEntityException.class)
    public void shouldSucceedOnUpdateWithLackOfMirrorWithSlash() throws RestValidationException {
        RepositoryConfigurationRest configuration = createValidConfiguration();
        configuration.setInternalUrl(INVALID_URL);
        repositoryConfigurationProvider.validateBeforeSaving(configuration);
    }

    private RepositoryConfigurationRest createValidConfiguration() {
        RepositoryConfigurationRest repositoryConfigurationRest = new RepositoryConfigurationRest();
        repositoryConfigurationRest.setId(1);
        repositoryConfigurationRest.setInternalUrl(VALID_URL);
        return repositoryConfigurationRest;
    }
}
