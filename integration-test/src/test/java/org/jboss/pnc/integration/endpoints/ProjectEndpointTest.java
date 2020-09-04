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
package org.jboss.pnc.integration.endpoints;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.client.patch.ProjectPatchBuilder;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.integration.setup.Deployments;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.dto.BuildConfiguration;

@RunAsClient
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class ProjectEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(ProjectEndpointTest.class);

    private ProjectClient client = new ProjectClient(RestClientConfiguration.asUser());

    private BuildConfigurationClient buildConfigClient = new BuildConfigurationClient(RestClientConfiguration.asUser());

    @Deployment
    public static EnterpriseArchive deploy() {
        return Deployments.testEar();
    }

    @Test
    public void shouldPatchProject() throws ClientException, PatchBuilderException {

        Project project = client.getAll().iterator().next();

        String newDescription = "Testing patch support";
        String newUrl = "http://example.com";

        String id = project.getId();

        ProjectPatchBuilder builder = new ProjectPatchBuilder().replaceDescription(newDescription)
                .replaceProjectUrl(newUrl);

        Project updated = client.patch(id, builder);

        assertThat(updated.getDescription()).isEqualTo(newDescription);
        assertThat(updated.getProjectUrl()).isEqualTo(newUrl);

        String newDescription2 = "Testing patch support 2";
        ProjectPatchBuilder builder2 = new ProjectPatchBuilder().replaceDescription(newDescription2);

        Project updated2 = client.patch(id, builder2.getJsonPatch(), Project.class);
        assertThat(updated2.getDescription()).isEqualTo(newDescription2);
    }

    @Test
    public void testUpdate() throws Exception {

        // given
        Project createProject = Project.builder()
                .projectUrl("https://github.com/entity-ncl/pnc")
                .issueTrackerUrl("https://github.com/entity-ncl/pnc/issues")
                .description("hello world")
                .name("Test: " + UUID.randomUUID().toString())
                .build();

        Project projectReturned = client.createNew(createProject);

        // given
        String newDescription = "Amazing project hhhhhhh " + UUID.randomUUID().toString();

        Project project = Project.builder()
                .projectUrl(createProject.getProjectUrl())
                .issueTrackerUrl(createProject.getIssueTrackerUrl())
                .description(newDescription)
                .name(createProject.getName())
                .id(projectReturned.getId())
                .build();

        // when
        client.update(projectReturned.getId(), project);

        // then: everything else should be the same, minus description
        Project retrieved = client.getSpecific(projectReturned.getId());

        assertThat(retrieved.getId()).isEqualTo(projectReturned.getId());
        assertThat(retrieved.getName()).isEqualTo(createProject.getName());
        assertThat(retrieved.getDescription()).isEqualTo(newDescription);
        assertThat(retrieved.getProjectUrl()).isEqualTo(createProject.getProjectUrl());
        assertThat(retrieved.getIssueTrackerUrl()).isEqualTo(createProject.getIssueTrackerUrl());
    }

    @Test
    public void shouldInsertNewProject() throws Exception {
        // given
        Project original = Project.builder()
                .projectUrl("https://github.com/entity-ncl/pnc")
                .issueTrackerUrl("https://github.com/entity-ncl/pnc/issues")
                .name("Test: " + UUID.randomUUID().toString())
                .build();

        // when
        Project projectReturned = client.createNew(original);

        // then
        assertThat(projectReturned.getId()).isNotNull().isNotEmpty();
        assertThat(projectReturned.getName()).isEqualTo(original.getName());
        assertThat(projectReturned.getProjectUrl()).isEqualTo(original.getProjectUrl());
        assertThat(projectReturned.getIssueTrackerUrl()).isEqualTo(original.getIssueTrackerUrl());
    }

    @Test
    public void testGetSpecific() throws ClientException {

        String id = client.getAll().iterator().next().getId();

        assertThat(id).isNotNull().isNotEmpty();

        Project project = client.getSpecific(id);
        assertThat(project.getId()).isEqualTo(id);
        assertThat(project.getName()).isNotNull().isNotEmpty();
        assertThat(project.getDescription()).isNotNull();
    }

    @Test
    public void shouldFailBecauseThereIsProjectWithTheSameName() throws ClientException {

        // given
        Project project = Project.builder().name("Scotch on a yacht: " + UUID.randomUUID().toString()).build();

        Project created = client.createNew(project);

        // then
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(project.getName());

        assertThatThrownBy(() -> client.createNew(project))
                .withFailMessage("We shouldn't be able to create projects with same name")
                .isInstanceOf(ClientException.class);
    }

    @Test
    public void shouldNotAllowChangingTheListOfConfigurationFromProject() throws ClientException {

        // when
        Project existingProject = client.getSpecific("100");
        Map<String, BuildConfigurationRef> buildConfigRefs = existingProject.getBuildConfigs();

        Project original = Project.builder().name("New name new me: " + UUID.randomUUID().toString()).build();

        Project projectReturned = client.createNew(original);
        Project projectUpdateRequest = Project.builder()
                .id(projectReturned.getId())
                .name(projectReturned.getName())
                .buildConfigs(buildConfigRefs)
                .build();

        client.update(projectReturned.getId(), projectUpdateRequest);

        Project projectUpdated = client.getSpecific(projectReturned.getId());

        // then
        assertThat(projectUpdated.getBuildConfigs()).hasSize(0);
    }

    @Test
    public void shouldGetBuildConfigurations() throws ClientException {

        // when: build config with id 100 (from test imported data) should
        // have 1 buildconfig
        RemoteCollection<BuildConfiguration> project = client.getBuildConfigurations("100");

        // then
        assertThat(project).isNotNull().hasSize(1);
    }
}
