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
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;

/**
 * Really basic Artifact info for optimized queries.
 *
 * @author Dominik Brázdil &lt;dbrazdil@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = ArtifactInfo.Builder.class)
public class ArtifactInfo {

    /**
     * ID of the artifact.
     */
    protected final String id;

    /**
     * A unique identifier of the artifact in a repository. For example, for a maven artifact this is the GATVC
     * (groupId:artifactId:type:version[:classifier] The format of the identifier string is determined by the repository
     * type.
     */
    protected final String identifier;

    /**
     * Quality level of the artifact.
     */
    protected final ArtifactQuality artifactQuality;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc). This field determines the format of the
     * identifier string.
     */
    protected final RepositoryType repositoryType;

    /**
     * Category of the build denoting its support and usage
     */
    protected final BuildCategory buildCategory;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
