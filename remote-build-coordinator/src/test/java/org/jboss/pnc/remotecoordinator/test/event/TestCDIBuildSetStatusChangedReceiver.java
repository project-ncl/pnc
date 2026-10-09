/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test.event;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class TestCDIBuildSetStatusChangedReceiver {

    private static final Logger log = LoggerFactory.getLogger(TestCDIBuildSetStatusChangedReceiver.class);

    private List<Consumer<BuildSetStatusChangedEvent>> listeners = new LinkedList<>();

    public synchronized void addBuildSetStatusChangedEventListener(Consumer<BuildSetStatusChangedEvent> listener) {
        log.info("Adding BuildSetStatusChangedEventListener {}.", listener);
        listeners.add(listener);
    }

    public synchronized void collectEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        log.debug("Observed new BuildSetStatusChangedEvent {}.", buildSetStatusChangedEvent);
        listeners.stream().forEach(listener -> listener.accept(buildSetStatusChangedEvent));
    }

    public void clear() {
        listeners.clear();
    }
}
