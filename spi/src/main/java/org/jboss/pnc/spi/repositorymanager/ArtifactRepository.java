/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager;

public interface ArtifactRepository {

    String getId();

    String getName();

    String getUrl();

    Boolean getReleases();

    Boolean getSnapshots();

}
