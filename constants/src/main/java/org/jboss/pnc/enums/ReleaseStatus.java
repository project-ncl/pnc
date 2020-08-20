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
package org.jboss.pnc.enums;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/26/16 Time: 2:39 PM
 */
public enum ReleaseStatus {
    SUCCESS(MilestoneCloseStatus.SUCCEEDED),
    FAILURE(MilestoneCloseStatus.FAILED),
    IMPORT_ERROR(MilestoneCloseStatus.FAILED),
    SET_UP_ERROR(MilestoneCloseStatus.SYSTEM_ERROR);

    private final MilestoneCloseStatus milestoneReleaseStatus;

    ReleaseStatus(MilestoneCloseStatus milestoneReleaseStatus) {
        this.milestoneReleaseStatus = milestoneReleaseStatus;
    }

    public MilestoneCloseStatus getMilestoneReleaseStatus() {
        return milestoneReleaseStatus;
    }
}
