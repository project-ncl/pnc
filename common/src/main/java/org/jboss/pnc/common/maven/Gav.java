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
package org.jboss.pnc.common.maven;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;

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
