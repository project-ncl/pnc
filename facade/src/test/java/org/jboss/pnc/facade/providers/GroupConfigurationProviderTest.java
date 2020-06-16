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
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.ProductVersionRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GroupConfigurationProviderTest extends AbstractIntIdProviderTest<BuildConfigurationSet> {

    @Mock
    protected BuildConfigurationSetRepository repository;

    @Mock
    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Mock
    private BuildConfigurationRepository buildConfigurationRepository;

    @Spy
    @InjectMocks
    protected GroupConfigurationProviderImpl provider;

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<BuildConfigurationSet, Integer> repository() {
        return repository;
    }

    private BuildConfigurationSet bcs = prepareBuildConfigSet(
            "UNIQUE",
            false,
            mockBuildConfig(1, "CFG"),
            mockBuildConfig(2, "HAPPY CONFIG"));

    @Before
    public void fill() {
        final BuildConfigurationSet a = prepareBuildConfigSet("Wu-tang clan", false);
        final BuildConfigurationSet b = prepareBuildConfigSet("Ain't nothing", false);
        final BuildConfigurationSet c = prepareBuildConfigSet("To frick with!", false);
        List<BuildConfigurationSet> configs = new ArrayList<>(
                Arrays.asList(new BuildConfigurationSet[] { a, b, c, bcs }));
        fillRepository(configs);
    }

    @Test
    public void testGetSpecific() {
        GroupConfiguration groupConfiguration = provider.getSpecific(bcs.getId().toString());

        assertThat(groupConfiguration).isNotNull();
        assertThat(groupConfiguration.getId()).isEqualTo(bcs.getId().toString());
        assertThat(groupConfiguration.getName()).isEqualTo(bcs.getName());
    }

    @Test
    public void testGetAll() {
        Page<GroupConfiguration> all = provider.getAll(0, 10, null, null);

        // note: archival is not mocked for getAll
        assertThat(all.getContent()).hasSize(4)
                .haveExactly(1, new Condition<>(e -> e.getName().equals(bcs.getName()), "GC Present"));
    }

    @Test
    public void testStore() {
        final String name = "NewNAME";
        final String id = "12";

        org.jboss.pnc.dto.GroupConfiguration groupConfiguration = GroupConfiguration.builder()
                .name(name)
                .productVersion(ProductVersionRef.refBuilder().id(id).build())
                .build();

        org.jboss.pnc.dto.GroupConfiguration stored = provider.store(groupConfiguration);

        assertThat(stored).isNotNull();
        assertThat(stored.getId()).isNotNull();
        assertThat(stored.getName()).isEqualTo(name);
        assertThat(stored.getProductVersion().getId()).isEqualTo(id);
    }

    @Test
    public void testAddConfiguration() {
        // With
        final int bcId = 3773;
        final BuildConfiguration buildConfiguration = mockBuildConfig(bcId, "NEWCOMER");
        when(buildConfigurationRepository.queryById(bcId)).thenReturn(buildConfiguration);

        // When
        provider.addConfiguration(bcs.getId().toString(), Integer.toString(bcId));

        // Then
        org.jboss.pnc.dto.GroupConfiguration refreshed = provider.getSpecific(bcs.getId().toString());
        assertThat(refreshed.getBuildConfigs()).containsKey(buildConfiguration.getId().toString());
    }

    @Test
    public void testRemoveConfiguration() {
        // With
        org.jboss.pnc.dto.GroupConfiguration groupConfiguration = provider.getSpecific("1"); //
        BuildConfiguration toRemove = bcs.getBuildConfigurations().stream().findFirst().get();
        when(buildConfigurationRepository.queryById(Integer.valueOf(toRemove.getId()))).thenReturn(toRemove);
        assertThat(groupConfiguration.getBuildConfigs()).containsKey(toRemove.getId().toString());

        // When
        provider.removeConfiguration(groupConfiguration.getId(), toRemove.getId().toString());

        // Then
        org.jboss.pnc.dto.GroupConfiguration refreshed = provider.getSpecific(groupConfiguration.getId());
        assertThat(refreshed.getBuildConfigs().values())
                .doNotHave(new Condition<>(toRemove::equals, "BC is equal to 'toRemove' bc"));
    }

    private BuildConfigurationSet prepareBuildConfigSet(
            String name,
            boolean archived,
            BuildConfiguration... configurations) {
        final BuildConfigurationSet buildConfigurationSet = BuildConfigurationSet.Builder.newBuilder()
                .id(entityId.getAndIncrement())
                .name(name)
                .archived(archived)
                .buildConfigurations(new HashSet<>(Arrays.asList(configurations)))
                .build();
        for (BuildConfiguration config : configurations) {
            config.addBuildConfigurationSet(buildConfigurationSet);
        }
        return buildConfigurationSet;
    }

    private BuildConfiguration mockBuildConfig(int id, String name) {
        return BuildConfiguration.Builder.newBuilder()
                .id(id)
                .name(name)
                .creationTime(Date.from(Instant.now()))
                .lastModificationTime(Date.from(Instant.now()))
                .repositoryConfiguration(mockRepoConfig(1))
                .project(mockProject(1))
                .buildEnvironment(mockEnvironment(1))
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
