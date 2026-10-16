/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.spi;

import java.util.Optional;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.environment.EnvironmentDriverResult;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class EnvironmentDriverResultMock {

    public static EnvironmentDriverResult mock() {
        return new EnvironmentDriverResult(
                CompletionStatus.SUCCESS,
                Optional.of(new SshCredentials("command", "password")));
    }

}
