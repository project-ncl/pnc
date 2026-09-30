/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.notifications.buildSetTask;

import java.util.function.Consumer;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildSetCallBack {
    private final Integer buildSetConfigurationId;
    private final Consumer<BuildSetStatusChangedEvent> callback;

    public BuildSetCallBack(int buildSetConfigurationId, Consumer<BuildSetStatusChangedEvent> callback) {
        this.buildSetConfigurationId = buildSetConfigurationId;
        this.callback = callback;
    }

    public Integer getBuildSetConfigurationId() {
        return buildSetConfigurationId;
    }

    public void callback(BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        callback.accept(buildSetStatusChangedEvent);
    }
}
