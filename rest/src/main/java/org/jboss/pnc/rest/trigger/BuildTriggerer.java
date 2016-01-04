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
import org.jboss.pnc.core.builder.coordinator.BuildCoordinator;
import org.jboss.pnc.core.builder.coordinator.BuildSetTask;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.executor.DefaultBuildExecutionConfiguration;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.utils.BpmNotifier;
import org.jboss.pnc.rest.utils.HibernateLazyInitializer;
import org.jboss.pnc.spi.BuildCoordinationStatus;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.events.BuildCoordinationStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildExecutionStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.jboss.pnc.spi.executor.BuildExecutionSession;
import org.jboss.pnc.spi.executor.BuildExecutor;
import org.jboss.pnc.spi.executor.exceptions.ExecutorException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Stateless
public class BuildTriggerer {

    public interface BuildConfigurationSetTriggerResult {
        int getBuildRecordSetId();
        List<Integer> getBuildRecordsIds();
    }

    private final Logger log = Logger.getLogger(BuildTriggerer.class);

    private BuildCoordinator buildCoordinator;
    private BuildExecutor buildExecutor;

    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;

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
                          BuildExecutor buildExecutor,
                          final BuildConfigurationRepository buildConfigurationRepository,
                          BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
                          final BuildConfigurationSetRepository buildConfigurationSetRepository,
                          BuildSetStatusNotifications buildSetStatusNotifications,
                          BuildStatusNotifications buildStatusNotifications,
                          BpmNotifier bpmNotifier,
                          HibernateLazyInitializer hibernateLazyInitializer,
                          SortInfoProducer sortInfoProducer) {
        this.buildCoordinator = buildCoordinator;
        this.buildExecutor = buildExecutor;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
        this.buildSetStatusNotifications = buildSetStatusNotifications;
        this.buildStatusNotifications = buildStatusNotifications;
        this.bpmNotifier = bpmNotifier;
        this.hibernateLazyInitializer = hibernateLazyInitializer;
        this.sortInfoProducer = sortInfoProducer;
    }

    public int triggerBuild(final Integer buildConfigurationId, User currentUser, boolean rebuildAll, URL callBackUrl)
            throws BuildConflictException {
        Consumer<BuildCoordinationStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.signalBpmEvent(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        int buildTaskId = triggerBuild(buildConfigurationId, currentUser, rebuildAll);
        buildStatusNotifications.subscribe(new BuildCallBack(buildTaskId, onStatusUpdate));
        return buildTaskId;
    }

    public int triggerBuild(final Integer configurationId, User currentUser, boolean rebuildAll) throws BuildConflictException {
        final BuildConfiguration configuration = buildConfigurationRepository.queryById(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        if (configuration.getProductVersions() != null && !configuration.getProductVersions().isEmpty()) {
            ProductVersion productVersion = configuration.getProductVersions().iterator().next();
            buildRecordSet.setPerformedInProductMilestone(productVersion.getCurrentProductMilestone());
        }

        Integer taskId = buildCoordinator.build(
                hibernateLazyInitializer.initializeBuildConfigurationBeforeTriggeringIt(configuration),
                currentUser,
                rebuildAll).getId();
        return taskId;
    }

    public BuildConfigurationSetTriggerResult triggerBuildConfigurationSet(final Integer buildConfigurationSetId,
            User currentUser, boolean rebuildAll, URL callBackUrl)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException {
        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if (statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.signalBpmEvent(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        BuildConfigurationSetTriggerResult result = triggerBuildConfigurationSet(buildConfigurationSetId, currentUser,
                rebuildAll);
        buildSetStatusNotifications.subscribe(new BuildSetCallBack(result.getBuildRecordSetId(), onStatusUpdate));
        return result;
    }

    public BuildConfigurationSetTriggerResult triggerBuildConfigurationSet(final Integer buildConfigurationSetId,
            User currentUser, boolean rebuildAll)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null,
                "Can't find configuration with given id=" + buildConfigurationSetId);

        BuildSetTask buildSetTask = buildCoordinator.build(
                hibernateLazyInitializer.initializeBuildConfigurationSetBeforeTriggeringIt(buildConfigurationSet),
                currentUser,
                rebuildAll);

        return new BuildConfigurationSetTriggerResult() {

            @Override
            public int getBuildRecordSetId() {
                return buildSetTask.getId();
            }

            @Override
            public List<Integer> getBuildRecordsIds() {
                return buildSetTask.getBuildTasks().stream()
                        .map(buildTask -> buildTask.getId())
                        .collect(Collectors.toList());
            }
        };
    }

    public BuildExecutionSession executeBuild(
            Integer buildTaskId,
            Integer buildConfigurationId,
            Integer buildConfigurationRevision,
            String buildRecordSetIdsCSV,
            String buildConfigSetRecordId,
            User userTriggered,
            Long submitTimeMillis, String callbackUrl) throws CoreException, ExecutorException {
        final BuildConfiguration configuration = buildConfigurationRepository.queryById(buildConfigurationId);
        IdRev idRev = new IdRev(buildConfigurationId, buildConfigurationRevision);
        log.debug("Querying for configurationAudited by idRev: " + idRev.toString());
        final BuildConfigurationAudited configurationAudited = buildConfigurationAuditedRepository.queryById(idRev);
        log.debug("Building configurationAudited " + configurationAudited.toString());
        log.debug("User triggered the process " + userTriggered.getUsername());

        Consumer<BuildCoordinationStatus> onComplete = (buildStatus) -> {
            if (callbackUrl != null && !callbackUrl.isEmpty()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmNotifier.signalBpmEvent(callbackUrl.toString() + "&event=" + buildStatus);
            }
        };

        Set<Integer> buildRecordSetIds = parseIntegers(buildRecordSetIdsCSV);

        Integer buildConfigSetRecordIdInt = null;
        if (buildConfigSetRecordId != null && !buildConfigSetRecordId.equals("") && !buildConfigSetRecordId.equals("null") ) {
            buildConfigSetRecordIdInt = Integer.parseInt(buildConfigSetRecordId);
        }


        String buildContentId = ContentIdentityManager.getBuildContentId(configuration.getName());

        BuildExecutionConfiguration buildExecutionConfig = new DefaultBuildExecutionConfiguration(
                buildTaskId, //TODO remove duplicate ID
                configuration,
                configurationAudited,
                buildContentId,
                userTriggered,
                buildTaskId);

        Consumer<BuildExecutionStatusChangedEvent> onExecutionStatusChange = (statusChangedEvent) -> {};
        BuildExecutionSession buildExecutionSession = buildExecutor.startBuilding(buildExecutionConfig, onExecutionStatusChange);

        return buildExecutionSession;
    }

    private Set<Integer> parseIntegers(String buildRecordSetIdsCSV) {
        if (buildRecordSetIdsCSV != null && !buildRecordSetIdsCSV.equals("") && !buildRecordSetIdsCSV.equals("null") ) {
            return Arrays.asList(buildRecordSetIdsCSV.split(",")).stream().map((s) -> Integer.parseInt(s)).collect(Collectors.toSet());
        } else {
            return null;
        }
    }

}
