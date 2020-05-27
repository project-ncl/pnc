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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.mapper.api.ArtifactMapper;
import org.jboss.pnc.facade.providers.api.ArtifactProvider;
import org.jboss.pnc.facade.validation.DTOValidationException;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Optional;

import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withDependantBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withMd5;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha1;
import static org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates.withSha256;

/**
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@PermitAll
@Stateless
public class ArtifactProviderImpl extends AbstractProvider<Integer, Artifact, org.jboss.pnc.dto.Artifact, ArtifactRef>
        implements ArtifactProvider {

    private static Logger logger = LoggerFactory.getLogger(ArtifactProviderImpl.class);

    private BuildRecordRepository buildRecordRepository;

    private final TargetRepositoryRepository targetRepositoryRepository;

    @Inject
    public ArtifactProviderImpl(
            ArtifactRepository repository,
            ArtifactMapper mapper,
            BuildRecordRepository buildRecordRepository,
            TargetRepositoryRepository targetRepositoryRepository) {
        super(repository, mapper, Artifact.class);
        this.buildRecordRepository = buildRecordRepository;
        this.targetRepositoryRepository = targetRepositoryRepository;
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getAll(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            Optional<String> sha256,
            Optional<String> md5,
            Optional<String> sha1) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withSha256(sha256),
                withMd5(md5),
                withSha1(sha1));
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public org.jboss.pnc.dto.Artifact store(org.jboss.pnc.dto.Artifact restEntity) throws DTOValidationException {
        return super.store(restEntity);
    }

    @Override
    @RolesAllowed(SYSTEM_USER)
    public org.jboss.pnc.dto.Artifact update(String id, org.jboss.pnc.dto.Artifact restEntity)
            throws DTOValidationException {
        return super.update(id, restEntity);
    }

    @Override
    @DenyAll
    public void delete(String id) throws DTOValidationException {
        throw new UnsupportedOperationException("Direct artifact manipulation is not available.");
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getBuiltArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {

        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }

    @Override
    public Page<org.jboss.pnc.dto.Artifact> getDependantArtifactsForBuild(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String buildId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                query,
                withDependantBuildRecordId(BuildMapper.idMapper.toEntity(buildId)));
    }
}
