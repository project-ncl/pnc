/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.spi.datastore.predicates.OperationPredicates.withMilestoneId;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerOperationProvider;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.DeliverableAnalyzerOperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
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
