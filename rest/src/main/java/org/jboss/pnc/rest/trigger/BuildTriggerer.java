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
import org.jboss.pnc.common.json.moduleconfig.SystemConfig;
import org.jboss.pnc.common.mdc.MDCMeta;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.coordinator.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.coordinator.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.utils.BpmNotifier;
import org.jboss.pnc.rest.utils.HibernateLazyInitializer;
import org.jboss.pnc.spi.BuildOptions;
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
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

@Stateless
public class BuildTriggerer {
    private final Logger log = Logger.getLogger(BuildTriggerer.class);

    private BuildCoordinator buildCoordinator;

    private BuildConfigurationRepository buildConfigurationRepository;

    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    private BuildSetStatusNotifications buildSetStatusNotifications;
    private BuildStatusNotifications buildStatusNotifications;
    private BpmNotifier bpmNotifier;
    private HibernateLazyInitializer hibernateLazyInitializer;

    private SortInfoProducer sortInfoProducer;

    private SystemConfig systemConfig;

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
                          SortInfoProducer sortInfoProducer,
                          SystemConfig systemConfig) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.buildSetStatusNotifications = buildSetStatusNotifications;
        this.buildStatusNotifications = buildStatusNotifications;
        this.bpmNotifier = bpmNotifier;
        this.hibernateLazyInitializer = hibernateLazyInitializer;
        this.sortInfoProducer = sortInfoProducer;
        this.systemConfig = systemConfig;
    }

    public int triggerBuild(final Integer buildConfigurationId,
                            User currentUser,
                            BuildOptions buildOptions,
                            URL callBackUrl)
            throws BuildConflictException, CoreException {
        Consumer<BuildCoordinationStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.simpleHttpPostCallback(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        BuildConfigurationSetTriggerResult result = doTriggerBuild(buildConfigurationId, currentUser, buildOptions);
        result.getBuildTasks().forEach(t -> buildStatusNotifications.subscribe(new BuildCallBack(t.getId(), onStatusUpdate)));
        return selectBuildRecordIdOf(result.getBuildTasks(), buildConfigurationId);
    }

    private int selectBuildRecordIdOf(Collection<BuildTask> buildTasks, Integer buildConfigurationId) throws CoreException {
        Optional<BuildTask> maybeTask = buildTasks.stream()
                .filter(t -> t.getBuildConfiguration().getId().equals(buildConfigurationId))
                .findAny();
        return maybeTask.map(BuildTask::getId)
                .orElseThrow(() -> new CoreException("No build id for the triggered configuration"));
    }

    public int triggerBuild(final Integer configurationId, User currentUser, BuildOptions buildOptions)
            throws BuildConflictException, CoreException {
        BuildConfigurationSetTriggerResult result =
                doTriggerBuild(configurationId, currentUser, buildOptions);
        return selectBuildRecordIdOf(result.getBuildTasks(), configurationId);
    }

    private BuildConfigurationSetTriggerResult doTriggerBuild(final Integer configurationId, User currentUser,
                                                              BuildOptions buildOptions)
            throws BuildConflictException, CoreException {
        final BuildConfiguration configuration = buildConfigurationRepository.queryById(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        // Initialize BuildConfiguration and load lazy loaded dependencies for processing in CDI beans as DefaultBuildCoordinator
        BuildConfiguration buildConfiguration = hibernateLazyInitializer.initializeBuildConfigurationBeforeTriggeringIt(configuration);
        buildConfiguration.getDependencies();
        buildConfiguration.getIndirectDependencies();

        BuildSetTask buildSetTask = buildCoordinator.build(
                hibernateLazyInitializer.initializeBuildConfigurationBeforeTriggeringIt(configuration),
                currentUser,
                buildOptions);
        return BuildConfigurationSetTriggerResult.fromBuildSetTask(buildSetTask);
    }

    public boolean cancelBuild(int buildTaskId) throws BuildConflictException, CoreException {
        return buildCoordinator.cancel(buildTaskId);
    }

    public BuildConfigurationSetTriggerResult triggerBuildConfigurationSet(
            final Integer buildConfigurationSetId,
            User currentUser,
            BuildOptions buildOptions,
            URL callBackUrl)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException {
        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.simpleHttpPostCallback(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        BuildConfigurationSetTriggerResult result = triggerBuildConfigurationSet(buildConfigurationSetId, currentUser, buildOptions);
        buildSetStatusNotifications.subscribe(new BuildSetCallBack(result.getBuildRecordSetId(), onStatusUpdate));
        return result;
    }

    public BuildConfigurationSetTriggerResult triggerBuildConfigurationSet(final Integer buildConfigurationSetId,
            User currentUser, BuildOptions buildOptions)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null,
                "Can't find configuration with given id=" + buildConfigurationSetId);

        BuildSetTask buildSetTask = buildCoordinator.build(
                hibernateLazyInitializer.initializeBuildConfigurationSetBeforeTriggeringIt(buildConfigurationSet),
                currentUser,
                buildOptions);

        return BuildConfigurationSetTriggerResult.fromBuildSetTask(buildSetTask);
    }

    public Optional<MDCMeta> getMdcMeta(Integer buildTaskId) {
        return buildCoordinator.getMDCMeta(buildTaskId);
    }

}
