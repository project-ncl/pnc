/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.builddriver;

import java.util.Optional;

import org.jboss.pnc.enums.BuildStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder")
public class DefaultBuildDriverResult implements BuildDriverResult {
    private final BuildStatus buildStatus;

    private final Optional<String> outputChecksum;
}
