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
import org.junit.Assert;

import javax.enterprise.event.Observes;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TestBuildStatusUpdates {
    public void collectEvent(@Observes BuildStatusChangedEvent buildStatusChangedEvent) {
        Assert.assertNotEquals(
                "Status update event should not be fired if there is no status updates. " + buildStatusChangedEvent,
                buildStatusChangedEvent.getNewStatus(),
                buildStatusChangedEvent.getOldStatus());
    }
}