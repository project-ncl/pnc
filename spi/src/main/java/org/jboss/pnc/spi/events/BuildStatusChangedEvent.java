/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.events;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;

public interface BuildStatusChangedEvent {

    BuildStatus getOldStatus();

    BuildStatus getNewStatus();

    Build getBuild();

}
