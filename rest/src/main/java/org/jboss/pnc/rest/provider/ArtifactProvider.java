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
import org.jboss.pnc.model.BuiltArtifact;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

        List<Artifact> fullArtifactList = new ArrayList<Artifact>();
        for (Artifact artifact : buildRecord.getBuiltArtifacts()) {
            fullArtifactList.add(artifact);
        }
        for (Artifact artifact : buildRecord.getDependencies()) {
            fullArtifactList.add(artifact);
        }

        return nullableStreamOf(fullArtifactList).map(artifact -> new ArtifactRest(artifact)).skip(pageIndex * pageSize)
                .limit(pageSize).collect(new CollectionInfoCollector<>(pageIndex, pageSize, fullArtifactList.size()));
    }

    public CollectionInfo<ArtifactRest> getBuiltArtifactsForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);
        return nullableStreamOf(buildRecord.getBuiltArtifacts()).map(artifact -> new ArtifactRest(artifact)).skip(pageIndex * pageSize)
                .limit(pageSize).collect(new CollectionInfoCollector<>(pageIndex, pageSize, (buildRecord.getBuiltArtifacts().size() + pageSize -1)/pageSize));
    }

    public CollectionInfo<ArtifactRest> getDependencyArtifactsForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withDependantBuildRecordId(buildRecordId));
    }

    @Override
    protected Function<? super Artifact, ? extends ArtifactRest> toRESTModel() {
        return artifact -> new ArtifactRest(artifact);
    }

    @Override
    protected Function<? super ArtifactRest, ? extends Artifact> toDBModel() {
        throw new UnsupportedOperationException();
    }
}
