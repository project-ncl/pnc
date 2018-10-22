/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Stateless
public class BuildRecordRepositoryImpl extends AbstractRepository<BuildRecord, Integer> implements BuildRecordRepository {

    private static final Logger logger = LoggerFactory.getLogger(BuildRecordRepositoryImpl.class);

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
        if (buildRecord == null) {
            return null;
        }

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
    public GraphWithMetadata<BuildRecord, Integer> getDependencyGraph(Integer buildRecordId) {
        GraphBuilder graphBuilder = new GraphBuilder();

        Graph<BuildRecord> graph = new Graph<>();
        logger.debug("Building dependency graph for buildRecordId: {}.", buildRecordId);
        Vertex<BuildRecord> current = graphBuilder.buildDependencyGraph(graph, buildRecordId);
        logger.trace("Dependency graph of buildRecord.id {} {}; Graph edges: {}.", buildRecordId, graph, graph.getEdges());

        if (current != null) {
            BuildRecord buildRecord = current.getData();
            graphBuilder.buildDependentGraph(graph, buildRecord.getId());
        }
        logger.trace("Graph with dependents of buildRecord.id {} {}; Graph edges: {}.", buildRecordId, graph, graph.getEdges());

        return new GraphWithMetadata(graph, graphBuilder.getMissingBuildRecords());
    }

    private class GraphBuilder {

        private List<Integer> missingBuildRecords = new ArrayList<>();


        Vertex<BuildRecord> buildDependencyGraph(Graph<BuildRecord> graph, Integer buildRecordId) {
            BuildRecord buildRecord = findByIdFetchProperties(buildRecordId);
            if (buildRecord != null) {
                Vertex<BuildRecord> buildRecordVertex = getVisited(buildRecordId, graph);
                if (buildRecordVertex == null) {
                    buildRecordVertex = new NameUniqueVertex<>(Integer.toString(buildRecordId), buildRecord);
                    graph.addVertex(buildRecordVertex);
                }
                for (Integer dependencyBuildRecordId : buildRecord.getDependencyBuildRecordIds()) {
                    Vertex<BuildRecord> dependency = getVisited(dependencyBuildRecordId, graph);
                    if (dependency == null) {
                        dependency = buildDependencyGraph(graph, dependencyBuildRecordId);
                    }
                    if (dependency != null) {
                        logger.trace("Creating new dependency edge from {} to {}.", buildRecordVertex, dependency);
                        graph.addEdge(buildRecordVertex, dependency, 1);
                    }
                }
                return buildRecordVertex;
            } else {
                logger.debug("Cannot find buildRecord with id {}.", buildRecordId   );
                missingBuildRecords.add(buildRecordId);
                return null;
            }
        }

        Vertex<BuildRecord> buildDependentGraph(Graph<BuildRecord> graph, Integer buildRecordId) {
            BuildRecord buildRecord = findByIdFetchProperties(buildRecordId);
            if (buildRecord != null) {
                Vertex<BuildRecord> buildRecordVertex = getVisited(buildRecordId, graph);
                if (buildRecordVertex == null) {
                    buildRecordVertex = new NameUniqueVertex<>(Integer.toString(buildRecordId), buildRecord);
                    graph.addVertex(buildRecordVertex);
                }
                for (Integer dependentBuildRecordId : buildRecord.getDependentBuildRecordIds()) {
                    Vertex<BuildRecord> dependent = getVisited(dependentBuildRecordId, graph);
                    if (dependent == null) {
                        dependent = buildDependentGraph(graph, dependentBuildRecordId);
                    }
                    if (dependent != null) {
                        logger.trace("Creating new dependent edge from {} to {}.", dependent, buildRecordVertex);
                        graph.addEdge(dependent, buildRecordVertex, 1);
                    }
                }
                return buildRecordVertex;
            } else {
                missingBuildRecords.add(buildRecordId);
                return null;
            }
        }

        private Vertex<BuildRecord> getVisited(Integer buildRecordId, Graph<BuildRecord> graph) {
            return graph.findVertexByName(Integer.toString(buildRecordId));
        }

        public List<Integer> getMissingBuildRecords() {
            return missingBuildRecords;
        }
    }

}
