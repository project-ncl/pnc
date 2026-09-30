/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.model;

import org.jboss.pnc.model.RepositoryConfiguration;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryConfigurationMock {

    public static RepositoryConfiguration newTestRepository() {
        RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
        repositoryConfiguration.setId(1645886423);
        repositoryConfiguration.setExternalUrl("externalUrl");
        repositoryConfiguration.setInternalUrl("internalUrl");
        repositoryConfiguration.setPreBuildSyncEnabled(true);
        return repositoryConfiguration;
    }
}
