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
package org.jboss.pnc.remotecoordinator.test.event;

import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

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
