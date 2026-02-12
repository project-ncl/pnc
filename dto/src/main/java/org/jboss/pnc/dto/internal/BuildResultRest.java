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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@Setter
@Builder(builderClassName = "Builder", toBuilder = true)
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildResultRest implements Serializable {

    @NotNull(groups = { WhenCreatingNew.class, WhenImporting.class })
    private CompletionStatus completionStatus;

    private ProcessException processException;

    private BuildExecutionConfigurationRest buildExecutionConfiguration;

    private RepourResultRest repourResult;

    private EnvironmentDriverResultRest environmentDriverResult;

    private BuildDriverResultRest buildDriverResult;

    private @Valid RepositoryManagerResultRest repositoryManagerResult;

    private @Valid List<Artifact> attachments;

    private Map<String, String> extraAttributes;

    @Override
    public String toString() {
        return "BuildResultRest{" + "completionStatus=" + completionStatus + ", processException=" + processException
                + '\'' + ", buildExecutionConfiguration=" + buildExecutionConfiguration + ", buildDriverResult="
                + (buildDriverResult == null ? null : buildDriverResult.toStringLimited())
                + ", repositoryManagerResult="
                + (repositoryManagerResult == null ? null : repositoryManagerResult.toStringLimited())
                + ", environmentDriverResult="
                + (environmentDriverResult == null ? null : environmentDriverResult.toStringLimited())
                + ", repourResult=" + (repourResult == null ? null : repourResult.toStringLimited()) + '}';
    }

}
