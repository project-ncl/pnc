/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.environment;

import java.io.Serializable;
import java.util.Optional;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.spi.SshCredentials;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = EnvironmentDriverResult.EnvironmentDriverResultBuilder.class)
@Builder
@AllArgsConstructor
public class EnvironmentDriverResult implements Serializable {

    @Getter
    private final CompletionStatus completionStatus;

    @Getter
    private final Optional<SshCredentials> sshCredentials;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class EnvironmentDriverResultBuilder {
    }

    @Override
    public String toString() {
        return toStringLimited();
    }

    public String toStringLimited() {
        return "EnvironmentDriverResult{" + "completionStatus=" + completionStatus + ", sshCredentials="
                + sshCredentials + '}';
    }
}
