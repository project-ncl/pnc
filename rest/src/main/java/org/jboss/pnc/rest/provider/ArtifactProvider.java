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

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;

@Stateless
public class ArtifactProvider extends AbstractProvider<Artifact, ArtifactRest> {

    private BuildRecordRepository buildRecordRepository;

    public ArtifactProvider() {
    }

    @Inject
    public ArtifactProvider(ArtifactRepository artifactRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer, BuildRecordRepository buildRecordRepository) {
        super(artifactRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildRecordRepository = buildRecordRepository;
    }

    @Deprecated
    public CollectionInfo<ArtifactRest> getAllForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);

        Set<Artifact> fullArtifactList = new HashSet<>();
        fullArtifactList.addAll(buildRecord.getBuiltArtifacts());
        fullArtifactList.addAll(buildRecord.getDependencies());

        return filterAndSort(pageIndex, pageSize, sortingRsql, query,
                ArtifactRest.class, fullArtifactList,
                ArtifactRest::new);
    }

    public CollectionInfo<ArtifactRest> getBuiltArtifactsForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);

        return filterAndSort(pageIndex, pageSize, sortingRsql, query,
                ArtifactRest.class, buildRecord.getBuiltArtifacts(),
                ArtifactRest::new);
    }

    private <DTO, Model> CollectionInfo<DTO> filterAndSort(int pageIndex, int pageSize, String sortingRsql, String query,
                                                       Class<DTO> selectingClass, Set<Model> artifacts,
                                                           DtoMapper<Model, DTO> dtoSupplier) {
        Predicate<DTO> queryPredicate = rsqlPredicateProducer.getStreamPredicate(selectingClass, query);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        Stream<DTO> filteredStream = nullableStreamOf(artifacts)
                .map(dtoSupplier::map)
                .filter(queryPredicate).sorted(sortInfo.getComparator());
        List<DTO> filteredList = filteredStream.collect(Collectors.toList());

        return filteredList.stream()
                .skip(pageIndex * pageSize)
                .limit(pageSize).collect(new CollectionInfoCollector<>(pageIndex, pageSize, (filteredList.size() + pageSize -1)/pageSize));
    }

    public CollectionInfo<ArtifactRest> getDependencyArtifactsForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withDependantBuildRecordId(buildRecordId));
    }

    @Override
    protected Function<? super Artifact, ? extends ArtifactRest> toRESTModel() {
        return ArtifactRest::new;
    }

    @Override
    protected Function<? super ArtifactRest, ? extends Artifact> toDBModel() {
        throw new UnsupportedOperationException();
    }

    public interface DtoMapper<Model, DTO> {
        DTO map(Model m);
    }
}
