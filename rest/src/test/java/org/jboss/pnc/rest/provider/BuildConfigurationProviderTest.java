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
import org.jboss.pnc.common.json.moduleconfig.ScmModuleConfig;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 3/14/16
 * Time: 9:25 AM
 */
public class BuildConfigurationProviderTest {

    private static final int EXISTING_ID = 1243;

    @Mock
    private Repository<BuildConfiguration, Integer> repository;
    @Mock
    private ScmModuleConfig scmModuleConfig;

    @InjectMocks
    private BuildConfigurationProvider provider = new BuildConfigurationProvider();

    @Before
    public void setUp() throws ConfigurationParseException {
        MockitoAnnotations.initMocks(this);
        when(repository.queryById(EXISTING_ID)).thenReturn(BuildConfiguration.Builder.newBuilder().build());
        when(scmModuleConfig.getInternalScmAuthority()).thenReturn("git@github.com");
    }

    @Test
    public void shouldSucceedOnUpdateWithLackOfMirrorGitUrl() throws RestValidationException {
        BuildConfigurationRest configuration = createValidConfiguration();
        configuration.setId(EXISTING_ID);
        provider.validateBeforeUpdating(EXISTING_ID, configuration);
    }

    @Test
    public void shouldSucceedOnLackOfMirrorGitUrl() throws RestValidationException {
        BuildConfigurationRest configuration = createValidConfiguration();
        provider.validateBeforeSaving(configuration);
    }

    private BuildConfigurationRest createValidConfiguration() {
        RepositoryConfigurationRest repositoryConfigurationRest = new RepositoryConfigurationRest();
        repositoryConfigurationRest.setId(1);

        BuildConfigurationRest configuration = new BuildConfigurationRest();
        configuration.setProject(createProject());
        configuration.setName("config");
        configuration.setRepositoryConfiguration(repositoryConfigurationRest);
        configuration.setEnvironment(new BuildEnvironmentRest());
        return configuration;
    }

    private ProjectRest createProject() {
        ProjectRest project = new ProjectRest();
        project.setId(234);
        return project;
    }
}