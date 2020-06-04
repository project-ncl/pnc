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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.ArtifactSpringRepository;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.predicates.ArtifactPredicates;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;

@Stateless
public class ArtifactRepositoryImpl extends AbstractRepository<Artifact, Integer> implements ArtifactRepository {

    @Inject
    public ArtifactRepositoryImpl(ArtifactSpringRepository springArtifactRepository) {
        super(springArtifactRepository, springArtifactRepository);
    }

    @Override
    public Set<Artifact> withIdentifierAndSha256s(Set<Artifact.IdentifierSha256> identifierSha256s) {
        Set<String> sha256s = identifierSha256s.stream().map(is -> is.getSha256()).collect(Collectors.toSet());

        List<Artifact> artifacts = queryWithPredicates(ArtifactPredicates.withSha256In(sha256s));

        // make sure the identifier matches too
        Set<Artifact> artifactsMatchingIdentifier = artifacts.stream()
                .filter(
                        a -> identifierSha256s
                                .contains(new Artifact.IdentifierSha256(a.getIdentifier(), a.getSha256())))
                .collect(Collectors.toSet());

        return artifactsMatchingIdentifier;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public Artifact save(Artifact artifact) {

        // If doing an update of the artifact
        if (artifact.getId() != null) {
            Artifact persisted = queryById(artifact.getId());
            if (persisted != null) {
                if (equalAuditedValues(persisted, artifact)) {
                    // No changes to audit, reset the modificationUser and modificationTime to previous existing
                    artifact.setModificationUser(persisted.getModificationUser());
                    artifact.setModificationTime(persisted.getModificationTime());
                }
            }
        }
        return springRepository.save(artifact);
    }

    private boolean equalAuditedValues(Artifact persisted, Artifact toUpdate) {
        return Objects.equals(persisted.getArtifactQuality(), toUpdate.getArtifactQuality())
                && Objects.equals(persisted.getReason(), toUpdate.getReason());
    }
}
