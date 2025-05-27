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

import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.facade.providers.api.BuildPushOperationProvider;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.BuildPushOperationMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.spi.datastore.predicates.BuildPushPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildPushOperationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

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
        Base32LongID id = BuildMapper.idMapper.toEntity(buildId);

        ValidationBuilder.validateObject(null).validateAgainstRepository(buildRecordRepository, id, true);
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, BuildPushPredicates.withBuild(id));
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
        ValidationBuilder.validateObject(null).validateAgainstRepository(productMilestoneRepository, id, true);

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
