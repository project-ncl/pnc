/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repositorymanager;

import java.util.List;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.model.Artifact;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class DefaultRepositoryManagerResult implements RepositoryManagerResult {
    private final List<Artifact> builtArtifacts;

    private final List<Artifact> dependencies;

    private final String buildContentId;

    private final CompletionStatus completionStatus;

}
