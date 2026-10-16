/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;
import java.util.List;

import javax.validation.Valid;

import org.jboss.pnc.api.enums.orch.CompletionStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
