/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager.model;

import java.util.Map;

public interface RepositoryConnectionInfo {

    String getDependencyUrl();

    String getToolchainUrl();

    String getDeployUrl();

    Map<String, String> getProperties();

}
