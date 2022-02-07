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

import static org.jboss.pnc.spi.datastore.predicates.OperationPredicates.withMilestoneId;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.api.enums.ProgressStatus;
import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.common.logging.MDCUtils;
import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerOperationProvider;
import org.jboss.pnc.facade.util.UserService;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.mapper.api.UserMapper;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.repositories.DeliverableAnalyzerOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Stateless
public class DeliverableAnalyzerOperationProviderImpl
        extends OperationProviderImpl<org.jboss.pnc.model.DeliverableAnalyzerOperation, DeliverableAnalyzerOperation>
        implements DeliverableAnalyzerOperationProvider {

    private final Logger logger = LoggerFactory.getLogger(DeliverableAnalyzerOperationProviderImpl.class);

    private ProductMilestoneRepository productMilestoneRepository;
    private ProductMilestoneMapper milestoneMapper;

    @Inject
    public DeliverableAnalyzerOperationProviderImpl(
            ProductMilestoneRepository productMilestoneRepository,
            DeliverableAnalyzerOperationRepository repository,
            DeliverableAnalyzerOperationMapper mapper,
            ProductMilestoneMapper milestoneMapper) {
        super(repository, mapper, org.jboss.pnc.model.DeliverableAnalyzerOperation.class);
        this.productMilestoneRepository = productMilestoneRepository;
        this.milestoneMapper = milestoneMapper;
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

}
