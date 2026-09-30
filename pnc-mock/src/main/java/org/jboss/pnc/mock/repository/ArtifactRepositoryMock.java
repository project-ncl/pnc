/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.datastore.repositories.ArtifactRepository;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:05 PM
 */
public class ArtifactRepositoryMock extends IntIdRepositoryMock<Artifact> implements ArtifactRepository {

    @Override
    public Artifact withPurl(String purl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Artifact> withIdentifierAndSha256(Collection<Artifact.IdentifierSha256> identifierSha256Set) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Artifact> withIdentifierAndSha256AndTargetRepository(
            Collection<Artifact.IdentifierSha256TargetRepository> identifierSha256TargetRepositorySet) {
        throw new UnsupportedOperationException();
    }

    public List<Artifact> withIdentifierAndSha256(String identifier, String sha256) {
        throw new UnsupportedOperationException("Unimplemented method 'withIdentifierAndSha256'");
    }

    @Override
    public List<Artifact> withSha256In(Set<String> sha256) {
        throw new UnsupportedOperationException("Unimplemented method 'withSha256In'");
    }
}
