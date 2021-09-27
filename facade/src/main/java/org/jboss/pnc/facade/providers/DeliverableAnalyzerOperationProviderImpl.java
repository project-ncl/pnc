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

import static org.jboss.pnc.spi.datastore.predicates.OperationPredicates.withMilestoneId;

import java.time.Instant;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.OperationRef;
import org.jboss.pnc.dto.User;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerOperationProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Stateless
public class DeliverableAnalyzerOperationProviderImpl extends
        AbstractUpdatableProvider<Base32LongID, org.jboss.pnc.model.DeliverableAnalyzerOperation, DeliverableAnalyzerOperation, OperationRef>
        implements DeliverableAnalyzerOperationProvider {

    private final Logger logger = LoggerFactory.getLogger(DeliverableAnalyzerOperationProviderImpl.class);

    private ProductMilestoneRepository productMilestoneRepository;

    private UserService userService;

    private UserMapper userMapper;

    @Inject
    public DeliverableAnalyzerOperationProviderImpl(
            ProductMilestoneRepository productMilestoneRepository,
            DeliverableAnalyzerOperationRepository repository,
            DeliverableAnalyzerOperationMapper mapper,
            UserService userService,
            UserMapper userMapper) {
        super(repository, mapper, org.jboss.pnc.model.DeliverableAnalyzerOperation.class);
        this.productMilestoneRepository = productMilestoneRepository;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @Override
    public Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperations(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query);
    }

    @Override
    public Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperationsForMilestone(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String milestoneId) {

        ValidationBuilder.validateObject(null)
                .validateAgainstRepository(productMilestoneRepository, Integer.valueOf(milestoneId), true);
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withMilestoneId(Integer.valueOf(milestoneId)));
    }

    @Override
    public DeliverableAnalyzerOperation store(DeliverableAnalyzerOperation restEntity) throws DTOValidationException {
        org.jboss.pnc.model.User currentUser = userService.currentUser();
        User user = userMapper.toDTO(currentUser);
        Instant now = Instant.now();
        return super.store(restEntity.toBuilder().user(user).startTime(now).build());
    }

    @Override
    public DeliverableAnalyzerOperation saveOrUpdate(String stringId, DeliverableAnalyzerOperation restEntity) {
        DeliverableAnalyzerOperation operation = null;
        // If the operation does not exists store it, otherwise update it
        try {
            Base32LongID id = parseId(stringId);
            findInDB(id);
            // Update will only update the operation endTime and status
            operation = update(stringId, restEntity);
        } catch (RepositoryViolationException e) {
            // The operation does not exist yet
            operation = store(restEntity);
        }
        return operation;
    }

    @Override
    public void delete(String id) {
        throw new UnsupportedOperationException("Deleting operations is prohibited!");
    }

}
