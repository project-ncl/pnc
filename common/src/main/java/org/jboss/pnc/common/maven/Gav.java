/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.maven;

import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@AllArgsConstructor
public class Gav {

    private String groupId;
    private String artifactId;
    private String version;

    public static Gav parse(String identifier) {
        SimpleArtifactRef artifactRef = SimpleArtifactRef.parse(identifier);
        return new Gav(artifactRef.getGroupId(), artifactRef.getArtifactId(), artifactRef.getVersionString());
    }
}
