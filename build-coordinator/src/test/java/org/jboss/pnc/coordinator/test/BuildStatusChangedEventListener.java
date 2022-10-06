/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.coordinator.test;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

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
