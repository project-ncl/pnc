/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.core.test.buildCoordinator.event;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@ApplicationScoped
public class TestCDIBuildSetStatusChangedReceiver {

    public static final TestCDIBuildSetStatusChangedReceiver INSTANCE = new TestCDIBuildSetStatusChangedReceiver(); //TODO do not instance in applicationScoped

    private List<Consumer<BuildSetStatusChangedEvent>> listeners = new LinkedList<>();

    public void addBuildSetStatusChangedEventListener(Consumer<BuildSetStatusChangedEvent> listener) {
        listeners.add(listener);
    }

    synchronized public void collectEvent(@Observes BuildSetStatusChangedEvent buildSetStatusChangedEvent) {
        listeners.stream().forEach(listener -> listener.accept(buildSetStatusChangedEvent));
    }

    public void clear() {
        listeners.clear();
    }
}
