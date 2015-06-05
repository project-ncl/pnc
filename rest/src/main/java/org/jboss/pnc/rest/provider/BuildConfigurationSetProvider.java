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

import com.google.common.base.Preconditions;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildConfigurationSetPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdInSet;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationSetProvider {

    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    public BuildConfigurationSetProvider() {
    }

    @Inject
    public BuildConfigurationSetProvider(BuildConfigurationSetRepository buildConfigurationSetRepository,
            BuildRecordRepository buildRecordRepository) {
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.buildRecordRepository = buildRecordRepository;
    }

    public Function<? super BuildConfigurationSet, ? extends BuildConfigurationSetRest> toRestModel() {
        return buildConfigurationSet -> {
            if (buildConfigurationSet != null) {
                return new BuildConfigurationSetRest(buildConfigurationSet);
            }
            return null;
        };
    }

    public String getDefaultSortingField() {
        return BuildConfigurationSet.DEFAULT_SORTING_FIELD;
    }

    public List<BuildConfigurationSetRest> getAll() {
        return buildConfigurationSetRepository.findAll().stream().map(buildConfigurationSet -> new BuildConfigurationSetRest(buildConfigurationSet))
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationSetRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildConfigurationSetRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getBuildRecords(Integer buildConfigurationSetId,
            int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        BuildConfigurationSetRest buildConfigSetRest = getSpecific(buildConfigurationSetId);
        return nullableStreamOf(buildRecordRepository.findAll(withBuildConfigurationIdInSet(buildConfigSetRest.getBuildConfigurationIds()).and(filteringCriteria.get()), paging))
                .map(buildRecordToRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigurationSetRest getSpecific(Integer id) {
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(id);
        if (buildConfigurationSet != null) {
            return new BuildConfigurationSetRest(buildConfigurationSet);
        }
        return null;
    }

    public Integer store(BuildConfigurationSetRest buildConfigurationSetRest) {
        Preconditions.checkArgument(buildConfigurationSetRest.getId() == null, "Id must be null");
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRest.toBuildConfigurationSet();
        buildConfigurationSet = buildConfigurationSetRepository.save(buildConfigurationSet);
        return buildConfigurationSet.getId();
    }

    public Integer update(Integer id, BuildConfigurationSetRest buildConfigurationSetRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(buildConfigurationSetRest.getId() == null || buildConfigurationSetRest.getId().equals(id),
                "Entity id does not match the id to update");
        buildConfigurationSetRest.setId(id);
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(buildConfigurationSetRest.getId());
        Preconditions.checkArgument(buildConfigurationSet != null, "Couldn't find buildConfigurationSet with id "
                + buildConfigurationSetRest.getId());
        buildConfigurationSet = buildConfigurationSetRepository.save(buildConfigurationSetRest.toBuildConfigurationSet(buildConfigurationSet));
        return buildConfigurationSet.getId();
    }

    public void delete(Integer configurationSetId) {
        buildConfigurationSetRepository.delete(configurationSetId);
    }

    private Function<BuildConfiguration, BuildConfigurationRest> buildConfigToRestModel() {
        return buildConfig -> new BuildConfigurationRest(buildConfig);
    }

    private Function<BuildRecord, BuildRecordRest> buildRecordToRestModel() {
        return buildRecord -> new BuildRecordRest(buildRecord);
    }

    public List<BuildConfigurationRest> getBuildConfigurations(Integer configurationSetId) {
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.findOne(withBuildConfigurationSetId(configurationSetId));
        Set<BuildConfiguration> buildConfigs = buildConfigSet.getBuildConfigurations();
        return nullableStreamOf(buildConfigs)
                .map(buildConfigToRestModel())
                .collect(Collectors.toList());
    }

    public Response addConfiguration(Integer configurationSetId, Integer configurationId) {
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.findOne(configurationSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.findOne(configurationId);
        if (buildConfigSet.getBuildConfigurations().contains(buildConfig)){
            return Response.status(Response.Status.CONFLICT).build();
        }
        buildConfigSet.addBuildConfiguration(buildConfig);
        buildConfigurationSetRepository.save(buildConfigSet);
        return Response.ok().build();
    }

    public void removeConfiguration(Integer configurationSetId, Integer configurationId) {
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.findOne(configurationSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.findOne(configurationId);
        buildConfigSet.removeBuildConfiguration(buildConfig);
        buildConfigurationSetRepository.save(buildConfigSet);
    }


}
