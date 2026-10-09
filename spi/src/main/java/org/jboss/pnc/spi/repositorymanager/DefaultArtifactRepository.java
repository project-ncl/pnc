/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class DefaultArtifactRepository implements ArtifactRepository {
    private final String id;

    private final String name;

    private final String url;

    private final Boolean releases;

    private final Boolean snapshots;
}
