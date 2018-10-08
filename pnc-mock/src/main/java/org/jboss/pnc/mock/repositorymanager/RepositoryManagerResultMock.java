/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryManagerResultMock {

    public static RepositoryManagerResult mockResult() {
        return mockResult(false);
    }

    public static RepositoryManagerResult mockResult(boolean failed) {
        return new RepositoryManagerResult() {
            @Override
            public List<Artifact> getBuiltArtifacts() {
                Artifact[] artifacts = {ArtifactBuilder.mockArtifact(11), ArtifactBuilder.mockArtifact(12)};
                return Arrays.asList(artifacts);
            }

            @Override
            public List<Artifact> getDependencies() {
                Artifact[] artifacts = { ArtifactBuilder.mockImportedArtifact(21), ArtifactBuilder.mockImportedArtifact(22),
                        ArtifactBuilder.mockArtifact(13) };
                return Arrays.asList(artifacts);
            }

            @Override
            public String getBuildContentId() {
                return "mock-content-id";
            }

            @Override
            public String getLog() {
                if (failed) {
                    return "MOCK: Validation of org.jboss.pnc:pnc-mock:1.0 failed due to: invalid pom file.";
                } else {
                    return "MOCK: Repository manager has promoted all collected artifacts.";
                }
            }

            @Override
            public CompletionStatus getCompletionStatus() {
                if (failed) {
                    return CompletionStatus.FAILED;
                } else {
                    return CompletionStatus.SUCCESS;
                }
            }
        };
    }
}
