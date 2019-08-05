/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.common.Configuration;
import org.jboss.pnc.common.json.ConfigurationParseException;
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.json.moduleprovider.PncConfigProvider;
import org.jboss.pnc.coordinator.builder.BuildQueue;
import org.jboss.pnc.coordinator.builder.BuildSchedulerFactory;
import org.jboss.pnc.coordinator.builder.DefaultBuildCoordinator;
import org.jboss.pnc.coordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.pnc.mapper.api.GroupBuildMapper;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildCoordinatorFactory {

    @Inject
    Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    @Inject
    BuildSchedulerFactory buildSchedulerFactory;

    @Inject
    private GroupBuildMapper groupBuildMapper;

    @Inject
    private BuildMapper buildMapper;

    public BuildCoordinatorBeans createBuildCoordinator(DatastoreMock datastore) throws ConfigurationParseException {
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        Configuration configuration = createConfiguration();
        BuildQueue queue = new BuildQueue(configuration);
        BuildCoordinator coordinator = new DefaultBuildCoordinator(datastoreAdapter, buildStatusChangedEventNotifier, buildSetStatusChangedEventNotifier,
                buildSchedulerFactory, queue, configuration.getModuleConfig(new PncConfigProvider<>(SystemConfig.class)),
                groupBuildMapper, buildMapper);
        coordinator.start();
        queue.initSemaphore();
        return new BuildCoordinatorBeans(queue, coordinator);
    }

    private Configuration createConfiguration() {
        try {
            Configuration configuration = mock(Configuration.class);
            doReturn(new SystemConfig(
                    "ProperDriver",
                    "local-build-scheduler",
                    "NO_AUTH",
                    "10",
                    "10",
                    "10",
                    "${product_short_name}-${product_version}-pnc",
                    "10",
                    null,
                    null,
                    "",
                    "",
                    "10")
                ).when(configuration)
                .getModuleConfig(any(PncConfigProvider.class));
            return configuration;
        } catch (ConfigurationParseException e) {
            throw new IllegalStateException("Unexpected exception while creating configuration mock", e);
        }
    }
}
