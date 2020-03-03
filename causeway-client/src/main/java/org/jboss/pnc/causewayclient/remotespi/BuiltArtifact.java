/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.causewayclient.remotespi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NonNull;

/**
 *
 * @author Honza Brázdil <janinko.g@gmail.com>
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@artifactType")
@JsonSubTypes({ @JsonSubTypes.Type(NpmBuiltArtifact.class), @JsonSubTypes.Type(MavenBuiltArtifact.class) })
public class BuiltArtifact {
    private final int id;
    @NonNull
    private final String filename;
    @NonNull
    private final String architecture;
    @NonNull
    private final String md5;
    @NonNull
    private final String artifactPath;
    @NonNull
    private final String repositoryPath;
    private final int size;

    public BuiltArtifact(
            int id,
            String filename,
            String architecture,
            String md5,
            String artifactPath,
            String repositoryPath,
            int size) {
        this.id = id;
        this.filename = filename;
        this.architecture = architecture;
        this.md5 = md5;
        this.artifactPath = artifactPath;
        this.repositoryPath = repositoryPath;
        this.size = size;
    }
}
