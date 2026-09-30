/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.notifications.buildTask;

import java.util.function.Consumer;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Deprecated // used in test only
public class BuildCallBack {

    private final String buildTaskId;
    private final Consumer<BuildStatusChangedEvent> callback;

    public BuildCallBack(String buildTaskId, Consumer<BuildStatusChangedEvent> callback) {
        this.buildTaskId = buildTaskId;
        this.callback = callback;
    }

    public String getBuildTaskId() {
        return buildTaskId;
    }

    @Deprecated // used only in the tests
    public void callback(BuildStatusChangedEvent buildStatusChangedEvent) {
        callback.accept(buildStatusChangedEvent);
    }

    @Override
    public String toString() {
        return "TaskId:" + buildTaskId + "; CallbackConsumer:" + callback.toString();
    }
}
