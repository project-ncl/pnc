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
package org.jboss.pnc.spi.coordinator.events;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.events.BuildStatusChangedEvent;

public class DefaultBuildStatusChangedEvent implements BuildStatusChangedEvent {

    private final BuildStatus oldStatus;
    private final BuildStatus newStatus;
    private final Build build;

    public DefaultBuildStatusChangedEvent(Build build, BuildStatus oldStatus, BuildStatus newStatus) {
        this.build = build;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
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

    @Override
    public String toString() {

        return "DefaultBuildStatusChangedEvent{" + "oldStatus=" + oldStatus + ", newStatus=" + newStatus + ", build="
                + build + '}';
    }
}
