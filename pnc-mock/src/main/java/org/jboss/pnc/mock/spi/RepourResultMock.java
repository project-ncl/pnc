/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.spi;

import org.jboss.pnc.api.enums.orch.CompletionStatus;
import org.jboss.pnc.spi.repour.RepourResult;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepourResultMock {

    public static RepourResult mock() {
        return new RepourResult(CompletionStatus.SUCCESS, "rootName", "rootVersion");
    }

    public static RepourResult mockFailed() {
        return new RepourResult(CompletionStatus.FAILED, "rootName", "rootVersion");
    }

}
