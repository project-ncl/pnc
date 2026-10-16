/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildStatusChangedEventListener implements Event<BuildStatusChangedEvent> {

    Consumer<BuildStatusChangedEvent> onEvent;

    public BuildStatusChangedEventListener(Consumer<BuildStatusChangedEvent> onEvent) {
        this.onEvent = onEvent;
    }

    @Override
    public void fire(BuildStatusChangedEvent event) {
        onEvent.accept(event);
    }

    @Override
    public <U extends BuildStatusChangedEvent> CompletionStage<U> fireAsync(U event) {
        return null;
    }

    @Override
    public <U extends BuildStatusChangedEvent> CompletionStage<U> fireAsync(U event, NotificationOptions options) {
        return null;
    }

    @Override
    public Event<BuildStatusChangedEvent> select(Annotation... qualifiers) {
        return null;
    }

    @Override
    public <U extends BuildStatusChangedEvent> Event<U> select(Class<U> subtype, Annotation... qualifiers) {
        return null;
    }

    @Override
    public <U extends BuildStatusChangedEvent> Event<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return null;
    }
}
