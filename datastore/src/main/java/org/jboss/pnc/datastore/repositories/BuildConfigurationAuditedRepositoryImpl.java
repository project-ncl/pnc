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

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Stateless
public class BuildConfigurationAuditedRepositoryImpl implements BuildConfigurationAuditedRepository {

    Logger logger = LoggerFactory.getLogger(BuildConfigurationAuditedRepositoryImpl.class);

    EntityManager entityManager;

    BuildRecordRepository buildRecordRepository;

    @Deprecated // CDI workaround
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

        return result.stream().map(o -> createAudited(o[0], o[1])).collect(Collectors.toList());
    }

    @Override
    public BuildConfigurationAudited findLatestById(int buildConfigurationId) {
        Object result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.id().eq(buildConfigurationId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(1)
                .getSingleResult();
        if (result == null) {
            return null;
        }

        Object[] parts = (Object[]) result;
        return createAudited(parts[0], parts[1]);
    }

    private BuildConfigurationAudited createAudited(Object entity, Object revision) {
        BuildConfiguration buildConfiguration = (BuildConfiguration) entity;
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) revision;

        // preload generic parameters
        buildConfiguration.getGenericParameters().forEach((k, v) -> k.equals(null));

        return BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, revisionEntity.getId());
    }

    @Override
    public BuildConfigurationAudited queryById(IdRev idRev) {
        logger.trace("Querying for BuildConfigurationAudited.idRev: {}.", idRev);
        BuildConfiguration buildConfiguration = AuditReaderFactory.get(entityManager)
                .find(BuildConfiguration.class, idRev.getId(), idRev.getRev());

        if (buildConfiguration == null) {
            return null;
        }

        // preload generic parameters
        buildConfiguration.getGenericParameters().forEach((k, v) -> k.equals(null));

        return BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, idRev.getRev());
    }

    @Override
    public Map<IdRev, BuildConfigurationAudited> queryById(Set<IdRev> idRevs) {
        logger.trace("Querying for BuildConfigurationAudited.idRevs: {}.", idRevs);

        List<String> idRevConcatenated = idRevs.stream()
                .map(idRev -> idRev.getId() + "-" + idRev.getRev())
                .collect(Collectors.toList());

        // WORKAROUND: as I cannot concatenate AuditEntity property to match
        // `AuditEntity.property("id")-AuditEntity.property("rev")` in idRevConcatenated list
        // I can query all BuildConfigurationAudited with the only id and later on filter id and rev
        List<Integer> bcaRevIds = idRevs.stream().map(IdRev::getRev).collect(Collectors.toList());
        // Getting all revisions of BuildConfiguration with specified list of IDs
        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.revisionNumber().in(bcaRevIds))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return result.stream().filter(res -> {
            BuildConfiguration buildConfiguration = (BuildConfiguration) res[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) res[1];
            return idRevConcatenated.contains(buildConfiguration.getId() + "-" + revisionEntity.getId());
        }).peek(res -> {
            BuildConfiguration buildConfiguration = (BuildConfiguration) res[0];
            // preload generic parameters
            buildConfiguration.getGenericParameters().forEach((k, v) -> k.equals(null));
        }).map(res -> {
            BuildConfiguration buildConfiguration = (BuildConfiguration) res[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) res[1];
            return BuildConfigurationAudited.fromBuildConfiguration(buildConfiguration, revisionEntity.getId());
        }).collect(Collectors.toMap(BuildConfigurationAudited::getIdRev, bca -> bca));
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
                cb.and(
                        cb.equal(root.get(BuildRecord_.buildConfigurationId), idRev.getId()),
                        cb.equal(root.get(BuildRecord_.buildConfigurationRev), idRev.getRev())));
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
        return result.stream().map(o -> createAudited(o[0], o[1])).collect(Collectors.toList());
    }

    @Override
    public List<IdRev> searchIdRevForBuildConfigurationName(String buildConfigurationName) {
        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.property("name").like(buildConfigurationName))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return result.stream().map(o -> {
            BuildConfiguration buildConfiguration = (BuildConfiguration) o[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) o[1];
            return new IdRev(buildConfiguration.getId(), revisionEntity.getId());
        }).collect(Collectors.toList());
    }

    @Override
    public List<IdRev> searchIdRevForBuildConfigurationNameOrProjectName(
            List<Project> projectsMatchingName,
            String name) {
        AuditDisjunction disjunction = AuditEntity.disjunction();
        projectsMatchingName.forEach(project -> disjunction.add(AuditEntity.relatedId("project").eq(project.getId())));
        disjunction.add(AuditEntity.property("name").like(name));

        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(disjunction)
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return result.stream().map(o -> {
            BuildConfiguration buildConfiguration = (BuildConfiguration) o[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) o[1];
            return new IdRev(buildConfiguration.getId(), revisionEntity.getId());
        }).collect(Collectors.toList());
    }

    @Override
    public List<IdRev> searchIdRevForProjectId(Integer projectId) {
        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .add(AuditEntity.relatedId("project").eq(projectId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return result.stream().map(o -> {
            BuildConfiguration buildConfiguration = (BuildConfiguration) o[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) o[1];
            return new IdRev(buildConfiguration.getId(), revisionEntity.getId());
        }).collect(Collectors.toList());
    }
}
