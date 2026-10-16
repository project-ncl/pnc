/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.restclient.util;

import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.npm.ident.ref.NpmPackageRef;
import org.jboss.pnc.common.util.ArtifactCoordinatesUtils;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.enums.RepositoryType;

public class ArtifactUtil {

    public static SimpleArtifactRef parseMavenCoordinates(ArtifactRef artifact) {
        if (artifact instanceof Artifact) {
            Artifact fullArtifact = (Artifact) artifact;
            if (fullArtifact.getTargetRepository().getRepositoryType() != RepositoryType.MAVEN) {
                throw new IllegalArgumentException("Artifact " + artifact + "is not a maven artifact");
            }
        }

        return ArtifactCoordinatesUtils.parseMavenCoordinates(artifact.getDeployPath());
    }

    public static NpmPackageRef parseNPMCoordinates(ArtifactRef artifact) {
        if (artifact instanceof Artifact) {
            Artifact fullArtifact = (Artifact) artifact;
            if (fullArtifact.getTargetRepository().getRepositoryType() != RepositoryType.NPM) {
                throw new IllegalArgumentException("Artifact " + artifact + "is not an NPM artifact");
            }
        }

        return ArtifactCoordinatesUtils.parseNPMCoordinates(artifact.getDeployPath());
    }

}
