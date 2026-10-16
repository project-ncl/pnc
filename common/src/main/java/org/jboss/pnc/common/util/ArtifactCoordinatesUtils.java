/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;
import org.commonjava.atlas.maven.ident.util.ArtifactPathInfo;
import org.commonjava.atlas.npm.ident.ref.NpmPackageRef;
import org.commonjava.atlas.npm.ident.util.NpmPackagePathInfo;

public class ArtifactCoordinatesUtils {
    public static SimpleArtifactRef parseMavenCoordinates(String artifactDeployPath) {
        ArtifactPathInfo pathInfo = ArtifactPathInfo.parse(artifactDeployPath);
        if (pathInfo == null) {
            return null;
        }

        return new SimpleArtifactRef(pathInfo.getProjectId(), pathInfo.getType(), pathInfo.getClassifier());
    }

    public static NpmPackageRef parseNPMCoordinates(String artifactDeployPath) {
        NpmPackagePathInfo npmPathInfo = NpmPackagePathInfo.parse(artifactDeployPath);
        if (npmPathInfo == null) {
            return null;
        }

        return new NpmPackageRef(npmPathInfo.getName(), npmPathInfo.getVersion());
    }
}
