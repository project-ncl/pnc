/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.dto.Attachment;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenImporting;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

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

    private @Valid List<@NotNull(groups = WhenImporting.class) Attachment> attachments;

    private Map<@NotNull String, @NotNull String> extraAttributes;

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
