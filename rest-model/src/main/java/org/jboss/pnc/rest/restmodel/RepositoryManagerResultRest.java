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

package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "repositoryManagerResult")
public class RepositoryManagerResultRest implements Serializable {
    private List<ArtifactRest> builtArtifacts;
    private List<ArtifactRest> dependencies;
    private String buildContentId;
    private String log;
    private CompletionStatus completionStatus;

    public RepositoryManagerResultRest() {}

    public RepositoryManagerResultRest(RepositoryManagerResult result) {
        builtArtifacts = result.getBuiltArtifacts().stream().map(artifact -> new ArtifactRest(artifact)).collect(Collectors.toList());
        dependencies = result.getDependencies().stream().map(artifact -> new ArtifactRest(artifact)).collect(Collectors.toList());
        buildContentId = result.getBuildContentId();
        log = result.getLog();
        completionStatus = result.getCompletionStatus();
    }

    public List<ArtifactRest> getBuiltArtifacts() {
        return builtArtifacts;
    }

    public List<ArtifactRest> getDependencies() {
        return dependencies;
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    public String getLog() {
        return log;
    }

    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }

    public RepositoryManagerResult toRepositoryManagerResult() {
        List<Artifact> builtArtifacts = getBuiltArtifacts().stream().map(artifactRest -> artifactRest.toDBEntityBuilder().build()).collect(Collectors.toList());
        List<Artifact> dependencies = getDependencies().stream().map(artifactRest -> artifactRest.toDBEntityBuilder().build()).collect(Collectors.toList());
        String buildContentId = getBuildContentId();

        return new GenericRepositoryManagerResult(builtArtifacts, dependencies, buildContentId, log, completionStatus);
    }

    @Override
    public String toString() {
        return "RepositoryManagerResultRest{" +
                "builtArtifacts=" + builtArtifacts +
                ", dependencies=" + dependencies +
                ", buildContentId='" + buildContentId + '\'' +
                ", log='" + log + '\'' +
                ", completionStatus=" + completionStatus +
                '}';
    }

    public String toStringLimited() {
        return "RepositoryManagerResultRest{" +
                ", buildContentId='" + buildContentId + '\'' +
                ", completionStatus=" + completionStatus +
                '}';
    }

    private class GenericRepositoryManagerResult implements RepositoryManagerResult {
        private final List<Artifact> builtArtifacts;
        private final List<Artifact> dependencies;
        private final String buildContentId;
        private final String log;
        private final CompletionStatus status;

        public GenericRepositoryManagerResult(
                List<Artifact> builtArtifacts,
                List<Artifact> dependencies,
                String buildContentId,
                String log,
                CompletionStatus status) {
            this.builtArtifacts = builtArtifacts;
            this.dependencies = dependencies;
            this.buildContentId = buildContentId;
            this.log = log;
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
        public String getLog() {
            return log;
        }

        @Override
        public CompletionStatus getCompletionStatus() {
            return status;
        }
    }
}
