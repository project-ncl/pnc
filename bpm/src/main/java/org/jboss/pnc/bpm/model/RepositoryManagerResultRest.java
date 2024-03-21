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

package org.jboss.pnc.bpm.model;

import lombok.AllArgsConstructor;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@AllArgsConstructor
@XmlRootElement(name = "repositoryManagerResult")
public class RepositoryManagerResultRest implements Serializable {
    private List<org.jboss.pnc.dto.Artifact> builtArtifacts;
    private List<org.jboss.pnc.dto.Artifact> dependencies;
    private String buildContentId;
    private CompletionStatus completionStatus;

    public RepositoryManagerResultRest() {
    }

    public List<org.jboss.pnc.dto.Artifact> getBuiltArtifacts() {
        return builtArtifacts;
    }

    public List<org.jboss.pnc.dto.Artifact> getDependencies() {
        return dependencies;
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    @Override
    public String toString() {
        return "RepositoryManagerResultRest{" + "builtArtifacts=" + builtArtifacts + ", dependencies=" + dependencies
                + ", buildContentId='" + buildContentId + '\'' + ", completionStatus=" + completionStatus + '}';
    }

    public String toStringLimited() {
        return "RepositoryManagerResultRest{" + "buildContentId='" + buildContentId + '\'' + ", completionStatus="
                + completionStatus + '}';
    }

    public static class GenericRepositoryManagerResult implements RepositoryManagerResult {
        private final List<Artifact> builtArtifacts;
        private final List<Artifact> dependencies;
        private final String buildContentId;
        private final CompletionStatus status;

        public GenericRepositoryManagerResult(
                List<Artifact> builtArtifacts,
                List<Artifact> dependencies,
                String buildContentId,
                CompletionStatus status) {
            this.builtArtifacts = builtArtifacts;
            this.dependencies = dependencies;
            this.buildContentId = buildContentId;
            this.status = status;
        }

        @Override
        public List<Artifact> getBuiltArtifacts() {
            return builtArtifacts;
        }

        @Override
        public List<Artifact> getDependencies() {
            return dependencies;
        }

        @Override
        public String getBuildContentId() {
            return buildContentId;
        }

        @Override
        public CompletionStatus getCompletionStatus() {
            return status;
        }
    }
}
