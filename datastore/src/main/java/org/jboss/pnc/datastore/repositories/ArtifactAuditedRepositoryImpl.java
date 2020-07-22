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

import java.util.List;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.ArtifactAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class ArtifactAuditedRepositoryImpl implements ArtifactAuditedRepository {

    Logger logger = LoggerFactory.getLogger(ArtifactAuditedRepositoryImpl.class);

    EntityManager entityManager;

    BuildRecordRepository buildRecordRepository;

    @Deprecated // CDI workaround
    public ArtifactAuditedRepositoryImpl() {
    }

    @Inject
    public ArtifactAuditedRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ArtifactAudited> findAllByIdOrderByRevDesc(Integer artifactId) {

        List<Object[]> result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(Artifact.class, false, false)
                .add(AuditEntity.id().eq(artifactId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        return result.stream().map(o -> createAudited(o[0], o[1])).collect(Collectors.toList());
    }

    @Override
    public ArtifactAudited findLatestById(int artifactId) {
        Object result = AuditReaderFactory.get(entityManager)
                .createQuery()
                .forRevisionsOfEntity(Artifact.class, false, false)
                .add(AuditEntity.id().eq(artifactId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .setMaxResults(1)
                .getSingleResult();
        if (result == null) {
            return null;
        }

        Object[] parts = (Object[]) result;
        return createAudited(parts[0], parts[1]);
    }

    @Override
    public ArtifactAudited queryById(IdRev idRev) {
        logger.trace("Querying for ArtifactAudited.idRev: {}.", idRev);
        Artifact artifact = AuditReaderFactory.get(entityManager).find(Artifact.class, idRev.getId(), idRev.getRev());

        if (artifact == null) {
            return null;
        }

        return ArtifactAudited.fromArtifact(artifact, idRev.getRev());
    }

    private ArtifactAudited createAudited(Object entity, Object revision) {
        Artifact artifact = (Artifact) entity;
        DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) revision;

        return ArtifactAudited.fromArtifact(artifact, revisionEntity.getId());
    }

}
