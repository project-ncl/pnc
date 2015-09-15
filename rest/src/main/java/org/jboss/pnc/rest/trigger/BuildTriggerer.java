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
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.model.*;
import org.jboss.pnc.rest.utils.BpmCallback;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.datastore.DatastoreException;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.net.URL;
import java.util.function.Consumer;

@Stateless
public class BuildTriggerer {

    private final Logger log = Logger.getLogger(BuildTriggerer.class);
    
    private BuildCoordinator buildCoordinator;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    private BuildSetStatusNotifications buildSetStatusNotifications;
    private BuildStatusNotifications buildStatusNotifications;
    private BpmCallback bpmCallback;

    private SortInfoProducer sortInfoProducer;

    @Deprecated //not meant for usage its only to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator, final BuildConfigurationRepository buildConfigurationRepository,
                          final BuildConfigurationSetRepository buildConfigurationSetRepository,
                          BuildSetStatusNotifications buildSetStatusNotifications, BuildStatusNotifications buildStatusNotifications,
                          BpmCallback bpmCallback, SortInfoProducer sortInfoProducer) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationSetRepository= buildConfigurationSetRepository;
        this.buildSetStatusNotifications = buildSetStatusNotifications;
        this.buildStatusNotifications = buildStatusNotifications;
        this.bpmCallback = bpmCallback;
        this.sortInfoProducer = sortInfoProducer;
    }

    public int triggerBuild( final Integer buildConfigurationId, User currentUser, URL callBackUrl)
            throws BuildConflictException
    {
        Consumer<BuildStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if(statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmCallback.signalBpmEvent(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        int buildTaskId = triggerBuild(buildConfigurationId, currentUser);
        buildStatusNotifications.subscribe(new BuildCallBack(buildTaskId, onStatusUpdate));
        return buildTaskId;
    }

    public int triggerBuild( final Integer configurationId, User currentUser ) throws BuildConflictException
    {
        final BuildConfiguration configuration = buildConfigurationRepository.queryById(configurationId);
        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        if (configuration.getProductVersions() != null  && !configuration.getProductVersions().isEmpty()) {
            ProductVersion productVersion = configuration.getProductVersions().iterator().next();
            buildRecordSet.setPerformedInProductMilestone(productVersion.getCurrentProductMilestone());
        }

        Integer taskId = buildCoordinator.build(configuration, currentUser, false).getId();
        return taskId;
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser, URL callBackUrl)
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException
    {
        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            if(statusChangedEvent.getNewStatus().isCompleted()) {
                // Expecting URL like: http://host:port/business-central/rest/runtime/org.test:Test1:1.0/process/instance/7/signal?signal=testSig
                bpmCallback.signalBpmEvent(callBackUrl.toString() + "&event=" + statusChangedEvent.getNewStatus());
            }
        };

        int buildSetTaskId = triggerBuildConfigurationSet(buildConfigurationSetId, currentUser);
        buildSetStatusNotifications.subscribe(new BuildSetCallBack(buildSetTaskId, onStatusUpdate));
        return buildSetTaskId;
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException, DatastoreException
    {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.queryById(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null, "Can't find configuration with given id=" + buildConfigurationSetId);

        return buildCoordinator.build(buildConfigurationSet, currentUser).getId();
    }


}
