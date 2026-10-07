/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.model.Attachment;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;
import org.jboss.pnc.spi.coordinator.ProcessException;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerResult;
import org.jboss.pnc.spi.repour.RepourResult;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-02.
 */
@AllArgsConstructor
public class BuildResult {

    @Getter
    private final CompletionStatus completionStatus;

    @Getter
    private final Optional<ProcessException> processException;

    @Getter
    private final Optional<BuildExecutionConfiguration> buildExecutionConfiguration;

    @Getter
    private final Optional<BuildDriverResult> buildDriverResult;

    /**
     * Note that RepositoryManagerResult can return nul if build was not successful completed.
     */
    @Getter
    private final Optional<RepositoryManagerResult> repositoryManagerResult;

    @Getter
    private final Optional<EnvironmentDriverResult> environmentDriverResult;

    @Getter
    private final Optional<RepourResult> repourResult;

    @Getter
    private final List<Attachment> attachments;

    @Getter
    private final Map<String, String> extraAttributes;

    public boolean hasFailed() {
        return processException.isPresent() || completionStatus.isFailed();
    }
}
