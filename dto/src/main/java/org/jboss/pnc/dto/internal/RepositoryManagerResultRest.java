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
package org.jboss.pnc.dto.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.orch.CompletionStatus;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@Builder(builderClassName = "Builder", toBuilder = true)
@Jacksonized
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryManagerResultRest implements Serializable {

    private final @Valid List<org.jboss.pnc.dto.Artifact> builtArtifacts;
    private final @Valid List<org.jboss.pnc.dto.Artifact> dependencies;
    private final String buildContentId;
    private final CompletionStatus completionStatus;

    @Override
    public String toString() {
        return "RepositoryManagerResultRest{" + "builtArtifacts=" + builtArtifacts + ", dependencies=" + dependencies
                + ", buildContentId='" + buildContentId + '\'' + ", completionStatus=" + completionStatus + '}';
    }

    public String toStringLimited() {
        return "RepositoryManagerResultRest{" + "buildContentId='" + buildContentId + '\'' + ", completionStatus="
                + completionStatus + '}';
    }
}
