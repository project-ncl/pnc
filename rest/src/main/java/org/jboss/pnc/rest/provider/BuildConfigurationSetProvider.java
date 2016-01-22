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
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.validation.exceptions.ConflictedEntryException;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.withName;

@Stateless
public class BuildConfigurationSetProvider extends AbstractProvider<BuildConfigurationSet, BuildConfigurationSetRest> {

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

    @Override
    protected void validateBeforeSaving(BuildConfigurationSetRest buildConfigurationSetRest) throws ValidationException {
        BuildConfigurationSet buildConfigurationSetFromDB = repository
                .queryByPredicates(withName(buildConfigurationSetRest.getName()));
        if (buildConfigurationSetFromDB != null) {
            throw new ConflictedEntryException("BuildConfiguration with this name already exists", BuildConfigurationSet.class,
                    buildConfigurationSetFromDB.getId());
        }
    }

    @Override
    protected Function<? super BuildConfigurationSet, ? extends BuildConfigurationSetRest> toRESTModel() {
        return buildConfigurationSet -> new BuildConfigurationSetRest(buildConfigurationSet);
    }

    @Override
    protected Function<? super BuildConfigurationSetRest, ? extends BuildConfigurationSet> toDBModelModel() {
        return buildConfigurationSet -> {
            if(buildConfigurationSet.getId() != null) {
                BuildConfigurationSet buildConfigurationSetFromDB = repository.queryById(buildConfigurationSet.getId());
                return buildConfigurationSet.toBuildConfigurationSet(buildConfigurationSetFromDB);
            }
            return buildConfigurationSet.toBuildConfigurationSet();
        };
    }

    public void addConfiguration(Integer configurationSetId, Integer configurationId) throws ConflictedEntryException {
        BuildConfigurationSet buildConfigSet = repository.queryById(configurationSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configurationId);
        if (buildConfigSet.getBuildConfigurations().contains(buildConfig)) {
            throw new ConflictedEntryException("BuildConfiguration is already in the BuildConfigurationSet",
                    BuildConfigurationSet.class, configurationSetId);
        }

        buildConfigSet.addBuildConfiguration(buildConfig);
        repository.save(buildConfigSet);
    }

    public void removeConfiguration(Integer configurationSetId, Integer configurationId) {
        BuildConfigurationSet buildConfigSet = repository.queryById(configurationSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configurationId);
        buildConfigSet.removeBuildConfiguration(buildConfig);
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

}
