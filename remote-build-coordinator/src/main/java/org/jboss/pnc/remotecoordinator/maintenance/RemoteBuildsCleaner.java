/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.maintenance;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface RemoteBuildsCleaner {

    Result deleteRemoteBuilds(BuildRecord buildRecord);
}
