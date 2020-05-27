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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProjectProviderTest extends AbstractIntIdProviderTest<Project> {

    @Mock
    private ProjectRepository repository;

    @InjectMocks
    private ProjectProviderImpl provider;

    private Project projectMock = prepareNewProject("mock1");
    private Project projectMockSecond = prepareNewProject("mock2");

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<Project, Integer> repository() {
        return repository;
    }

    @Before
    public void setup() {
        List<Project> projects = new ArrayList<>();

        projects.add(projectMock);
        projects.add(projectMockSecond);
        projects.add(prepareNewProject("pnc-project"));
        projects.add(prepareNewProject("project-zion"));
        projects.add(prepareNewProject("project-trinity"));

        fillRepository(projects);
    }

    @Test
    public void testStoreNewProjectWithoutId() {

        org.jboss.pnc.dto.Project projectDTO = org.jboss.pnc.dto.Project.builder()
                .name("new-project-who-this")
                .description("i'm a happy little project")
                .build();

        org.jboss.pnc.dto.Project projectDTOSaved = provider.store(projectDTO);

        // then
        assertThat(projectDTOSaved.getId()).isNotNull().isNotBlank();
        // check if DTO pre-save is the same as DTO post-save
        assertThat(projectDTOSaved.getName()).isEqualTo(projectDTO.getName());
        assertThat(projectDTOSaved.getDescription()).isEqualTo(projectDTO.getDescription());
    }

    @Test
    public void testStoreNewProjectWithIdShouldFail() {

        org.jboss.pnc.dto.Project projectDTO = org.jboss.pnc.dto.Project.builder()
                .id(Integer.toString(entityId++))
                .name("naughty-project")
                .description("i'm a naughty project")
                .build();

        // then: can't store new project with id already set
        assertThatThrownBy(() -> provider.store(projectDTO)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreProjectWithExistingNameShouldFail() {

        // Prepare
        // return entity of project with same name as dto
        when(repository.queryByPredicates(any(Predicate.class))).thenAnswer(env -> projectMock);

        // when
        org.jboss.pnc.dto.Project projectDTO = org.jboss.pnc.dto.Project.builder().name(projectMock.getName()).build();

        // then
        assertThatThrownBy(() -> provider.store(projectDTO)).isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testUpdate() {

        // Prepare
        String newDescription = projectMock.getDescription() + "-- Updated";

        org.jboss.pnc.dto.Project projectUpdate = org.jboss.pnc.dto.Project.builder()
                .id(projectMock.getId().toString())
                .name(projectMock.getName())
                .description(newDescription)
                .build();

        // when
        org.jboss.pnc.dto.Project projectCheck = provider.update(projectMock.getId().toString(), projectUpdate);

        // then
        // check if dto pre-update is the same as dto retrieved from database post-update
        assertThat(projectCheck.getId()).isEqualTo(projectUpdate.getId());
        assertThat(projectCheck.getName()).isEqualTo(projectUpdate.getName());
        assertThat(projectCheck.getDescription()).isEqualTo(projectUpdate.getDescription());
    }

    @Test
    public void testUpdateWithDifferentExistingNameShouldFail() {
        // Prepare
        // return entity corresponding to the updated name of dto
        when(repository.queryByPredicates(any(Predicate.class))).thenAnswer(env -> projectMockSecond);

        // when
        org.jboss.pnc.dto.Project projectUpdate = org.jboss.pnc.dto.Project.builder()
                .id(projectMock.getId().toString())
                .name(projectMockSecond.getName())
                .description(projectMock.getDescription())
                .build();

        assertThatThrownBy(() -> provider.update(projectMock.getId().toString(), projectUpdate))
                .isInstanceOf(ConflictedEntryException.class);

    }

    @Test
    public void testGetAll() {

        // when
        Page<org.jboss.pnc.dto.Project> all = provider.getAll(0, 10, null, null);

        // then
        assertThat(all.getContent()).hasSize(5);
    }

    @Test
    public void testGetSpecific() {

        // when
        org.jboss.pnc.dto.Project project = provider.getSpecific(projectMock.getId().toString());

        // then
        assertThat(project.getId()).isEqualTo(projectMock.getId().toString());
        assertThat(project.getName()).isEqualTo(projectMock.getName());
    }

    @Test
    public void testDeleteShouldFail() {

        assertThatThrownBy(() -> provider.delete(projectMock.getId().toString()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private Project prepareNewProject(String name) {
        return Project.Builder.newBuilder().id(entityId++).name(name).description("Happy little project").build();
    }

}