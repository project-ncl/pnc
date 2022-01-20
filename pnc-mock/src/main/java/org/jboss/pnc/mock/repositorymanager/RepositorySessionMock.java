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
package org.jboss.pnc.mock.repositorymanager;

import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.coordinator.CompletionStatus;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConnectionInfo;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.pnc.enums.RepositoryType;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
public class RepositorySessionMock implements RepositorySession {
    @Override
    public RepositoryType getType() {
        return RepositoryType.MAVEN;
    }

    @Override
    public String getBuildRepositoryId() {
        return "test";
    }

    @Override
    public RepositoryConnectionInfo getConnectionInfo() {
        return new RepositoryConnectionInfo() {
            // TODO: This is not connected to anything...
            String repo = "http://localhost:8090/api/groups/test";

            @Override
            public String getToolchainUrl() {
                return repo;
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> props = new HashMap<>();
                props.put("altDeploymentRepository", "test::default::" + repo);

                return props;
            }

            @Override
            public String getDependencyUrl() {
                return repo;
            }

            @Override
            public String getDeployUrl() {
                return repo;
            }
        };
    }

    @Override
    public RepositoryManagerResult extractBuildArtifacts(boolean liveBuild) throws RepositoryManagerException {
        return new RepositoryManagerResult() {
            @Override
            public List<Artifact> getBuiltArtifacts() {
                List<Artifact> builtArtifacts = new ArrayList<>();
                builtArtifacts.add(getArtifact(1));
                return builtArtifacts;
            }

            @Override
            public List<Artifact> getDependencies() {
                List<Artifact> dependencies = new ArrayList<>();
                dependencies.add(getArtifact(10));
                return dependencies;
            }

            @Override
            public String getBuildContentId() {
                return "mock-content-id";
            }

            @Override
            public String getLog() {
                return "";
            }

            @Override
            public CompletionStatus getCompletionStatus() {
                return CompletionStatus.SUCCESS;
            }
        };
    }

    private Artifact getArtifact(int i) {
        return Artifact.Builder.newBuilder().id(i).identifier(ArtifactBuilder.IDENTIFIER_PREFIX + i).build();
    }

    @Override
    public void close() {
    }

    @Override
    public void deleteBuildGroup() throws RepositoryManagerException {
    }

}
