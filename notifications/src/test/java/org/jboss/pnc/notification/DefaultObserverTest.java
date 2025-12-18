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
package org.jboss.pnc.notification;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.notification.BuildChangedNotification;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.notification.dist.DefaultDistributedEventHandlerFactory;
import org.jboss.pnc.notification.dist.DistributedEventHandler;
import org.jboss.pnc.notification.dist.DistributedEventHandlerFactory;
import org.jboss.pnc.notification.dist.DistributedNotifier;
import org.jboss.pnc.rest.jackson.JacksonProvider;
import org.jboss.pnc.spi.coordinator.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.spi.notifications.AttachedClient;
import org.jboss.pnc.spi.notifications.Notifier;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DefaultObserverTest {

    @Test
    public void testObserve() throws Exception {
        AttachedClient attachedClient = mock(AttachedClient.class);
        doReturn(true).when(attachedClient).isEnabled();

        SystemConfig config = createConfiguration();
        // SystemConfig config = createKafkaConfiguration();
        // SystemConfig config = createInfinispanConfiguration();

        DistributedEventHandlerFactory factory = new DefaultDistributedEventHandlerFactory();
        DistributedEventHandler handler = factory.createDistributedEventHandler(config);

        Notifier notifier = new LocalNotifier();
        Notifier distributedNotifier = new DistributedNotifier(notifier, handler);

        distributedNotifier.attachClient(attachedClient);

        DefaultEventObserver observer = new DefaultEventObserver();
        observer.notifier = distributedNotifier;

        Thread.sleep(5000L); // wait for Kafka consumer to be subscribed

        Build build = Build.builder().status(BuildStatus.BUILDING).build();
        DefaultBuildStatusChangedEvent event = new DefaultBuildStatusChangedEvent(
                build,
                BuildStatus.BUILDING,
                BuildStatus.SUCCESS);
        observer.collectBuildStatusChangedEvent(event);

        JacksonProvider provider = new JacksonProvider();
        BuildChangedNotification notification = new BuildChangedNotification(event.getOldStatus(), event.getBuild());
        Object message = provider.getMapper().writeValueAsString(notification);

        Thread.sleep(5000L); // wait for the message to be consumed

        verify(attachedClient).sendMessage(message, notifier.getCallback());
        assertThat(notifier.getAttachedClientsCount()).isEqualTo(1);
    }

    private SystemConfig createConfiguration() {
        return new SystemConfig(
                "NO_AUTH",
                "10",
                "${product_short_name}-${product_version}-pnc",
                "10",
                null,
                null,
                "3600",
                "",
                "10",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "true",
                null,
                "false",
                null,
                null);
    }

    private SystemConfig createKafkaConfiguration() {
        return new SystemConfig(
                "NO_AUTH",
                "10",
                "${product_short_name}-${product_version}-pnc",
                "10",
                null,
                null,
                "3600",
                "",
                "10",
                "kafka",
                "localhost:9092",
                "mytopic",
                "1",
                "5",
                "100",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "true",
                null,
                "false",
                null,
                null);
    }

    private SystemConfig createInfinispanConfiguration() {
        return new SystemConfig(
                "NO_AUTH",
                "10",
                "${product_short_name}-${product_version}-pnc",
                "10",
                null,
                null,
                "3600",
                "",
                "10",
                "infinispan",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "pnc-ispn-cluster",
                null,
                "true",
                null,
                "false",
                null,
                null);
    }

}