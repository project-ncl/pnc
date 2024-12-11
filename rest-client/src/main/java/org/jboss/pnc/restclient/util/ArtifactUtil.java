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
