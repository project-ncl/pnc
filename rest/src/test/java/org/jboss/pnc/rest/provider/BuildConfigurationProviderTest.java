/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.restmodel.BuildConfigurationAuditedRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.validation.exceptions.RestValidationException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 3/14/16
 * Time: 9:25 AM
 */
public class BuildConfigurationProviderTest {

    private static final int EXISTING_ID = 1243;
    private static final int PROJECT_ID = 234;
    private static final int ENVIRONMENT_ID = 42;
    private static final int REPOSITORY_ID = 666;
    private static final String SCM_REVISION = "master";
    private static final String CONFIG_NAME = "config";
    private static final String BUILD_SCRIPT = "mvn clean";
    private static final String DESCRIPTION = "Some Configuration";

    private static final RepositoryConfigurationRest REPOSITORY = createRepository(REPOSITORY_ID);
    private static final ProjectRest PROJECT = createProject(PROJECT_ID);
    private static final BuildEnvironmentRest ENVIRONMENT = createEnvironment(ENVIRONMENT_ID);

    @Mock
    private Repository<BuildConfiguration, Integer> repository;
    @Mock
    private ScmModuleConfig scmModuleConfig;

    @Mock
    private BuildConfigurationAuditedRepository auditedBCRepository;

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

    @Test
    public void testGetLatestAuditedMatchingBCRest(){
        Map<String, String> params = new HashMap();
        params.put("Key", "Value");

        BuildConfigurationAudited matching1 = createBuildConfigurationAudited(1,
                ENVIRONMENT_ID, BUILD_SCRIPT, null, params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited matching2 = createBuildConfigurationAudited(2,
                ENVIRONMENT_ID, BUILD_SCRIPT, null, params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);

        BuildConfigurationAudited mismatching3 = createBuildConfigurationAudited(3,
                ENVIRONMENT_ID+1, BUILD_SCRIPT, DESCRIPTION, params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited mismatching4 = createBuildConfigurationAudited(4,
                ENVIRONMENT_ID, BUILD_SCRIPT+"a", DESCRIPTION, params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited mismatching5 = createBuildConfigurationAudited(5,
                ENVIRONMENT_ID, BUILD_SCRIPT, DESCRIPTION+"a", params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited mismatching6 = createBuildConfigurationAudited(6,
                ENVIRONMENT_ID, BUILD_SCRIPT, DESCRIPTION, new HashMap(), CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited mismatching7 = createBuildConfigurationAudited(7,
                ENVIRONMENT_ID, BUILD_SCRIPT, DESCRIPTION, params, CONFIG_NAME+"a", PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited mismatching8 = createBuildConfigurationAudited(8,
                ENVIRONMENT_ID, BUILD_SCRIPT, DESCRIPTION, params, CONFIG_NAME, PROJECT_ID+1,
                REPOSITORY_ID, SCM_REVISION);
        BuildConfigurationAudited mismatching9 = createBuildConfigurationAudited(9,
                ENVIRONMENT_ID, BUILD_SCRIPT, DESCRIPTION, params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID+1, SCM_REVISION);
        BuildConfigurationAudited mismatching10 = createBuildConfigurationAudited(10,
                ENVIRONMENT_ID, BUILD_SCRIPT, DESCRIPTION, params, CONFIG_NAME, PROJECT_ID,
                REPOSITORY_ID, SCM_REVISION+"a");

        List<BuildConfigurationAudited> bcas = Arrays.asList(mismatching10, mismatching9,
                mismatching8, mismatching7, mismatching6, mismatching5, mismatching4, mismatching3,
                matching2, matching1);
        when(auditedBCRepository.findAllByIdOrderByRevDesc(EXISTING_ID)).thenReturn(bcas);


        BuildConfigurationRest configuration = createValidConfiguration();
        configuration.setId(EXISTING_ID);
        configuration.setParameters(params);
        Optional<BuildConfigurationAuditedRest> bcaRest = provider
                .getLatestAuditedMatchingBCRest(configuration);

        assertTrue(bcaRest.isPresent());
        assertEquals(EXISTING_ID, bcaRest.get().getId().intValue());
        assertEquals(2, bcaRest.get().getRev().intValue()); // matching with higest rev.
    }

    private BuildConfigurationAudited createBuildConfigurationAudited(int rev, int environmentID,
            String buildScript, String description, Map<String, String> parameters, String name,
            int projectID, int repositoryID, String scmRevision) {
        BuildConfigurationAudited bca = new BuildConfigurationAudited();
        bca.setId(EXISTING_ID);
        bca.setRev(rev);
        bca.setIdRev(new IdRev(EXISTING_ID, rev));
        bca.setBuildEnvironment(BuildEnvironment.Builder.newBuilder().id(environmentID).build());
        bca.setBuildScript(buildScript);
        bca.setDescription(description);
        bca.setGenericParameters(parameters);
        bca.setName(name);
        bca.setProject(Project.Builder.newBuilder().id(projectID).build());
        bca.setRepositoryConfiguration(RepositoryConfiguration.Builder.newBuilder().id(repositoryID).build());
        bca.setScmRevision(scmRevision);
        return bca;
    }

    private BuildConfigurationRest createValidConfiguration() {
        BuildConfigurationRest configuration = new BuildConfigurationRest();
        configuration.setProject(PROJECT);
        configuration.setName(CONFIG_NAME);
        configuration.setDescription(null);
        configuration.setScmRevision(SCM_REVISION);
        configuration.setRepositoryConfiguration(REPOSITORY);
        configuration.setBuildType(BuildType.MVN);
        configuration.setEnvironment(ENVIRONMENT);
        configuration.setBuildScript(BUILD_SCRIPT);
        return configuration;
    }

    private static BuildEnvironmentRest createEnvironment(int id) {
        BuildEnvironmentRest environment = new BuildEnvironmentRest();
        environment.setId(id);
        return environment;
    }

    private static RepositoryConfigurationRest createRepository(int id) {
        RepositoryConfigurationRest repository = new RepositoryConfigurationRest();
        repository.setId(id);
        return repository;
    }

    private static ProjectRest createProject(int id) {
        ProjectRest project = new ProjectRest();
        project.setId(id);
        return project;
    }
}