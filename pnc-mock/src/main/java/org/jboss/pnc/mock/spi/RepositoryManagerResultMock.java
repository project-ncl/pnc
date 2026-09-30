/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.spi;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.mock.model.builders.ArtifactBuilder;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryManagerResultMock {

    private static AtomicInteger rebuildNumber = new AtomicInteger();

    public static RepositoryManagerResult mockResult() {
        return mockResult(false);
    }

    public static RepositoryManagerResult mockResult(boolean failed) {
        int rebuild = rebuildNumber.getAndAdd(100);
        return mockResult(failed, rebuild);
    }

    public static RepositoryManagerResult mockResult(boolean failed, int base) {
        return new RepositoryManagerResult() {
            @Override
            public List<Artifact> getBuiltArtifacts() {
                Artifact[] artifacts = {
                        ArtifactBuilder.mockArtifact(base + 11),
                        ArtifactBuilder.mockArtifact(base + 12) };
                return Arrays.asList(artifacts);
            }

            @Override
            public List<Artifact> getDependencies() {
                Artifact[] artifacts = {
                        ArtifactBuilder.mockImportedArtifact(base + 21),
                        ArtifactBuilder.mockImportedArtifact(base + 22),
                        ArtifactBuilder.mockArtifact(base + 13) };
                return Arrays.asList(artifacts);
            }

            @Override
            public String getBuildContentId() {
                return "mock-content-id";
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
