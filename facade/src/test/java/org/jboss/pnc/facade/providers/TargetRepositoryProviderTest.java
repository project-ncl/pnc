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
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.facade.validation.ConflictedEntryException;
import org.jboss.pnc.facade.validation.InvalidEntityException;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
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
public class TargetRepositoryProviderTest extends AbstractIntIdProviderTest<TargetRepository> {

    @Mock
    private TargetRepositoryRepository repository;

    @InjectMocks
    private TargetRepositoryProviderImpl provider;

    private TargetRepository targetRepositoryMock = prepareNewTargetRepository("mock1");
    private TargetRepository targetRepositoryMockSecond = prepareNewTargetRepository("mock2");

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<TargetRepository, Integer> repository() {
        return repository;
    }

    @Before
    public void setup() {
        List<TargetRepository> targetRepositories = new ArrayList<>();

        targetRepositories.add(targetRepositoryMock);
        targetRepositories.add(targetRepositoryMockSecond);
        targetRepositories.add(prepareNewTargetRepository("pnc-targetRepository"));
        targetRepositories.add(prepareNewTargetRepository("targetRepository-zion"));
        targetRepositories.add(prepareNewTargetRepository("targetRepository-trinity"));

        fillRepository(targetRepositories);
    }

    @Test
    public void testStoreNewTargetRepositoryWithoutId() {

        org.jboss.pnc.dto.TargetRepository targetRepositoryDTO = org.jboss.pnc.dto.TargetRepository.refBuilder()
                .identifier("indy-mvn")
                .repositoryPath("/path/to/maven/repo")
                .repositoryType(RepositoryType.MAVEN)
                .temporaryRepo(false)
                .build();

        org.jboss.pnc.dto.TargetRepository targetRepositoryDTOSaved = provider.store(targetRepositoryDTO);

        // then
        assertThat(targetRepositoryDTOSaved.getId()).isNotNull().isNotBlank();
        // check if DTO pre-save is the same as DTO post-save
        assertThat(targetRepositoryDTOSaved.getIdentifier()).isEqualTo(targetRepositoryDTO.getIdentifier());
        assertThat(targetRepositoryDTOSaved.getRepositoryPath()).isEqualTo(targetRepositoryDTO.getRepositoryPath());
        assertThat(targetRepositoryDTOSaved.getRepositoryType()).isEqualTo(targetRepositoryDTO.getRepositoryType());
    }

    @Test
    public void testStoreNewTargetRepositoryWithIdShouldFail() {

        org.jboss.pnc.dto.TargetRepository targetRepositoryDTO = org.jboss.pnc.dto.TargetRepository.refBuilder()
                .id(Integer.toString(entityId.getAndIncrement()))
                .identifier("indy-mvn")
                .repositoryPath("/path/to/naughty/repo")
                .repositoryType(RepositoryType.MAVEN)
                .temporaryRepo(false)
                .build();

        // then: can't store new targetRepository with id already set
        assertThatThrownBy(() -> provider.store(targetRepositoryDTO)).isInstanceOf(InvalidEntityException.class);
    }

    @Test
    public void testStoreTargetRepositoryWithExistingIdentifierAndPathShouldFail() {

        // Prepare
        // return entity of targetRepository with same name as dto
        when(repository.queryByPredicates(any(Predicate.class))).thenAnswer(env -> targetRepositoryMock);

        // when
        org.jboss.pnc.dto.TargetRepository targetRepositoryDTO = org.jboss.pnc.dto.TargetRepository.refBuilder()
                .identifier(targetRepositoryMock.getIdentifier())
                .repositoryPath(targetRepositoryMock.getRepositoryPath())
                .repositoryType(RepositoryType.NPM)
                .temporaryRepo(true)
                .build();

        // then
        assertThatThrownBy(() -> provider.store(targetRepositoryDTO)).isInstanceOf(ConflictedEntryException.class);
    }

    @Test
    public void testUpdate() {

        // Prepare
        String newPath = targetRepositoryMock.getRepositoryPath() + "/updated";

        org.jboss.pnc.dto.TargetRepository targetRepositoryUpdate = org.jboss.pnc.dto.TargetRepository.refBuilder()
                .id(targetRepositoryMock.getId().toString())
                .identifier(targetRepositoryMock.getIdentifier())
                .repositoryPath(newPath)
                .repositoryType(targetRepositoryMock.getRepositoryType())
                .temporaryRepo(targetRepositoryMock.getTemporaryRepo())
                .build();

        assertThatThrownBy(() -> provider.update(targetRepositoryMock.getId().toString(), targetRepositoryUpdate))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testGetAll() {

        // when
        Page<org.jboss.pnc.dto.TargetRepository> all = provider.getAll(0, 10, null, null);

        // then
        assertThat(all.getContent()).hasSize(5);
    }

    @Test
    public void testGetSpecific() {

        // when
        org.jboss.pnc.dto.TargetRepository targetRepository = provider
                .getSpecific(targetRepositoryMock.getId().toString());

        // then
        assertThat(targetRepository.getId()).isEqualTo(targetRepositoryMock.getId().toString());
        assertThat(targetRepository.getIdentifier()).isEqualTo(targetRepositoryMock.getIdentifier());
        assertThat(targetRepository.getRepositoryPath()).isEqualTo(targetRepositoryMock.getRepositoryPath());
        assertThat(targetRepository.getRepositoryType()).isEqualTo(targetRepositoryMock.getRepositoryType());
    }

    @Test
    public void testDeleteShouldFail() {

        assertThatThrownBy(() -> provider.delete(targetRepositoryMock.getId().toString()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    private TargetRepository prepareNewTargetRepository(String name) {
        return TargetRepository.newBuilder()
                .id(entityId.getAndIncrement())
                .identifier("indy-mvn")
                .repositoryPath("/path/to/" + name + "/repo")
                .repositoryType(RepositoryType.MAVEN)
                .temporaryRepo(false)
                .build();
    }

}
