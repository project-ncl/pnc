/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.facade.validation.ValidationBuilder;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.jboss.pnc.facade.validation.RepositoryViolationException;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.withProductVersionId;

@PermitAll
@Stateless
public class GroupConfigurationProviderImpl extends AbstractProvider<BuildConfigurationSet, GroupConfiguration, GroupConfigurationRef> implements GroupConfigurationProvider {

    @Inject
    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    public GroupConfigurationProviderImpl(BuildConfigurationSetRepository repository, GroupConfigurationMapper mapper) {
        super(repository, mapper, BuildConfigurationSet.class);
    }

    @Override
    public Page<GroupConfiguration> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, isNotArchived());
    }

    @Override
    public GroupConfiguration getSpecific(Integer id) {
        BuildConfigurationSet dbEntity = repository.queryById(id);
        if(dbEntity.isArchived()){
            return null;
        }
        return mapper.toDTO(dbEntity);
    }

    @Override
    public GroupConfiguration update(Integer id, GroupConfiguration restEntity) {
        BuildConfigurationSet dbEntity = repository.queryById(id);
        if (dbEntity != null && dbEntity.isArchived()) {
            throw new RepositoryViolationException("The Group Config " + id + " is already deleted.");
        }
        return super.update(id, restEntity);
    }

    @Override
    public void delete(Integer id) {
        if (hasLink(repository.queryById(id))) {
            archive(id);
        } else {
            super.delete(id);
        }
    }

    @Override
    public Page<GroupConfiguration> getGroupConfigurationsForProductVersion(int pageIndex,
                                                                            int pageSize,
                                                                            String sortingRsql,
                                                                            String query,
                                                                            Integer productVersionId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(productVersionId), isNotArchived());
    }

    @Override
    public Page<GroupConfiguration> getGroupConfigurationsForBuildConfiguration(int pageIndex,
                                                                                int pageSize,
                                                                                String sortingRsql,
                                                                                String query,
                                                                                Integer bcId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildConfigurationId(bcId), isNotArchived());
    }

    private boolean hasLink(BuildConfigurationSet buildConfigurationSet) {
        return !buildConfigurationSet.getBuildConfigSetRecords().isEmpty();
    }

    private void archive(int groupConfigurationId) throws DTOValidationException {
        ValidationBuilder.validateObject(WhenUpdating.class)
                .validateAgainstRepository(repository, groupConfigurationId, true);
        BuildConfigurationSet buildConfigurationSet = repository.queryById(groupConfigurationId);
        buildConfigurationSet.setArchived(true);

        // if a build group is archived, unlink the build group from the build configurations is associated with
        for (BuildConfiguration bc: buildConfigurationSet.getBuildConfigurations()) {
            bc.removeBuildConfigurationSet(buildConfigurationSet);
            buildConfigurationRepository.save(bc);
            buildConfigurationSet.removeBuildConfiguration(bc);
        }

        repository.save(buildConfigurationSet);
    }

    @Override
    public void addConfiguration(int id, int configId) throws DTOValidationException {
        BuildConfigurationSet buildConfigSet = repository.queryById(id);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        ValidationBuilder.validateObject(buildConfigSet, WhenUpdating.class)
                .validateCondition(buildConfigSet != null, "No build configuration set exists with id: " + id)
                .validateCondition(buildConfig != null, "No build configuration exists with id: " + configId);
        if (!buildConfigSet.getBuildConfigurations().contains(buildConfig)) {
            buildConfigSet.addBuildConfiguration(buildConfig);
            repository.save(buildConfigSet);
        }
    }

    @Override
    public void removeConfiguration(int id, int configId) {
        BuildConfigurationSet buildConfigSet = repository.queryById(id);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        ValidationBuilder.validateObject(buildConfigSet, WhenUpdating.class)
                .validateCondition(buildConfigSet != null, "No build configuration set exists with id: " + id)
                .validateCondition(buildConfig != null, "No build configuration exists with id: " + configId);
        if (buildConfigSet.getBuildConfigurations().contains(buildConfig)) {
            buildConfigSet.removeBuildConfiguration(buildConfig);
            repository.save(buildConfigSet);
        }
    }
}
