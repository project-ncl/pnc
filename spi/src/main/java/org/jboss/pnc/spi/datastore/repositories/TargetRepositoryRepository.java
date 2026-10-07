/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.List;
import java.util.Set;

import org.jboss.pnc.model.TargetRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface TargetRepositoryRepository extends Repository<TargetRepository, Integer> {

    TargetRepository queryByIdentifierAndPath(String identifier, String repositoryPath);

    List<TargetRepository> queryByIdentifiersAndPaths(Set<TargetRepository.IdentifierPath> identifiersAndPaths);
}
