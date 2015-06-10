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
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.datastore.repositories.*;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdInSet;

@Stateless
public class BuildConfigurationSetProvider {

    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    private BuildRecordRepository buildRecordRepository;
    @Inject
    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    private BuildConfigurationRepository buildConfigurationRepository;

    public BuildConfigurationSetProvider() {
    }

    @Inject
    public BuildConfigurationSetProvider(BuildConfigurationSetRepository buildConfigurationSetRepository,
            BuildConfigurationRepository buildConfigurationRepository, BuildRecordRepository buildRecordRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildRecordRepository = buildRecordRepository;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public Function<? super BuildConfigurationSet, ? extends BuildConfigurationSetRest> toRestModel() {
        return buildConfigurationSet -> {
            if (buildConfigurationSet != null) {
                return new BuildConfigurationSetRest(buildConfigurationSet);
            }
            return null;
        };
    }

    public List<BuildConfigurationSetRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<BuildConfigurationSet> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfigurationSet.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildConfigurationSetRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordRest> getBuildRecords(Integer buildConfigurationSetId,
            int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<BuildRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        BuildConfigurationSetRest buildConfigSetRest = getSpecific(buildConfigurationSetId);
        return nullableStreamOf(buildRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate,
                withBuildConfigurationIdInSet(buildConfigSetRest.getBuildConfigurationIds())))
                .map(buildRecordToRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigurationSetRest getSpecific(Integer id) {
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(id);
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
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(
                buildConfigurationSetRest.getId());
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
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.queryByPredicates(
                withBuildConfigurationSetId(configurationSetId));
        Set<BuildConfiguration> buildConfigs = buildConfigSet.getBuildConfigurations();
        return nullableStreamOf(buildConfigs)
                .map(buildConfigToRestModel())
                .collect(Collectors.toList());
    }

    public void addConfiguration(Integer configurationSetId, Integer configurationId) {
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.queryById(configurationSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configurationId);
        if (buildConfigSet.getBuildConfigurations().contains(buildConfig))
            throw new ConflictedEntryException("BuildConfiguration is already in the BuildConfigurationSet");

        buildConfigSet.addBuildConfiguration(buildConfig);
        buildConfigurationSetRepository.save(buildConfigSet);
    }

    public void removeConfiguration(Integer configurationSetId, Integer configurationId) {
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.queryById(configurationSetId);
        BuildConfiguration buildConfig = buildConfigurationRepository.queryById(configurationId);
        buildConfigSet.removeBuildConfiguration(buildConfig);
        buildConfigurationSetRepository.save(buildConfigSet);
    }


}
