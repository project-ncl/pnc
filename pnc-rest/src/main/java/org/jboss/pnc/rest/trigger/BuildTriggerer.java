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
import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.exception.CoreException;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetCallBack;
import org.jboss.pnc.core.notifications.buildSetTask.BuildSetStatusNotifications;
import org.jboss.pnc.core.notifications.buildTask.BuildCallBack;
import org.jboss.pnc.core.notifications.buildTask.BuildStatusNotifications;
import org.jboss.pnc.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.jboss.pnc.spi.repositorymanager.RepositoryManagerException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.function.Consumer;

@Stateless
public class BuildTriggerer {

    private BuildCoordinator buildCoordinator;
    private BuildConfigurationRepository buildConfigurationRepository;
    private BuildConfigurationAuditedRepository buildConfigurationAuditedRepository;
    private BuildConfigurationSetRepository buildConfigurationSetRepository;
    private BuildSetStatusNotifications buildSetStatusNotifications;
    private BuildStatusNotifications buildStatusNotifications;

    @Deprecated //not meant for usage its only to make CDI happy
    public BuildTriggerer() {
    }

    @Inject
    public BuildTriggerer(final BuildCoordinator buildCoordinator,
                          final BuildConfigurationRepository buildConfigurationRepository,
                          final BuildConfigurationAuditedRepository buildConfigurationAuditedRepository,
                          final BuildConfigurationSetRepository buildConfigurationSetRepository,
                          BuildSetStatusNotifications buildSetStatusNotifications,
                          BuildStatusNotifications buildStatusNotifications) {
        this.buildCoordinator = buildCoordinator;
        this.buildConfigurationRepository = buildConfigurationRepository;
        this.buildConfigurationAuditedRepository = buildConfigurationAuditedRepository;
        this.buildConfigurationSetRepository= buildConfigurationSetRepository;
        this.buildSetStatusNotifications = buildSetStatusNotifications;
        this.buildStatusNotifications = buildStatusNotifications;
    }

    public int triggerBuilds( final Integer buildConfigurationId, User currentUser, URL callBackUrl)
            throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        Consumer<BuildStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            //TODO call-back JBPM engine to notify completion
            //callBackUrl.toString() + "&newStatus=" + statusEvent.getNewStatus();
        };

        int buildTaskId = triggerBuilds(buildConfigurationId, currentUser);
        buildStatusNotifications.subscribe(new BuildCallBack(buildTaskId, onStatusUpdate));
        return buildTaskId;
    }

    public int triggerBuilds( final Integer configurationId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfiguration configuration = buildConfigurationRepository.findOne(configurationId);
        configuration.setBuildConfigurationAudited(this.getLatestAuditedBuildConfiguration(configurationId));

        Preconditions.checkArgument(configuration != null, "Can't find configuration with given id=" + configurationId);

        final BuildRecordSet buildRecordSet = new BuildRecordSet();
        if (configuration.getProductVersions() != null  && !configuration.getProductVersions().isEmpty()) {
            ProductVersion productVersion = configuration.getProductVersions().iterator().next();
            buildRecordSet.setProductMilestone(productVersion.getCurrentProductMilestone());
        }

        Integer taskId = buildCoordinator.build(configuration, currentUser).getBuildConfiguration().getId();
        return taskId;
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser, URL callbackUrl)
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        Consumer<BuildSetStatusChangedEvent> onStatusUpdate = (statusChangedEvent) -> {
            //TODO call-back JBPM engine to notify completion
            //callBackUrl.toString() + "&newStatus=" + statusEvent.getNewStatus();
        };

        int buildSetTaskId = triggerBuildConfigurationSet(buildConfigurationSetId, currentUser);
        buildSetStatusNotifications.subscribe(new BuildSetCallBack(buildSetTaskId, onStatusUpdate));
        return buildSetTaskId;
    }

    public int triggerBuildConfigurationSet( final Integer buildConfigurationSetId, User currentUser )
        throws InterruptedException, CoreException, BuildDriverException, RepositoryManagerException
    {
        final BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(buildConfigurationSetId);
        Preconditions.checkArgument(buildConfigurationSet != null, "Can't find configuration with given id=" + buildConfigurationSetId);

        for (BuildConfiguration config : buildConfigurationSet.getBuildConfigurations()) {
            config.setBuildConfigurationAudited(this.getLatestAuditedBuildConfiguration(config.getId()));
        }

        return buildCoordinator.build(buildConfigurationSet, currentUser).getId();
    }

    /**
     * Get the latest audited revision for the given build configuration ID
     * 
     * @param buildConfigurationId
     * @return The latest revision of the given build configuration
     */
    private BuildConfigurationAudited getLatestAuditedBuildConfiguration(Integer buildConfigurationId) {
        List<BuildConfigurationAudited> buildConfigRevs = buildConfigurationAuditedRepository.findAllByIdOrderByRevDesc(buildConfigurationId);
        if ( buildConfigRevs.isEmpty() ) {
            // TODO should we throw an exception?  This should never happen.
            return null;
        }
        return buildConfigRevs.get(0);
    }
}
