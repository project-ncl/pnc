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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Condition;

import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DeliverableAnalyzerOperationProviderImplTest
        extends AbstractBase32LongIDProviderTest<DeliverableAnalyzerOperation> {

    private final Logger logger = LoggerFactory.getLogger(DeliverableAnalyzerOperationProviderImplTest.class);

    private static final String USER_TOKEN = "token";

    @Mock
    private DeliverableAnalyzerOperationRepository repository;

    @Mock
    private ProductMilestoneRepository productMilestoneRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private DeliverableAnalyzerOperationProviderImpl provider;

    private User user;

    @Override
    protected AbstractProvider provider() {
        return provider;
    }

    @Override
    protected Repository<DeliverableAnalyzerOperation, Base32LongID> repository() {
        return repository;
    }

    @Before
    @Override
    public void prepareRepositoryMock() {
        when(em.getReference(any(), any())).thenAnswer(inv -> {
            Class<GenericEntity> type = inv.getArgument(0, Class.class);
            Serializable id = inv.getArgument(1, Serializable.class);
            GenericEntity mock = mock(type);
            when(mock.getId()).thenReturn(id);
            return mock;
        });
        when(repository().queryWithPredicates(any(), any(), any())).thenAnswer(new ListAnswer(repositoryList));
        when(repository().count(any())).thenAnswer(inv -> repositoryList.size());
        when(repository().queryById(any())).thenAnswer(inv -> {
            Base32LongID id = inv.getArgument(0);
            return repositoryList.stream().filter(a -> id.equals(a.getId())).findFirst().orElse(null);
        });
        when(repository().save(any())).thenAnswer(inv -> {
            DeliverableAnalyzerOperation entity = inv.getArgument(0);
            if (entity.getId() == null) {
                entity.setId(getNextId());
                repositoryList.add(entity);
                return entity;
            } else {
                boolean found = false;
                for (int i = 0; i < repositoryList.size(); i++) {
                    if (repositoryList.get(i).getId().equals(entity.getId())) {
                        repositoryList.set(i, entity);
                        return entity;
                    }
                }
                if (!found) {
                    repositoryList.add(entity);
                    return entity;
                }
            }
            throw new IllegalArgumentException("Provided entity has ID but is not in the repository.");
        });
        doAnswer(inv -> {
            Base32LongID id = inv.getArgument(0);
            DeliverableAnalyzerOperation object = repositoryList.stream()
                    .filter(a -> id.equals(a.getId()))
                    .findFirst()
                    .orElse(null);
            repositoryList.remove(object);
            return null;
        }).when(repository()).delete(any());
    }

    @Before
    public void prepareMock() throws ReflectiveOperationException, IllegalArgumentException {

        user = mock(User.class);
        when(user.getLoginToken()).thenReturn(USER_TOKEN);
        when(userService.currentUser()).thenReturn(user);

    }

    private DeliverableAnalyzerOperation mockDeliverableAnalyzerOperation() {
        Map<String, String> operationParameters = new HashMap<String, String>();
        operationParameters.put("url-0", "https://github.com/project-ncl/pnc/archive/refs/tags/2.1.1.tar.gz");
        operationParameters.put("url-1", "https://github.com/project-ncl/pnc-common/archive/refs/tags/2.1.0.zip");
        DeliverableAnalyzerOperation operation = DeliverableAnalyzerOperation.Builder.newBuilder()
                .id(getNextId())
                .startTime(new Date())
                .progressStatus(ProgressStatus.IN_PROGRESS)
                .operationParameters(operationParameters)
                .build();
        try {
            Thread.sleep(1L); // make sure there are no two builds with the same start date
        } catch (InterruptedException e) {
            logger.error("I can get no sleep.", e);
        }
        repositoryList.add(0, operation);
        return operation;
    }

    @Test
    public void testGetSpecificOperation() {
        DeliverableAnalyzerOperation operation = mockDeliverableAnalyzerOperation();

        org.jboss.pnc.dto.DeliverableAnalyzerOperation specific = provider
                .getSpecific(DeliverableAnalyzerOperationMapper.idMapper.toDto(operation.getId()));
        assertThat(specific.getId()).isEqualTo(DeliverableAnalyzerOperationMapper.idMapper.toDto(operation.getId()));
        assertThat(specific.getStartTime()).isEqualTo(operation.getStartTime().toInstant());
        assertThat(specific.getProgressStatus()).isEqualTo(operation.getProgressStatus());
    }

    @Test
    public void testGetAll() throws InterruptedException {
        DeliverableAnalyzerOperation operation1 = mockDeliverableAnalyzerOperation();
        Thread.sleep(1L); // make sure new start time is in the next millisecond
        DeliverableAnalyzerOperation operation2 = mockDeliverableAnalyzerOperation();
        Thread.sleep(1L); // make sure new start time is in the next millisecond
        DeliverableAnalyzerOperation operation3 = mockDeliverableAnalyzerOperation();
        Page<org.jboss.pnc.dto.DeliverableAnalyzerOperation> all = provider.getAll(0, 10, null, null);

        assertThat(all.getContent()).hasSize(3)
                .haveExactly(
                        1,
                        new Condition<>(op -> operation1.getId().getId().equals(op.getId()), "Operation 1 present"))
                .haveExactly(
                        1,
                        new Condition<>(op -> operation2.getId().getId().equals(op.getId()), "Operation 2 present"))
                .haveExactly(
                        1,
                        new Condition<>(op -> operation3.getId().getId().equals(op.getId()), "Operation 3 present"));
    }
}
