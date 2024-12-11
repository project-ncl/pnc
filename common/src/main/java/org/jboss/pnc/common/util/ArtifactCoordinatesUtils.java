/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
