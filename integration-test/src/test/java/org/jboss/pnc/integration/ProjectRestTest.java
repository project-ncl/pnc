/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.integration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.path.json.config.JsonPathConfig;
import com.jayway.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.AbstractTest;
import org.jboss.pnc.integration.assertions.ResponseAssertion;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.ProjectRestClient;
import org.jboss.pnc.integration.client.util.RestResponse;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.integration.env.IntegrationTestEnv.getHttpPort;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProjectRestTest extends AbstractTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ProjectRestClient projectRestClient;
    private static BuildConfigurationRestClient buildConfigurationRestClient;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void before() {
        if(projectRestClient == null) {
            projectRestClient = new ProjectRestClient();
        }
        if(buildConfigurationRestClient == null) {
            buildConfigurationRestClient = new BuildConfigurationRestClient();
        }
    }

    @Test
    public void shouldInsertNewProject() throws Exception {
        //given
        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        RestResponse<ProjectRest> response = projectRestClient.createNew(project);

        //than
        assertThat(response.hasValue()).isEqualTo(true);
    }

    @Test
    public void shouldFailBecauseThereIsProjectWithTheSameName() throws Exception {
        //given
        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        RestResponse<ProjectRest> firstResponse = projectRestClient.createNew(project);
        RestResponse<ProjectRest> secondResponse = projectRestClient.createNew(project, false);

        //than
        assertThat(firstResponse.hasValue()).isEqualTo(true);
        assertThat(secondResponse.hasValue()).isEqualTo(false);
    }

    @Test
    public void shouldNotAllowChangingTheListOfConfigurationFromProject() throws Exception {
        //given
        RestResponse<BuildConfigurationRest> configuration = buildConfigurationRestClient.firstNotNull();

        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        project = projectRestClient.createNew(project).getValue();
        project.setConfigurationIds(Arrays.asList(configuration.getValue().getId()));

        ProjectRest updatedProject = projectRestClient.update(project.getId(), project).getValue();

        //than
        assertThat(updatedProject.getConfigurationIds()).isEmpty();
    }

    @Test
    public void shouldDeleteProject() throws Exception {
        //given
        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        RestResponse<ProjectRest> createdProject = projectRestClient.createNew(project);

        //when
        projectRestClient.delete(createdProject.getValue().getId());
        RestResponse<ProjectRest> returnedProject = projectRestClient.get(createdProject.getValue().getId(), false);

        //than
        assertThat(returnedProject.hasValue()).isEqualTo(false);
    }
    @Test
    public void shouldGetBuildConfigurations() throws Exception {
        //when
        RestResponse<List<BuildConfigurationRest>> response = projectRestClient.getBuildConfigurations(100, true);

        //then
        assertThat(response.getValue()).hasSize(1);
    }


    @Test
    @InSequence(999)
    public void shouldDeleteProjectWithConfiguration() throws Exception {
        //given
        BuildConfigurationRest configuration = buildConfigurationRestClient.firstNotNull().getValue();
        assertNotNull(configuration.getId());

        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());
        project.setConfigurationIds(Arrays.asList(configuration.getId()));

        //when
        RestResponse<ProjectRest> createdProject = projectRestClient.createNew(project);
        projectRestClient.delete(createdProject.getValue().getId());
        RestResponse<ProjectRest> returnedProject = projectRestClient.get(createdProject.getValue().getId(), false);

        RestResponse<BuildConfigurationRest> configurationAfterDeletingTheProject = buildConfigurationRestClient
                .get(configuration.getId());

        //then
        assertThat(returnedProject.hasValue()).isEqualTo(false);
        assertThat(configurationAfterDeletingTheProject.hasValue()).isEqualTo(true);
    }
}
