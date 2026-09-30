/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.repour;

import java.io.Serializable;

import org.jboss.pnc.api.enums.orch.CompletionStatus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = RepourResult.RepourResultBuilder.class)
@Builder
@AllArgsConstructor
public class RepourResult implements Serializable {

    @Getter
    private final CompletionStatus completionStatus;

    @Getter
    private final String executionRootName;

    @Getter
    private final String executionRootVersion;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RepourResultBuilder {
    }

    @Override
    public String toString() {
        return toStringLimited();
    }

    public String toStringLimited() {
        return "RepourResult{" + "completionStatus=" + completionStatus + ", executionRootName='" + executionRootName
                + '\'' + ", executionRootVersion='" + executionRootVersion + '\'' + '}';
    }
}
