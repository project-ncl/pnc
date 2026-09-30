/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;

/**
 * @author Jakub Bartecek
 */
@Stateless
public class RepositoryConfigurationRepositoryImpl extends AbstractRepository<RepositoryConfiguration, Integer>
        implements RepositoryConfigurationRepository {

    public RepositoryConfigurationRepositoryImpl() {
        super(RepositoryConfiguration.class, Integer.class);
    }

    @Override
    public RepositoryConfiguration queryByExactInternalScm(String internalScmRepoUrl) {
        return queryByPredicates(RepositoryConfigurationPredicates.withExactInternalScmRepoUrl(internalScmRepoUrl));
    }

    @Override
    public RepositoryConfiguration queryByInternalScm(String internalScmRepoUrl) {
        return queryByPredicates(RepositoryConfigurationPredicates.withInternalScmRepoUrl(internalScmRepoUrl));
    }

    @Override
    public RepositoryConfiguration queryByExternalScm(String externalScmRepoUrl) {
        return queryByPredicates(RepositoryConfigurationPredicates.withExternalScmRepoUrl(externalScmRepoUrl));
    }
}
