/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Deprecated
public class BuildStatusChangedEventRest implements BuildStatusChangedEvent {

    private BuildStatus oldStatus;
    private BuildStatus newStatus;
    private Build build;

    public void setOldStatus(BuildStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public void setNewStatus(BuildStatus newStatus) {
        this.newStatus = newStatus;
    }

    public void setBuild(Build build) {
        this.build = build;
    }

    @Override
    public BuildStatus getOldStatus() {
        return oldStatus;
    }

    @Override
    public BuildStatus getNewStatus() {
        return newStatus;
    }

    @Override
    public Build getBuild() {
        return build;
    }
}