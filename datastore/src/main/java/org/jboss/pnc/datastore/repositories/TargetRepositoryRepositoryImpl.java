/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import java.util.List;
import java.util.Set;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.datastore.predicates.TargetRepositoryPredicates;
import org.jboss.pnc.spi.datastore.repositories.TargetRepositoryRepository;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class TargetRepositoryRepositoryImpl extends AbstractRepository<TargetRepository, Integer>
        implements TargetRepositoryRepository {

    public TargetRepositoryRepositoryImpl() {
        super(TargetRepository.class, Integer.class);
    }

    @Override
    public TargetRepository queryByIdentifierAndPath(String identifier, String repositoryPath) {
        return queryByPredicates(TargetRepositoryPredicates.byIdentifierAndPath(identifier, repositoryPath));
    }

    @Override
    public List<TargetRepository> queryByIdentifiersAndPaths(Set<TargetRepository.IdentifierPath> identifiersAndPaths) {
        return queryWithPredicates(TargetRepositoryPredicates.withIdentifierAndPathIn(identifiersAndPaths));
    }
}
