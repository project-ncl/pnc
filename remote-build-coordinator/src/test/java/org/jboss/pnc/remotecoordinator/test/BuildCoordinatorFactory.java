/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.test;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mock.datastore.BuildTaskRepositoryMock;
import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.remotecoordinator.builder.BifrostLogUploaderMock;
import org.jboss.pnc.remotecoordinator.builder.datastore.DatastoreAdapter;
import org.jboss.pnc.spi.datastore.BuildTaskRepository;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

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
        DatastoreAdapter datastoreAdapter = new DatastoreAdapter(datastore, new BifrostLogUploaderMock());

        SystemConfig systemConfig = createConfiguration();

        BuildTaskRepository taskRepository = new BuildTaskRepositoryMock();

        // TODO RexBuildSchedulerMockMock localBuildScheduler = new RexBuildSchedulerMockMock();
        //
        // BuildCoordinator coordinator = new RemoteBuildCoordinator(
        // datastoreAdapter,
        // buildStatusChangedEventNotifier,
        // buildSetStatusChangedEventNotifier,
        // localBuildScheduler,
        // taskRepository,
        // systemConfig,
        // groupBuildMapper,
        // buildMapper);
        // localBuildScheduler.setBuildCoordinator(coordinator);
        //
        // SetRecordUpdateJob setJob = new SetRecordUpdateJob(taskRepository, datastore, coordinator);
        //
        // return new BuildCoordinatorBeans(taskRepository, coordinator, setJob);
        return null;
    }

    private SystemConfig createConfiguration() {
        // return new SystemConfig(
        // "NO_AUTH",
        // "10",
        // "${product_short_name}-${product_version}-pnc",
        // "10",
        // null,
        // "3600",
        // "14",
        // "",
        // "10",
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // null,
        // "false");
        return null;
    }

    // @Alternative
    // @ApplicationScoped
    // public static class RexBuildSchedulerMockMock extends RexBuildSchedulerMock {
    //
    // @Inject
    // public RexBuildSchedulerMockMock(BuildExecutor buildExecutor, BuildCoordinator buildCoordinator) {
    // super(buildExecutor, buildCoordinator);
    // }
    //
    // public void setBuildCoordinator(BuildCoordinator buildCoordinator) {
    // this.buildCoordinator = buildCoordinator;
    // }
    //
    // public RexBuildSchedulerMockMock() {
    // buildExecutor = new BuildExecutorMock();
    // }
    // };
}
