/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.spi;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.pnc.api.enums.AttachmentType;
import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.spi.BuildResult;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildResultMock {

    private static AtomicInteger rebuildNumber = new AtomicInteger(100);

    public static BuildResult mock(BuildStatus status) {
        BuildExecutionConfiguration buildExecutionConfig = BuildExecutionConfigurationMock.mockConfig();
        BuildDriverResult buildDriverResult = BuildDriverResultMock.mockResult(status);
        RepositoryManagerResult repositoryManagerResult = RepositoryManagerResultMock.mockResult();
        Map<String, String> attributes = Map.of("EXTERNAL-EXECUTION-ID", "123");

        CompletionStatus completionStatus;
        if (status.completedSuccessfully()) {
            completionStatus = CompletionStatus.SUCCESS;
        } else {
            completionStatus = CompletionStatus.FAILED;
        }

        return new BuildResult(
                completionStatus,
                Optional.of(new ProcessException("Test Exception.")),
                Optional.ofNullable(buildExecutionConfig),
                Optional.ofNullable(buildDriverResult),
                Optional.ofNullable(repositoryManagerResult),
                Optional.of(EnvironmentDriverResultMock.mock()),
                Optional.of(RepourResultMock.mock()),
                List.of(mockAttachment("Build Log"), mockAttachment("Alignment Log")),
                attributes);
    }

    private static Attachment mockAttachment(String name) {
        int id = rebuildNumber.getAndAdd(1);
        return Attachment.builder()
                .id(id)
                .name(name)
                .sha256("sha256-" + id)
                .description(String.valueOf(id))
                .type(AttachmentType.LOG)
                .url("http://filestore.com/file-" + name + "-" + id + ".txt")
                .build();
    }
}
