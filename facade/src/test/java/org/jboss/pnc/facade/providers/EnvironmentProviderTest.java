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
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.facade.providers.api.UserRoles;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EnvironmentProviderTest extends AbstractIntIdProviderTest<BuildEnvironment> {

    @Mock
    private BuildEnvironmentRepository repository;

    @Spy
    @InjectMocks
    private EnvironmentProviderImpl provider;

    @Mock
    private UserService userService;

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<BuildEnvironment, Integer> repository() {
        return repository;
    }

    private BuildEnvironment env = prepareBuildEnvironment("Crash Test Dummy 1");

    @Before
    public void fill() {
        List<BuildEnvironment> envs = new ArrayList<>();
        envs.add(prepareBuildEnvironment("Hello"));
        envs.add(prepareBuildEnvironment("My name is"));
        envs.add(prepareBuildEnvironment("S-S-S-Slim Shady"));
        envs.add(env);
        fillRepository(envs);
    }

    @Before
    public void prepareMocks() throws ReflectiveOperationException {
        when(userService.hasLoggedInUserRole(UserRoles.SYSTEM_USER)).thenReturn(true);
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

    @Test
    public void testGetAll() {
        Page<Environment> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(4)
                .haveExactly(1, new Condition<>(e -> e.getName().equals(env.getName()), "Environment present"));
    }

    @Test
    public void testStore() {
        final String name = "Hello";
        final String sysImageId = "NewID";
        final String sysRepoUrl = "quay.io/rh-newcastle";

        // when
        Environment environment = Environment.builder()
                .name(name)
                .systemImageId(sysImageId)
                .systemImageRepositoryUrl(sysRepoUrl)
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .build();

        org.jboss.pnc.dto.Environment envDTOSaved = provider.store(environment);

        // then
        assertThat(envDTOSaved.getId()).isNotNull().isNotBlank();
        // check if DTO pre-save is the same as DTO post-save
        assertThat(envDTOSaved.getName()).isEqualTo(environment.getName());
        assertThat(envDTOSaved.getSystemImageId()).isEqualTo(environment.getSystemImageId());
        assertThat(envDTOSaved.getSystemImageRepositoryUrl()).isEqualTo(environment.getSystemImageRepositoryUrl());
    }

    @Test
    public void testGetSpecific() {
        Environment environment = provider.getSpecific(env.getId().toString());

        assertThat(environment.getId()).isEqualTo(env.getId().toString());
        assertThat(environment.getName()).isEqualTo(env.getName());
    }

    @Test
    public void testUpdate() {
        final String newName = "newName";

        Environment environment = Environment.builder()
                .id(env.getId().toString())
                .name(newName)
                .systemImageId(env.getSystemImageId())
                .deprecated(env.isDeprecated())
                .systemImageType(env.getSystemImageType())
                .description(env.getDescription())
                .systemImageRepositoryUrl(env.getSystemImageRepositoryUrl())
                .attributes(env.getAttributes())
                .build();

        assertThat(env.getName()).isNotEqualTo(environment.getName());

        assertThatThrownBy(() -> provider.update(env.getId().toString(), environment))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testDelete() {
        assertThatThrownBy(() -> provider.delete(env.getId().toString()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private BuildEnvironment prepareBuildEnvironment(String name) {
        return BuildEnvironment.builder()
                .id(entityId.getAndIncrement())
                .name(name)
                .description("Am I even?")
                .systemImageId("ID!")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .systemImageRepositoryUrl("http://repogetcha.com")
                .deprecated(false)
                .attribute("MVN", "3.5.3")
                .build();
    }
}
