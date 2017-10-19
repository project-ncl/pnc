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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.MavenRepoDriverModuleConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactRepo;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.provider.collection.CollectionInfoCollector;
import org.jboss.pnc.rest.restmodel.ArtifactRest;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;

@Stateless
public class ArtifactProvider extends AbstractProvider<Artifact, ArtifactRest> {

    private static final Logger logger = LoggerFactory.getLogger(ArtifactProvider.class);

    /**
     * Used only in a deprecated method
     */
    @Deprecated
    private BuildRecordRepository buildRecordRepository;
    private MavenRepoDriverModuleConfig moduleConfig;

    public ArtifactProvider() {
    }

    @Inject
    public ArtifactProvider(ArtifactRepository artifactRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer, BuildRecordRepository buildRecordRepository,
            Configuration configuration) {
        super(artifactRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
        this.buildRecordRepository = buildRecordRepository;

        try {
            moduleConfig = configuration.getModuleConfig(new PncConfigProvider<>(MavenRepoDriverModuleConfig.class));
        } catch (ConfigurationParseException e) {
            logger.error("Cannot read configuration", e);
        }
    }

    /**
     * @deprecated is uses in-memory sort and order
     */
    @Deprecated
    public CollectionInfo<ArtifactRest> getAllForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        BuildRecord buildRecord = buildRecordRepository.queryById(buildRecordId);

        Set<Artifact> fullArtifactList = new HashSet<>();
        fullArtifactList.addAll(buildRecord.getBuiltArtifacts());
        fullArtifactList.addAll(buildRecord.getDependencies());

        return filterAndSort(pageIndex, pageSize, sortingRsql, query,
                ArtifactRest.class, fullArtifactList);
    }

    /**
     * Lookups built artifacts for the specified BuildRecord
     * 
     * @return Returns requested artifacts or empty collection if BuildRecord with the specified ID doesn't exists
     */
    public CollectionInfo<ArtifactRest> getBuiltArtifactsForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withBuildRecordId(buildRecordId));
    }

    @Deprecated
    private <DTO, Model> CollectionInfo<ArtifactRest> filterAndSort(int pageIndex, int pageSize, String sortingRsql, String query,
                                                       Class<ArtifactRest> selectingClass, Set<Artifact> artifacts) {
        Predicate<ArtifactRest> queryPredicate = rsqlPredicateProducer.getStreamPredicate(selectingClass, query);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        Stream<ArtifactRest> filteredStream = nullableStreamOf(artifacts)
                .map(artifact -> new ArtifactRest(artifact, getDeployUrl(artifact), getPublicUrl(artifact)))
                .filter(queryPredicate).sorted(sortInfo.getComparator());
        List<ArtifactRest> filteredList = filteredStream.collect(Collectors.toList());

        return filteredList.stream()
                .skip(pageIndex * pageSize)
                .limit(pageSize).collect(new CollectionInfoCollector<>(pageIndex, pageSize, (filteredList.size() + pageSize -1)/pageSize));
    }

    private String getDeployUrl(Artifact artifact) {
        if (artifact.getRepoType().equals(ArtifactRepo.Type.MAVEN)) {
            if (artifact.getDeployPath() == null || artifact.getDeployPath().equals("")) {
                return "";
            } else {
                return StringUtils.addEndingSlash(moduleConfig.getInternalRepositoryMvnPath()) + StringUtils.stripTrailingSlash(artifact.getDeployPath());
            }
        } else {
            return artifact.getOriginUrl();
        }
    }

    private String getPublicUrl(Artifact artifact) {
        if (artifact.getRepoType().equals(ArtifactRepo.Type.MAVEN)) {
            if (artifact.getDeployPath() == null || artifact.getDeployPath().equals("")) {
                return "";
            } else {
                return StringUtils.addEndingSlash(moduleConfig.getExternalRepositoryMvnPath()) + StringUtils.stripTrailingSlash(artifact.getDeployPath());
            }
        } else {
            return artifact.getOriginUrl();
        }
    }

    public CollectionInfo<ArtifactRest> getDependencyArtifactsForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            int buildRecordId) {
        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withDependantBuildRecordId(buildRecordId));
    }

    @Override
    public ArtifactRest getSpecific(Integer id) {
        Artifact artifact = repository.queryById(id);
        if (artifact != null) {
            return new ArtifactRest(artifact, getDeployUrl(artifact), getPublicUrl(artifact));
        }
        return null;
    }

    public Integer store(ArtifactRest restEntity) throws ValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    public void update(Integer id, ArtifactRest restEntity) throws ValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    public void delete(Integer id) throws ValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    @Override
    protected Function<? super Artifact, ? extends ArtifactRest> toRESTModel() {
        return (artifact) -> new ArtifactRest(artifact, getDeployUrl(artifact), getPublicUrl(artifact));
    }

    @Override
    protected Function<? super ArtifactRest, ? extends Artifact> toDBModel() {
        throw new UnsupportedOperationException();
    }
}
