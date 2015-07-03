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
package org.jboss.pnc.rest.debug;

import org.jboss.pnc.spi.BuildSetStatus;
import org.jboss.pnc.spi.events.BuildSetStatusChangedEvent;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class BuildStatusSetChangedEventRest implements BuildSetStatusChangedEvent {

    private BuildSetStatus oldStatus;
    private BuildSetStatus newStatus;
    private Integer buildSetTaskId;
    private Integer userId;

    public void setOldStatus(BuildSetStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(BuildSetStatus newStatus) {
        this.newStatus = newStatus;
    }

    public void setBuildSetTaskId(Integer buildSetTaskId) {
        this.buildSetTaskId = buildSetTaskId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public BuildSetStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildSetStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public Integer getBuildSetTaskId() {
        return buildSetTaskId;
    }

    @Override
    public Integer getUserId() {
        return userId;
    }
}