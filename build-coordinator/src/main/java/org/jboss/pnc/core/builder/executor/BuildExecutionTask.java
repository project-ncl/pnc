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

package org.jboss.pnc.core.builder.executor;

import org.jboss.pnc.core.builder.datastore.BuildConfigurationUtils;
import org.jboss.pnc.core.content.ContentIdentityManager;
import org.jboss.pnc.core.events.DefaultBuildStatusChangedEvent;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.BuildExecution;
import org.jboss.pnc.spi.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Event;
import java.net.URI;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionTask implements BuildExecution {

    public static final Logger log = LoggerFactory.getLogger(BuildExecutionTask.class);

    private int id;
    private BuildConfiguration buildConfiguration;
    private BuildConfigurationAudited buildConfigurationAudited;
    private BuildStatus status;
    private Date startTime;
    private Date endTime;
    private final AtomicReference<URI> logsWebSocketLink = new AtomicReference<>();
    private String topContentId;
    private String buildContentId;
    private User user;
    private Set<Integer> buildRecordSetIds;
    private Integer buildConfigSetRecordId;
    private Optional<Event<BuildStatusChangedEvent>> buildStatusChangedEventNotifier; //TODO decouple event notifications
    private Integer buildTaskId;
    private boolean failed = false;
    //BuildTask.submitTime
    private Date submitTime;

    public BuildExecutionTask(int id, BuildConfiguration buildConfiguration, BuildConfigurationAudited buildConfigurationAudited, String topContentId, String buildContentId, User user, Set<Integer> buildRecordSetIds, Integer buildConfigSetRecordId, Optional<Event<BuildStatusChangedEvent>> buildStatusChangedEventNotifier, Integer buildTaskId, Date submitTime) {
        this.id = id;
        this.buildConfiguration = buildConfiguration;
        this.buildConfigurationAudited = buildConfigurationAudited;
        this.topContentId = topContentId;
        this.buildContentId = buildContentId;
        this.user = user;
        this.buildRecordSetIds = Optional.ofNullable(buildRecordSetIds).orElse(new HashSet<>());
        this.buildConfigSetRecordId = buildConfigSetRecordId;
        this.buildStatusChangedEventNotifier = buildStatusChangedEventNotifier;
        this.buildTaskId = buildTaskId;
        this.submitTime = submitTime;
    }

    public void setStatus(BuildStatus status) {
        IdRev idRev = buildConfigurationAudited.getId();
        BuildStatusChangedEvent buildStatusChanged = new DefaultBuildStatusChangedEvent(this.status, status, buildTaskId,
                idRev.getId(),
                user.getId());
        log.debug("Updating build execution task {} status to {}. Task is linked to coordination task {}.", id, buildStatusChanged, buildTaskId);
        this.status = status;
        if (status.hasFailed()) {
            failed = true;
        }
        buildStatusChangedEventNotifier.ifPresent((notifier) -> notifier.fire(buildStatusChanged));
    }

    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public int getId() {
        return id;
    }

    public boolean hasFailed() {
        return failed;
    }

    public BuildStatus getStatus() {
        return status;
    }

    @Override
    public String getTopContentId() {
        return topContentId;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    @Override
    public String getProjectName() {
        return buildConfigurationAudited.getProject().getName();
    }

    @Override
    public void setLogsWebSocketLink(URI link) {
        logsWebSocketLink.set(link);
    }

    @Override
    public void clearLogsWebSocketLink() {
        logsWebSocketLink.set(null);
    }

    @Override
    public Optional<URI> getLogsWebSocketLink() {
        return Optional.ofNullable(logsWebSocketLink.get());
    }

    @Override
    public boolean isPartOfBuildSet() { //TODO remove, we are not promoting a build set
        return false;
    }

    @Override
    public String getBuildSetContentId() { //TODO remove, we are not promoting a build set
        return "";
    }

    public User getUser() {
        return user;
    }

    /**
     * @deprecated Avoid using reference to buildSet from build execution
     */
    @Deprecated
    public Set<Integer> getBuildRecordSetIds() {
        return buildRecordSetIds;
    }

    public static BuildExecutionTask build (
            int buildExecutionTaskId,
            BuildConfiguration buildConfiguration,
            BuildConfigurationAudited buildConfigAudited,
            User user,
            Set<Integer> buildRecordSetIds,
            Integer buildConfigSetRecordId,
            Optional<Event<BuildStatusChangedEvent>> buildStatusChangedEventNotifier,
            Integer buildTaskId,
            Date submitTime) {
        String topContentId = ContentIdentityManager.getProductContentId(BuildConfigurationUtils.getFirstProductVersion(buildConfiguration)); //TODO is first always the correct one ?
        String buildContentId = ContentIdentityManager.getBuildContentId(buildConfiguration);

        return new BuildExecutionTask(
                buildExecutionTaskId,
                buildConfiguration,
                buildConfigAudited,
                topContentId,
                buildContentId,
                user,
                buildRecordSetIds,
                buildConfigSetRecordId,
                buildStatusChangedEventNotifier,
                buildTaskId,
                submitTime);
    }

    public Integer getBuildConfigSetRecordId() {
        return buildConfigSetRecordId;
    }

    public Date getSubmitTime() {
        return submitTime;
    }
}
