/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.RepositoryConfiguration} entity.
 *
 * @author Jakub Bartecek
 */
public interface RepositoryConfigurationRepository extends Repository<RepositoryConfiguration, Integer> {

    RepositoryConfiguration queryByExactInternalScm(String internalScmRepoUrl);

    RepositoryConfiguration queryByInternalScm(String internalScmRepoUrl);

    RepositoryConfiguration queryByExternalScm(String externalScmRepoUrl);
}
