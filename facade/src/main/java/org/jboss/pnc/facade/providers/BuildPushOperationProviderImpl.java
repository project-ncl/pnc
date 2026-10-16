/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.providers.api.BuildPushOperationProvider;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildPushOperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.exception.MissingDataException;
import org.jboss.pnc.spi.exception.RemoteRequestException;

@PermitAll
@Stateless
public class BuildPushOperationProviderImpl
        extends OperationProviderImpl<org.jboss.pnc.model.BuildPushOperation, BuildPushOperation>
        implements BuildPushOperationProvider {

    @Inject
    BuildRecordRepository buildRecordRepository;

    @Inject
    ProductMilestoneRepository productMilestoneRepository;

    @Inject
    BuildCoordinator buildCoordinator;

    @Inject
    public BuildPushOperationProviderImpl(BuildPushOperationRepository repository, BuildPushOperationMapper mapper) {
        super(repository, mapper, org.jboss.pnc.model.BuildPushOperation.class);
    }

    @Override
    public Page<BuildPushOperation> getOperationsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {
        validateBuildExists(buildId);

        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, BuildPushPredicates.withBuild(id));
    }

    private void validateBuildExists(String buildId) {
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);
        if (buildRecordRepository.queryById(id) == null) {
            try {
                buildCoordinator.getSubmittedBuildTask(buildId)
                        .orElseThrow(() -> new EmptyEntityException("Build with id " + buildId + " not found"));
            } catch (RemoteRequestException | MissingDataException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Page<BuildPushOperation> getOperationsForMilestone(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            boolean latest,
            String milestoneId) {
        Integer id = ProductMilestoneMapper.idMapper.toEntity(milestoneId);
        if (productMilestoneRepository.queryById(id) == null) {
            throw new EmptyEntityException("Milestone with id " + milestoneId + " not found");
        }

        Set<Base32LongID> buildIds = new HashSet<>(
                buildRecordRepository.queryIdsWithPredicates(
                        BuildRecordPredicates.withStatus(BuildStatus.SUCCESS),
                        BuildRecordPredicates.withPerformedInMilestone(id)));

        Predicate<org.jboss.pnc.model.BuildPushOperation> predicate;
        if (latest) {
            predicate = BuildPushPredicates.latestWithBuilds(buildIds);
        } else {
            predicate = BuildPushPredicates.withBuilds(buildIds);
        }
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, predicate);
    }
}
