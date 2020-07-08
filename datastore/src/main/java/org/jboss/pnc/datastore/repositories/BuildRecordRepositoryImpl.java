/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.graph.GraphBuilder;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildRecordSpringRepository;
import org.jboss.pnc.datastore.repositories.internal.PageableMapper;
import org.jboss.pnc.datastore.repositories.internal.SpecificationsMapper;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.GraphWithMetadata;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultSortInfo;
import org.jboss.util.graph.Graph;
import org.jboss.util.graph.Vertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.buildFinishedBefore;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.includeTemporary;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.temporaryBuild;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withBuildConfigurationIdRev;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withSuccess;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withoutImplicitDependants;

@Stateless
public class BuildRecordRepositoryImpl extends AbstractRepository<BuildRecord, Integer>
        implements BuildRecordRepository {

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
        BuildConfigurationAudited buildConfigurationAudited = buildConfigurationAuditedRepository
                .queryById(new IdRev(buildRecord.getBuildConfigurationId(), revision));
        buildRecord.setBuildConfigurationAudited(buildConfigurationAudited);
        return buildRecord;
    }

    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(
            PageInfo pageInfo,
            SortInfo sortInfo,
            Predicate<BuildRecord>... predicates) {
        return repository.findAll(SpecificationsMapper.map(predicates), PageableMapper.mapCursored(pageInfo, sortInfo))
                .getContent();
    }

    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(
            PageInfo pageInfo,
            SortInfo sortInfo,
            List<Predicate<BuildRecord>> andPredicates,
            List<Predicate<BuildRecord>> orPredicates) {
        return repository
                .findAll(
                        SpecificationsMapper.map(andPredicates, orPredicates),
                        PageableMapper.mapCursored(pageInfo, sortInfo))
                .getContent();
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(Integer configurationId, boolean temporaryBuild) {
        List<BuildRecord> buildRecords = queryWithBuildConfigurationId(configurationId);
        return getLatestSuccessfulBuildRecord(buildRecords, temporaryBuild);
    }

    @Override
    public List<BuildRecord> queryWithBuildConfigurationId(Integer configurationId) {
        return queryWithPredicates(withBuildConfigurationId(configurationId));
    }

    @Override
    public List<BuildRecord> findIndependentTemporaryBuildsOlderThan(Date date) {
        return queryWithPredicates(temporaryBuild(), buildFinishedBefore(date), withoutImplicitDependants());
    }

    @Override
    public GraphWithMetadata<BuildRecord, Integer> getDependencyGraph(Integer buildRecordId) {
        GraphBuilder graphBuilder = new GraphBuilder<BuildRecord>(
                id -> Optional.ofNullable(findByIdFetchProperties(id)),
                buildRecord -> Arrays.asList(buildRecord.getDependencyBuildRecordIds()),
                buildRecord -> Arrays.asList(buildRecord.getDependentBuildRecordIds()));

        Graph<BuildRecord> graph = new Graph<>();
        logger.debug("Building dependency graph for buildRecordId: {}.", buildRecordId);
        Vertex<BuildRecord> current = graphBuilder.buildDependencyGraph(graph, buildRecordId);
        logger.trace(
                "Dependency graph of buildRecord.id {} {}; Graph edges: {}.",
                buildRecordId,
                graph,
                graph.getEdges());

        // if it is stored in the DB, add dependent nodes
        if (current != null) {
            BuildRecord buildRecord = current.getData();
            graphBuilder.buildDependentGraph(graph, buildRecord.getId());
        }
        logger.trace(
                "Graph with dependents of buildRecord.id {} {}; Graph edges: {}.",
                buildRecordId,
                graph,
                graph.getEdges());

        return new GraphWithMetadata(graph, graphBuilder.getMissingNodes());
    }

    @Override
    public BuildRecord getLatestSuccessfulBuildRecord(IdRev idRev, boolean temporaryBuild) {
        PageInfo pageInfo = new DefaultPageInfo(0, 1);
        SortInfo sortInfo = new DefaultSortInfo(SortInfo.SortingDirection.DESC, BuildRecord_.id.getName());

        List<BuildRecord> buildRecords = queryWithPredicates(
                pageInfo,
                sortInfo,
                withBuildConfigurationIdRev(idRev),
                withSuccess(),
                includeTemporary(idRev, temporaryBuild));

        if (buildRecords.size() == 0) {
            return null;
        } else {
            return buildRecords.get(0);
        }
    }

    @Override
    public Set<BuildRecord> findByBuiltArtifacts(Set<Integer> artifactsId) {
        return repository.findByBuiltArtifacts(artifactsId);
    }

}
