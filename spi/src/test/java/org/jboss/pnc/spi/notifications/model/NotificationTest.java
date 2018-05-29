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
package org.jboss.pnc.spi.notifications.model;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class NotificationTest {

    @Test
    public void shouldDeserialize() throws Exception {
//        String serialized = "{\"eventType\":\"BUILD_STATUS_CHANGED\",\"payload\":{\"id\":386,\"buildCoordinationStatus\":\"ENQUEUED\",\"userId\":4,\"buildConfigurationId\":143,\"buildConfigurationName\":\"test-build-execution\"}}";
        String serialized = "{\"eventType\":\"BUILD_STATUS_CHANGED\",\"payload\":{\"id\":387,\"buildCoordinationStatus\":\"BUILDING\",\"userId\":4,\"buildConfigurationId\":143,\"buildConfigurationName\":\"test-build-execution\",\"buildStartTime\":1526473388394}}";
        Notification notification = new Notification(serialized);

        Assert.assertEquals(EventType.BUILD_STATUS_CHANGED, notification.getEventType());
    }
}
