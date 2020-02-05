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
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.TargetRepository.Type;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Artifact} entity.
 */
public interface ArtifactRepository extends Repository<Artifact, Integer> {

    Set<Artifact> withIdentifierAndSha256s(Set<Artifact.IdentifierSha256> identifierSha256s);

    List<RawArtifact> getMinimizedDependencyArtifactsForBuildRecord(Integer buildRecordId, int pageSize, int offset);

    Object[] countMinimizedDependencyArtifactsForBuildRecord(Integer buildRecordId);

    public interface RawArtifact {
        Integer getId();
        Artifact.Quality getArtifactQuality();
        String getDeployPath();
        String getFilename();
        String getIdentifier();
        Date getImportDate();
        String getMd5();
        String getOriginUrl();
        String getSha1();
        String getSha256();
        Long getSize();
        Integer getTargetRepositoryId();
        Boolean getTemporaryRepo();
        String getTargetRepositoryIdentifier();
        String getRepositoryPath();
        Type getRepositoryType();
    }
}
