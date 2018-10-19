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

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
public class BuildConfigurationAuditedRepositoryImpl implements BuildConfigurationAuditedRepository {

    Logger logger = LoggerFactory.getLogger(BuildConfigurationAuditedRepositoryImpl.class);

    EntityManager entityManager;

    BuildRecordRepository buildRecordRepository;

    @Deprecated //CDI workaround
    public BuildConfigurationAuditedRepositoryImpl() {
    }

    @Inject
    public BuildConfigurationAuditedRepositoryImpl(
            EntityManager entityManager,
            BuildRecordRepository buildRecordRepository) {
        this.entityManager = entityManager;
        this.buildRecordRepository = buildRecordRepository;
    }

    @Override
    public List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer buildConfigurationId) {

        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.id().eq(buildConfigurationId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        List<BuildRecord> buildRecords = getBuildRecords(buildConfigurationId);

        return result.stream().map(o -> createAudited(o[0], o[1], buildRecords)).collect(Collectors.toList());
    }

    private BuildConfigurationAudited createAudited(Object entity, Object revision, List<BuildRecord> buildRecords) {
        BuildConfiguration buildConfiguration = (BuildConfiguration) entity;
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) revision;

        //preload generic parameters
        buildConfiguration.getGenericParameters().forEach((k,v) -> k.equals(null));

        return BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, revisionEntity.getId(), buildRecords);
    }

    @Override
    public BuildConfigurationAudited queryById(IdRev idRev) {
        logger.trace("Querying for BuildConfigurationAudited.idRev: {}.", idRev);
        BuildConfiguration buildConfiguration = AuditReaderFactory.get(entityManager)
                .find(BuildConfiguration.class, idRev.getId(), idRev.getRev());

        if (buildConfiguration == null) {
            return null;
        }
        List<BuildRecord> buildRecords = getBuildRecords(idRev);

        //preload generic parameters
        buildConfiguration.getGenericParameters().forEach((k,v) -> k.equals(null));

        return BuildConfigurationAudited.fromBuildConfiguration(
                buildConfiguration,
                idRev.getRev(),
                buildRecords
        );
    }

    /**
     * @param idRev
     * @return List of BuildRecords where only id is fetched
     */
    private List<BuildRecord> getBuildRecords(IdRev idRev) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<BuildRecord> root = query.from(BuildRecord.class);
        query.select(root.get(BuildRecord_.id));
        query.where(
            cb.and( cb.equal(root.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                    cb.equal(root.get(BuildRecord_.buildConfigurationRev), idRev.getRev()))
        );
        List<Integer> buildRecordIds = entityManager.createQuery(query).getResultList();
        return buildRecordIds.stream()
                .map(id -> BuildRecord.Builder.newBuilder().id(id).build())
                .collect(Collectors.toList());
    }

    /**
     * @param buildConfigurationId
     * @return List of BuildRecords where only id is fetched
     */
    private List<BuildRecord> getBuildRecords(Integer buildConfigurationId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Integer> query = cb.createQuery(Integer.class);
        Root<BuildRecord> root = query.from(BuildRecord.class);
        query.select(root.get(BuildRecord_.id));
        query.where(cb.equal(root.get(BuildRecord_.buildConfigurationId), buildConfigurationId));
        List<Integer> buildRecordIds = entityManager.createQuery(query).getResultList();
        return buildRecordIds.stream()
                .map(id -> BuildRecord.Builder.newBuilder().id(id).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildConfigurationAudited> searchForBuildConfigurationName(String buildConfigurationName) {
        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.property("name").like(buildConfigurationName))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();
        List<BuildRecord> emptyList = Collections.EMPTY_LIST;
        return result.stream().map(o -> createAudited(o[0], o[1], emptyList)).collect(Collectors.toList());
    }
}
