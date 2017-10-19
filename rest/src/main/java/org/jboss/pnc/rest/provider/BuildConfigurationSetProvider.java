/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.validation.ValidationBuilder;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.InvalidEntityException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.withName;

@Stateless
public class BuildConfigurationSetProvider extends AbstractProvider<BuildConfigurationSet, BuildConfigurationSetRest> {

    Logger logger = LoggerFactory.getLogger(BuildConfigurationSetProvider.class);

    private BuildConfigurationRepository buildConfigurationRepository;

    public BuildConfigurationSetProvider() {
    }

    @Inject
    public BuildConfigurationSetProvider(BuildConfigurationSetRepository buildConfigurationSetRepository,
            BuildConfigurationRepository buildConfigurationRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(buildConfigurationSetRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildConfigurationRepository = buildConfigurationRepository;
    }

    @Override //better error logging
    public BuildConfigurationSetRest getSpecific(Integer id) {
        BuildConfigurationSet dbEntity = repository.queryById(id);
        try {
            return new BuildConfigurationSetRest(dbEntity);
        } catch (Exception e) {
            logger.error("Cannot create rest entity.", e);
            return null;
        }
    }

    @Override
    protected void validateBeforeSaving(BuildConfigurationSetRest buildConfigurationSetRest) throws ValidationException {
        BuildConfigurationSet buildConfigurationSetFromDB = repository
                .queryByPredicates(withName(buildConfigurationSetRest.getName()));
        if (buildConfigurationSetFromDB != null) {
            throw new ConflictedEntryException("BuildConfigurationSet with this name already exists", BuildConfigurationSet.class,
                    buildConfigurationSetFromDB.getId());
        }
    }

    @Override
    protected Function<? super BuildConfigurationSet, ? extends BuildConfigurationSetRest> toRESTModel() {
        return buildConfigurationSet -> {
            try {
                return new BuildConfigurationSetRest(buildConfigurationSet);
            } catch (Exception e) {
                logger.error("Cannot create rset entity.", e);
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    protected Function<? super BuildConfigurationSetRest, ? extends BuildConfigurationSet> toDBModel() {
        return buildConfigSetRest -> {
            try {
                return buildConfigSetRest.toDBEntityBuilder().build();
            } catch (Exception e) {
                logger.error("Cannot create rset entity.", e);
                throw new RuntimeException(e);
            }
        };
    }

    public void addConfiguration(Integer configSetId, Integer configId) throws ValidationException {
        BuildConfigurationSet buildConfigSet = repository.queryById(configSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        ValidationBuilder.validateObject(buildConfigSet, WhenUpdating.class)
                .validateCondition(buildConfigSet != null, "No build configuration set exists with id: " + configSetId)
                .validateCondition(buildConfig != null, "No build configuration exists with id: " + configId);
        if (buildConfigSet.getBuildConfigurations().contains(buildConfig)) {
            throw new ConflictedEntryException("BuildConfiguration is already in the BuildConfigurationSet",
                    BuildConfigurationSet.class, configSetId);
        }

        buildConfigSet.addBuildConfiguration(buildConfig);
        repository.save(buildConfigSet);
    }

    public void removeConfiguration(Integer configSetId, Integer configId) throws ValidationException {
        BuildConfigurationSet buildConfigSet = repository.queryById(configSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configId);
        ValidationBuilder.validateObject(buildConfigSet, WhenUpdating.class)
                .validateCondition(buildConfigSet != null, "No build configuration set exists with id: " + configSetId)
                .validateCondition(buildConfig != null, "No build configuration exists with id: " + configId);
        buildConfigSet.removeBuildConfiguration(buildConfig);
        repository.save(buildConfigSet);
    }

    public void updateConfigurations(Integer configSetId, Collection<BuildConfigurationRest> buildConfigurationRests) throws ValidationException {
        BuildConfigurationSet buildConfigSet = repository.queryById(configSetId);

        if (buildConfigSet == null) {
            throw new InvalidEntityException("No BuildConfigurationSet exists with id: " + configSetId);
        }

        Set<BuildConfiguration> buildConfigurations = new HashSet<>();

        for (BuildConfigurationRest restModel : buildConfigurationRests) {
            BuildConfiguration dbModel = buildConfigurationRepository.queryById(restModel.getId());
            if (dbModel == null) {
                throw new InvalidEntityException("No BuildConfiguration exists with id: " + restModel.getId());
            }

            buildConfigurations.add(dbModel);
        }

        buildConfigSet.setBuildConfigurations(buildConfigurations);
        repository.save(buildConfigSet);
    }

    public CollectionInfo<BuildConfigurationSetRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql,
            String rsql, Integer productVersionId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, rsql,
                BuildConfigurationSetPredicates.withProductVersionId(productVersionId));
    }

    public CollectionInfo<BuildConfigurationSetRest> getAllForBuildConfiguration(int pageIndex, int pageSize, String sortingRsql,
            String rsql, Integer buildConfigurationId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, rsql,
                BuildConfigurationSetPredicates.withBuildConfigurationId(buildConfigurationId));
    }

    private List<BuildConfiguration> getBuildConfigurations(Integer id) {
        return buildConfigurationRepository.queryWithPredicates(
                BuildConfigurationPredicates.withBuildConfigurationSetId(id));
    }


}
