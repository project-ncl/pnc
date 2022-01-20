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
package org.jboss.pnc.coordinator.test.event;

import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
