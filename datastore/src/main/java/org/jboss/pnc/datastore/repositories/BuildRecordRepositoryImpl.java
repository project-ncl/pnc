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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.common.graph.NameUniqueVertex;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildRecordSpringRepository;
import org.jboss.pnc.datastore.repositories.internal.PageableMapper;
import org.jboss.pnc.datastore.repositories.internal.SpecificationsMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Stateless
public class BuildRecordRepositoryImpl extends AbstractRepository<BuildRecord, Integer> implements BuildRecordRepository {

    private BuildRecordSpringRepository repository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildRecordRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildRecordRepositoryImpl(
            BuildRecordSpringRepository buildRecordSpringRepository,
            BuildConfigurationAuditedRepository buildConfigurationAuditedRepository) {
        super(buildRecordSpringRepository, buildRecordSpringRepository);
        this.repository = buildRecordSpringRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
    }

    @Override
    public BuildRecord findByIdFetchAllProperties(Integer id) {
        return repository.findByIdFetchAllProperties(id);
    }

    @Override
    public BuildRecord findByIdFetchProperties(Integer id) {
        BuildRecord buildRecord = repository.findByIdFetchProperties(id);

        Integer revision = buildRecord.getBuildConfigurationRev();
        BuildConfigurationAudited buildConfigurationAudited =
                buildConfigurationAuditedRepository.queryById(new IdRev(buildRecord.getBuildConfigurationId(), revision));
        buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
        return buildRecord;
    }


    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildRecord>... predicates) {
        return repository.findAll(SpecificationsMapper.map(predicates), PageableMapper.mapCursored(pageInfo, sortInfo)).getContent();
    }

    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, List<Predicate<BuildRecord>> andPredicates,
                                                            List<Predicate<BuildRecord>> orPredicates) {
        return repository.findAll(SpecificationsMapper.map(andPredicates, orPredicates), PageableMapper.mapCursored(pageInfo, sortInfo)).getContent();
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId) {
        List<BuildRecord> buildRecords = queryWithPredicates(BuildRecordPredicates.withBuildConfigurationId(configurationId));

        return getLatestSuccessfulBuildRecord(buildRecords);
    }

    @Override
    public List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId) {
        return queryWithPredicates(BuildRecordPredicates.withBuildConfigurationId(configurationId));
    }

    @Override
    public List<BuildRecord> findTemporaryBuildsOlderThan(Date date) {
        return queryWithPredicates(
                BuildRecordPredicates.temporaryBuild(),
                BuildRecordPredicates.buildFinishedBefore(date));
    }

    @Override
    public Graph<BuildRecord> getDependencyGraph(Integer buildRecordId) {
        Graph<BuildRecord> graph = new Graph<>();
        Vertex<BuildRecord> current = buildDependencyGraph(graph, buildRecordId);

        BuildRecord buildRecord = repository.findOne(buildRecordId);
        for (Integer dependentBuildRecordId : buildRecord.getDependentBuildRecordIds()) {
            Vertex<BuildRecord> dependentRecord = buildDependentGraph(graph, dependentBuildRecordId);
            graph.addEdge(dependentRecord, current, 1);
        }
        return graph;
    }

    Vertex<BuildRecord> buildDependencyGraph(Graph<BuildRecord> graph, Integer buildRecordId) {
        BuildRecord buildRecord = repository.findOne(buildRecordId);
        Vertex<BuildRecord> buildRecordVertex = new NameUniqueVertex<>(Integer.toString(buildRecord.getId()), buildRecord);
        graph.addVertex(buildRecordVertex);
        for (Integer dependencyBuildRecordId : buildRecord.getDependencyBuildRecordIds()) {
            Vertex<BuildRecord> dependency = buildDependencyGraph(graph, dependencyBuildRecordId);
            graph.addEdge(buildRecordVertex, dependency, 1);
        }
        return buildRecordVertex;
    }

    private Vertex<BuildRecord> buildDependentGraph(Graph<BuildRecord> graph, Integer buildRecordId) {
        BuildRecord buildRecord = repository.findOne(buildRecordId);
        Vertex<BuildRecord> buildRecordVertex = new NameUniqueVertex<>(Integer.toString(buildRecord.getId()), buildRecord);
        graph.addVertex(buildRecordVertex);
        for (Integer dependentBuildRecordId : buildRecord.getDependentBuildRecordIds()) {
            Vertex<BuildRecord> dependent = buildDependentGraph(graph, dependentBuildRecordId);
            graph.addEdge(buildRecordVertex, dependent, 1);
        }
        return buildRecordVertex;
    }
}
