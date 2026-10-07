/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.Artifact} entity.
 */
public interface ArtifactRepository extends Repository<Artifact, Integer> {

    Artifact withPurl(String purl);

    Set<Artifact> withIdentifierAndSha256(Collection<Artifact.IdentifierSha256> identifierSha256Set);

    Set<Artifact> withIdentifierAndSha256AndTargetRepository(
            Collection<Artifact.IdentifierSha256TargetRepository> identifierSha256TargetRepositorySet);

    List<Artifact> withIdentifierAndSha256(String identifier, String sha256);

    List<Artifact> withSha256In(Set<String> sha256);

}
