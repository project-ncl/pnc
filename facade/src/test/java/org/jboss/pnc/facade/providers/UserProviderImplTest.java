/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserProviderImplTest extends AbstractIntIdProviderTest<User> {

    @Mock
    private UserRepository repository;

    @Mock
    private UserService service;

    @InjectMocks
    private UserProviderImpl provider;

    private User userMock = prepareNewUser("tada");

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<User, Integer> repository() {
        return repository;
    }

    @Before
    public void setup() {
        List<User> users = new ArrayList<>();
        users.add(prepareNewUser("boris"));
        users.add(prepareNewUser("theresa"));
        users.add(prepareNewUser("david"));
        users.add(userMock);

        fillRepository(users);
    }

    @Test
    public void testGetCurrentUser() {
        // Prepare
        User user = repositoryList.get(0);

        when(service.currentUser()).thenAnswer(inv -> user);

        // When
        org.jboss.pnc.dto.User userDTO = provider.getCurrentUser();

        // then
        assertThat(userDTO.getId()).isEqualTo(user.getId().toString());
        assertThat(userDTO.getUsername()).isEqualTo(user.getUsername());
    }

    @Test
    public void testGetAll() {

        // when
        Page<org.jboss.pnc.dto.User> all = provider.getAll(0, 10, null, null);

        // then
        assertThat(all.getContent()).hasSize(4);
    }

    @Test
    public void testGetSpecific() {
        // when
        org.jboss.pnc.dto.User user = provider.getSpecific(userMock.getId().toString());

        // then
        assertThat(user.getId()).isEqualTo(userMock.getId().toString());
        assertThat(user.getUsername()).isEqualTo(userMock.getUsername());
    }

    @Test
    public void testStoreNewUserWithIdShouldFail() {

        // when
        org.jboss.pnc.dto.User userDTO = org.jboss.pnc.dto.User.builder()
                .id(Integer.toString(entityId.getAndIncrement()))
                .username("john-cormack")
                .build();

        // then: can't store new user with id already set
        assertThatThrownBy(() -> provider.store(userDTO)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreNewUserWithoutId() {

        // when
        org.jboss.pnc.dto.User userDTO = org.jboss.pnc.dto.User.builder().username("john-cormack").build();

        org.jboss.pnc.dto.User userDTOSaved = provider.store(userDTO);

        // then
        assertThat(userDTOSaved.getId()).isNotNull().isNotBlank();
        assertThat(userDTOSaved.getUsername()).isEqualTo(userDTO.getUsername());
    }

    @Test
    public void testUpdateShouldFail() {

        org.jboss.pnc.dto.User userDTO = org.jboss.pnc.dto.User.builder().id("1001").username("john-cormack").build();

        assertThatThrownBy(() -> provider.update("1001", userDTO)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testDeleteShouldFail() {
        assertThatThrownBy(() -> provider.delete("hello-test")).isInstanceOf(UnsupportedOperationException.class);
    }

    private User prepareNewUser(String username) {

        return User.Builder.newBuilder()
                .id(entityId.getAndIncrement())
                .email("example@example.com")
                .firstName("Boris")
                .lastName("Caesar")
                .username(username)
                .build();
    }
}
