/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.logging;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@AllArgsConstructor
@Getter
public class BuildTaskContext {

    private final String buildContentId;

    private final String userId;

    private final boolean temporaryBuild;

    private final Instant temporaryBuildExpireDate;
}
