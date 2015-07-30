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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.pnc.integration.client.BuildConfigurationRestClient;
import org.jboss.pnc.integration.client.ClientResponse;
import org.jboss.pnc.integration.client.ProjectRestClient;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.rest.endpoint.ProjectEndpoint;
import org.jboss.pnc.rest.provider.ProjectProvider;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProjectRestTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static ProjectRestClient projectRest;

    @Deployment(testable = false)
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEar();

        WebArchive restWar = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        restWar.addClass(ProjectProvider.class);
        restWar.addClass(ProjectEndpoint.class);
        restWar.addClass(ProjectRest.class);

        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Before
    public void initializeClient() throws Exception {
        if(projectRest == null) {
            projectRest = ProjectRestClient.firstNotNull();
        }
    }

    @Test
    public void shouldInsertNewProject() throws Exception {
        //given
        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        ClientResponse response = projectRest.createNew(project);

        //than
        assertThat(response.getHttpCode()).isEqualTo(201);
        assertThat(response.getId().isPresent()).isEqualTo(true);
    }

    @Test
    public void shouldFailBecauseThereIsProjectWithTheSameName() throws Exception {
        //given
        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        ClientResponse firstResponse = projectRest.createNew(project);
        ClientResponse secondResponse = projectRest.createNew(project);

        //than
        assertThat(firstResponse.getHttpCode()).isEqualTo(201);
        assertThat(secondResponse.getHttpCode()).isEqualTo(409);
    }

    @Test
    public void shouldAllowToAddConfiguration() throws Exception {
        //given
        BuildConfigurationRestClient configuration = BuildConfigurationRestClient.firstNotNull();

        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        ClientResponse createdProject = projectRest.createNew(project);

        int projectId = createdProject.getId().get();
        project.setConfigurationIds(Arrays.asList(configuration.getBuildConfigurationId()));

        ClientResponse updatedProject = projectRest.update(projectId, project);

        //than
        assertThat(createdProject.getHttpCode()).isEqualTo(201);
        assertThat(updatedProject.getHttpCode()).isEqualTo(200);
    }

    @Test
    public void shouldDeleteProject() throws Exception {
        //given
        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());

        //when
        ClientResponse createdProject = projectRest.createNew(project);
        int projectId = createdProject.getId().get();

        ClientResponse deletedProject = projectRest.delete(projectId);

        //than
        assertThat(deletedProject.getHttpCode()).isEqualTo(200);
    }

    @Test
    @InSequence(999)
    public void shouldDeleteProjectWithConfiguration() throws Exception {
        //given
        BuildConfigurationRestClient configuration = BuildConfigurationRestClient.firstNotNull();
        assertNotNull(configuration.getBuildConfigurationId());

        ProjectRest project = new ProjectRest();
        project.setName(UUID.randomUUID().toString());
        project.setConfigurationIds(Arrays.asList(configuration.getBuildConfigurationId()));

        //when
        ClientResponse createdProject = projectRest.createNew(project);
        int projectId = createdProject.getId().get();

        ClientResponse deletedProject = projectRest.delete(projectId);

        assertNotNull(configuration);
        assertNotNull(configuration.getBuildConfigurationId());

        ClientResponse configurationAfterDeletingTheProject = configuration.get(configuration.getBuildConfigurationId());

        //then
        assertThat(deletedProject.getHttpCode()).isEqualTo(200);
        assertThat(configurationAfterDeletingTheProject.getHttpCode()).isEqualTo(200);
    }

}
