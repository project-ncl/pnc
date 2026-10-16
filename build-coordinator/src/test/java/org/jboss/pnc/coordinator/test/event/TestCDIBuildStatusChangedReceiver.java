/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.test.event;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

@ApplicationScoped
public class TestCDIBuildStatusChangedReceiver {
    // TODO instance should not be used with @ApplicationScoped
    public static final TestCDIBuildStatusChangedReceiver INSTANCE = new TestCDIBuildStatusChangedReceiver();

    private List<Consumer<BuildStatusChangedEvent>> listeners = new LinkedList<>();

    public void addBuildStatusChangedEventListener(Consumer<BuildStatusChangedEvent> listener) {
        listeners.add(listener);
    }

    synchronized public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        listeners.stream().forEach(listener -> listener.accept(buildStatusChangedEvent));
    }

    public void clear() {
        listeners.clear();
    }
}
