/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.mock.model.builders;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactRepo;

import java.time.Instant;
import java.util.Date;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ArtifactBuilder {
    public static final String IDENTIFIER_PREFIX = "org.jboss.pnc:mock.artifact";

    public static Artifact mockImportedArtifact(int id) {
        return Artifact.Builder.newBuilder()
                .id(id)
                .identifier(IDENTIFIER_PREFIX + ":" + id)
                .checksum("ABCD1234")
                .originUrl("http://central.maven.org/" + id + ".jar")
                .importDate(Date.from(Instant.now()))
                .deployUrl("deploy url " + id)
                .repoType(ArtifactRepo.Type.MAVEN)
                .filename("File " + id + ".jar")
                .build();
    }

    public static Artifact mockBuiltArtifact(int id) {
        return Artifact.Builder.newBuilder()
                .id(id)
                .identifier(IDENTIFIER_PREFIX + ":" + id)
                .checksum("ABCDABCD")
                .deployUrl("deploy url " + id)
                .repoType(ArtifactRepo.Type.MAVEN)
                .filename("File " + id + ".jar")
                .build();
    }
}
