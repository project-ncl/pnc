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
package org.jboss.pnc.rest.trigger;

import com.google.common.base.Preconditions;
import org.jboss.logging.Logger;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.utils.BpmNotifier;
import org.jboss.pnc.rest.utils.HibernateLazyInitializer;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.coordinator.BuildCoordinator;
import org.jboss.pnc.spi.coordinator.BuildSetTask;
import org.jboss.pnc.spi.coordinator.BuildTask;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Stateless
public class BuildTriggerer {

    public interface BuildConfigurationSetTriggerResult {
        int getBuildRecordSetId();
        Collection<BuildTask> getBuildTasks();
    }

    private final Logger log = Logger.getLogger(BuildTriggerer.class);

    private BuildCoordinator buildCoordinator;

    private BuildConfigurationRepository buildConfigurationRepository;

    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    private BuildSetStatusNotifications buildSetStatusNotifications;
    private BuildStatusNotifications buildStatusNotifications;
    private BpmNotifier bpmNotifier;
    private HibernateLazyInitializer hibernateLazyInitializer;

    private SortInfoProducer sortInfoProducer;

    @Deprecated //not meant for usage its only to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator,
                          final BuildConfigurationRepository buildConfigurationRepository,
                          final BuildConfigurationSetRepository buildConfigurationSetRepository,
                          BuildSetStatusNotifications buildSetStatusNotifications,
                          BuildStatusNotifications buildStatusNotifications,
                          BpmNotifier bpmNotifier,
                          HibernateLazyInitializer hibernateLazyInitializer,
                          SortInfoProducer sortInfoProducer) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.buildSetStatusNotifications = buildSetStatusNotifications;
        this.buildStatusNotifications = buildStatusNotifications;
        this.bpmNotifier = bpmNotifier;
        this.hibernateLazyInitializer = hibernateLazyInitializer;
        this.sortInfoProducer = sortInfoProducer;
    }

    public int triggerBuild(final Integer buildConfigurationId, User currentUser, boolean keepPodAliveAfterFailure, boolean rebuildAll, URL callBackUrl)
            throws BuildConflictException {
        Consumer<BuildCoordinationStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.simpleHttpPostCallback(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        int buildTaskId = triggerBuild(buildConfigurationId, currentUser, keepPodAliveAfterFailure, rebuildAll);
        buildStatusNotifications.subscribe(new BuildCallBack(buildTaskId, onStatusUpdate));
        return buildTaskId;
    }

    public int triggerBuild(final Integer configurationId, User currentUser, boolean keepPodAliveAfterFailure, boolean rebuildAll) throws BuildConflictException {
        final BuildConfiguration configuration = buildConfigurationRepository.queryById(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        return buildCoordinator.build(
                hibernateLazyInitializer.initializeBuildConfigurationBeforeTriggeringIt(configuration),
                currentUser,
                keepPodAliveAfterFailure,
                rebuildAll).getId();
    }

    public boolean cancelBuild(int buildTaskId) throws BuildConflictException, CoreException {
        return buildCoordinator.cancel(buildTaskId);
    }

    public BuildConfigurationSetTriggerResult triggerBuildConfigurationSet(final Integer buildConfigurationSetId,
            User currentUser, boolean keepPodAliveAfterFailure, boolean rebuildAll, URL callBackUrl)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException {
        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.simpleHttpPostCallback(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        BuildConfigurationSetTriggerResult result = triggerBuildConfigurationSet(buildConfigurationSetId, currentUser,
                keepPodAliveAfterFailure,
                rebuildAll);
        buildSetStatusNotifications.subscribe(new BuildSetCallBack(result.getBuildRecordSetId(), onStatusUpdate));
        return result;
    }

    public BuildConfigurationSetTriggerResult triggerBuildConfigurationSet(final Integer buildConfigurationSetId,
            User currentUser, boolean keepPodAliveAfterFailure, boolean rebuildAll)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null,
                "Can't find configuration with given id=" + buildConfigurationSetId);

        BuildSetTask buildSetTask = buildCoordinator.build(
                hibernateLazyInitializer.initializeBuildConfigurationSetBeforeTriggeringIt(buildConfigurationSet),
                currentUser,
                keepPodAliveAfterFailure,
                rebuildAll);

        return new BuildConfigurationSetTriggerResult() {

            @Override
            public int getBuildRecordSetId() {
                return buildSetTask.getId();
            }

            @Override
            public Collection<BuildTask> getBuildTasks() {
                return buildSetTask.getBuildTasks();
            }
        };
    }

    private Set<Integer> parseIntegers(String buildRecordSetIdsCSV) {
        if (buildRecordSetIdsCSV != null && !buildRecordSetIdsCSV.equals("") && !buildRecordSetIdsCSV.equals("null") ) {
            return Arrays.asList(buildRecordSetIdsCSV.split(",")).stream().map(Integer::parseInt).collect(Collectors.toSet());
        } else {
            return null;
        }
    }

}
