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

import org.assertj.core.api.Condition;
import org.jboss.pnc.dto.BuildConfigurationRevision;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mock.repository.SequenceHandlerRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BuildConfigProviderTest extends AbstractIntIdProviderTest<BuildConfiguration> {

    @Mock
    private BuildConfigurationRepository repository;

    @Mock
    private BuildEnvironmentRepository buildEnvironmentRepository;

    @Mock
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    @Mock
    private UserService userService;

    @Spy
    private SequenceHandlerRepository sequence = new SequenceHandlerRepositoryMock();

    @Spy
    @InjectMocks
    private BuildConfigurationProviderImpl provider;

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<BuildConfiguration, Integer> repository() {
        return repository;
    }

    private BuildConfiguration bc = prepareBuildConfig("Creative", 4, 4, 4);

    @Before
    public void fill() {
        User currentUser = prepareNewUser();
        when(userService.currentUser()).thenReturn(currentUser);
        when(buildEnvironmentRepository.queryById(anyInt())).thenAnswer(i -> mockEnvironment(12));
        final BuildConfiguration a = prepareBuildConfig("First!", 1, 1, 1);
        final BuildConfiguration b = prepareBuildConfig("Second!!", 1, 2, 2, a);
        final BuildConfiguration c = prepareBuildConfig("THIRD!!!", 2, 2, 3, a, b);
        final BuildConfiguration d = prepareBuildConfig("Fourth-_-", 3, 3, 3, a, b, c);
        List<BuildConfiguration> configs = new ArrayList<>(Arrays.asList(new BuildConfiguration[] { a, b, c, d, bc }));
        fillRepository(configs);
    }

    @Test
    public void testGetSpecific() {
        org.jboss.pnc.dto.BuildConfiguration buildConfiguration = provider.getSpecific(bc.getId().toString());

        assertThat(buildConfiguration).isNotNull();
        assertThat(buildConfiguration.getId()).isEqualTo(bc.getId().toString());
        assertThat(buildConfiguration.getName()).isEqualTo(bc.getName());
    }

    @Test
    public void testGetAll() {
        Page<org.jboss.pnc.dto.BuildConfiguration> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(5)
                .haveExactly(1, new Condition<>(e -> e.getName().equals(bc.getName()), "BC Present"));
    }

    @Test
    public void testStore() {
        final String name = "NewHire";
        final String repoId = "12";
        final String envId = "12";
        final String projId = "12";

        org.jboss.pnc.dto.BuildConfiguration buildConfiguration = org.jboss.pnc.dto.BuildConfiguration.builder()
                .name(name)
                .buildType(BuildType.MVN)
                .project(org.jboss.pnc.dto.ProjectRef.refBuilder().id(projId).build())
                .environment(Environment.builder().id(envId).build())
                .scmRepository(SCMRepository.builder().id(repoId).build())
                .build();

        org.jboss.pnc.dto.BuildConfiguration stored = provider.store(buildConfiguration);

        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(stored.getName()).isEqualTo(name);
        assertThat(stored.getProject().getId()).isEqualTo(projId);
        assertThat(stored.getBuildType()).isEqualTo(BuildType.MVN);
        assertThat(stored.getEnvironment().getId()).isEqualTo(envId);
        assertThat(stored.getScmRepository().getId()).isEqualTo(repoId);

    }

    // FIXME unignore after NCL-5114 is implemented
    @Test
    @Ignore
    public void testDeleteWithArchival() {
        // With
        assertThat(bc.getActive()).isTrue();

        // When
        provider.delete(bc.getId().toString());

        // Then
        org.jboss.pnc.dto.BuildConfiguration archived = provider.getSpecific(bc.getId().toString());
        assertThat(archived).isNotNull();
        // assertThat(archived.isArchived()).isTrue();
    }

    @Test
    public void testAddDependency() {
        // With
        org.jboss.pnc.dto.BuildConfiguration dependency = provider.getSpecific("2"); // BC(name: "First!")
        assertThat(dependency.getDependencies()).isNullOrEmpty();

        // When
        provider.addDependency(dependency.getId(), bc.getId().toString());

        // Then
        org.jboss.pnc.dto.BuildConfiguration refreshed = provider.getSpecific(dependency.getId());
        assertThat(refreshed.getDependencies()).containsKey(bc.getId().toString());
    }

    @Test
    public void testRemoveDependency() {
        // With
        org.jboss.pnc.dto.BuildConfiguration dependency = provider.getSpecific("2"); // BC(name: "First!")
        org.jboss.pnc.dto.BuildConfiguration dependant = provider.getSpecific("3"); // BC(name: "Second!!")
        assertThat(dependant.getDependencies()).containsKey(dependency.getId());

        // When
        provider.removeDependency(dependant.getId(), dependency.getId());

        // Then
        org.jboss.pnc.dto.BuildConfiguration refreshed = provider.getSpecific(dependant.getId());
        assertThat(refreshed.getDependencies().values())
                .doNotHave(new Condition<>(dependency::equals, "BC is equal to 'dependency' bc"));
    }

    @Test
    public void testClone() {
        // When
        org.jboss.pnc.dto.BuildConfiguration cloned = provider.clone(bc.getId().toString());

        // Then
        org.jboss.pnc.dto.BuildConfiguration original = provider.getSpecific(bc.getId().toString());
        org.jboss.pnc.dto.BuildConfiguration clonedRefreshed = provider.getSpecific(cloned.getId());

        assertThat(clonedRefreshed)
                .isEqualToIgnoringGivenFields(original, "id", "name", "creationTime", "modificationTime");
    }

    @Test
    public void testGetRevision() {
        // With
        final Integer revision = 1;
        BuildConfigurationAudited bca = BuildConfigurationAudited.fromBuildConfiguration(bc, revision);
        when(buildConfigurationAuditedRepository.queryById(new IdRev(bc.getId(), revision))).thenReturn(bca);

        // When
        BuildConfigurationRevision bcr = provider.getRevision(bc.getId().toString(), revision);

        // Then
        assertThat(bcr).isNotNull();
        assertThat(bcr.getId()).isEqualTo(bc.getId().toString());
        assertThat(bcr.getRev()).isEqualTo(revision);
        assertThat(bcr.getName()).isEqualTo(bc.getName());
        assertThat(bcr.getProject().getId()).isEqualTo(bc.getProject().getId().toString());
        assertThat(bcr.getCreationTime()).isEqualTo(bc.getCreationTime().toInstant());
        assertThat(bcr.getBuildType()).isEqualTo(bc.getBuildType());
    }

    @Test
    public void testRestoreRevision() {
        // With
        final Integer revision = 1;
        final String restoredName = "Old guy";
        BuildConfigurationAudited bca = BuildConfigurationAudited.fromBuildConfiguration(bc, revision);
        bca.setName(restoredName);
        when(buildConfigurationAuditedRepository.queryById(new IdRev(bc.getId(), revision))).thenReturn(bca);

        // When
        provider.restoreRevision(bc.getId().toString(), revision);

        // Then
        org.jboss.pnc.dto.BuildConfiguration restored = provider.getSpecific(bc.getId().toString());
        assertThat(restored).isNotNull();
        assertThat(restored.getName()).isEqualTo(restoredName);
    }

    private BuildConfiguration prepareBuildConfig(
            String name,
            int repoId,
            int projId,
            int envId,
            BuildConfiguration... dependencies) {
        return BuildConfiguration.Builder.newBuilder()
                .id(entityId.getAndIncrement())
                .name(name)
                .creationTime(Date.from(Instant.now()))
                .lastModificationTime(Date.from(Instant.now()))
                .repositoryConfiguration(mockRepoConfig(repoId))
                .project(mockProject(projId))
                .dependencies(new HashSet<>(Arrays.asList(dependencies)))
                .buildEnvironment(mockEnvironment(envId))
                .build();
    }

    private User prepareNewUser() {

        return User.Builder.newBuilder()
                .id(113)
                .email("example@example.com")
                .firstName("Andrea")
                .lastName("Vibelli")
                .username("avibelli")
                .build();
    }

    private RepositoryConfiguration mockRepoConfig(int id) {
        return RepositoryConfiguration.Builder.newBuilder().id(id).build();
    }

    private Project mockProject(int id) {
        return Project.Builder.newBuilder().id(id).build();
    }

    private BuildEnvironment mockEnvironment(int id) {
        return BuildEnvironment.builder().id(id).build();
    }
}
