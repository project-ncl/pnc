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

package org.jboss.pnc.remotecoordinator.test;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mock.datastore.BuildTaskRepositoryMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.enterprise.event.Event;
import javax.inject.Inject;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildCoordinatorFactory {

    @Inject
    Event<BuildStatusChangedEvent> buildStatusChangedEventNotifier;

    @Inject
    Event<BuildSetStatusChangedEvent> buildSetStatusChangedEventNotifier;

    @Inject
    private GroupBuildMapper groupBuildMapper;

    @Inject
    private BuildMapper buildMapper;

    public BuildCoordinatorBeans createBuildCoordinator(DatastoreMock datastore) {
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore);

        SystemConfig systemConfig = createConfiguration();

        BuildTaskRepository taskRepository = new BuildTaskRepositoryMock();

// TODO       RexBuildSchedulerMockMock localBuildScheduler = new RexBuildSchedulerMockMock();
//
//         BuildCoordinator coordinator = new RemoteBuildCoordinator(
//                datastoreAdapter,
//                buildStatusChangedEventNotifier,
//                buildSetStatusChangedEventNotifier,
//                localBuildScheduler,
//                taskRepository,
//                systemConfig,
//                groupBuildMapper,
//                buildMapper);
//        localBuildScheduler.setBuildCoordinator(coordinator);
//
//        SetRecordUpdateJob setJob = new SetRecordUpdateJob(taskRepository, datastore, coordinator);
//
//        return new BuildCoordinatorBeans(taskRepository, coordinator, setJob);
        return null;
    }

    private SystemConfig createConfiguration() {
//        return new SystemConfig(
//                "NO_AUTH",
//                "10",
//                "${product_short_name}-${product_version}-pnc",
//                "10",
//                null,
//                "3600",
//                "14",
//                "",
//                "10",
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                null,
//                "false");
        return null;
    }

//    @Alternative
//    @ApplicationScoped
//    public static class RexBuildSchedulerMockMock extends RexBuildSchedulerMock {
//
//        @Inject
//        public RexBuildSchedulerMockMock(BuildExecutor buildExecutor, BuildCoordinator buildCoordinator) {
//            super(buildExecutor, buildCoordinator);
//        }
//
//        public void setBuildCoordinator(BuildCoordinator buildCoordinator) {
//            this.buildCoordinator = buildCoordinator;
//        }
//
//        public RexBuildSchedulerMockMock() {
//            buildExecutor = new BuildExecutorMock();
//        }
//    };
}
