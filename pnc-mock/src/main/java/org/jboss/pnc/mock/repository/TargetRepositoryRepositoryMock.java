/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TargetRepositoryRepositoryMock extends IntIdRepositoryMock<TargetRepository>
        implements TargetRepositoryRepository {

    @Override
    public TargetRepository queryByIdentifierAndPath(String identifier, String repositoryPath) {
        return data.stream()
                .filter(tr -> tr.getIdentifier().equals(identifier) && tr.getRepositoryPath().equals(repositoryPath))
                .findAny()
                .orElse(null);
    }

    @Override
    public List<TargetRepository> queryByIdentifiersAndPaths(Set<TargetRepository.IdentifierPath> identifiersAndPaths) {
        return data.stream()
                .filter(tr -> identifiersAndPaths.contains(tr.getIdentifierPath()))
                .collect(Collectors.toList());
    }

}
