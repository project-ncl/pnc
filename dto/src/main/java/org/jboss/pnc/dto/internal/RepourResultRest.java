/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;

import org.jboss.pnc.api.enums.orch.CompletionStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder(builderClassName = "Builder", toBuilder = true)
@Jacksonized
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepourResultRest implements Serializable {

    private final CompletionStatus completionStatus;
    private final String executionRootName;
    private final String executionRootVersion;

    @Override
    public String toString() {
        return toStringLimited();
    }

    public String toStringLimited() {
        return "RepourResult{" + "completionStatus=" + completionStatus + ", executionRootName='" + executionRootName
                + '\'' + ", executionRootVersion='" + executionRootVersion + '\'' + '}';
    }
}
