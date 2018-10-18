/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.restmodel.causeway;

import org.jboss.pnc.enums.MilestoneReleaseStatus;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 8/26/16
 * Time: 2:39 PM
 */
public enum ReleaseStatus {
    SUCCESS(MilestoneReleaseStatus.SUCCEEDED),
    IMPORT_ERROR(MilestoneReleaseStatus.FAILED),
    SET_UP_ERROR(MilestoneReleaseStatus.SYSTEM_ERROR);

    private final MilestoneReleaseStatus milestoneReleaseStatus;


    ReleaseStatus(MilestoneReleaseStatus milestoneReleaseStatus) {
        this.milestoneReleaseStatus = milestoneReleaseStatus;
    }

    public MilestoneReleaseStatus getMilestoneReleaseStatus() {
        return milestoneReleaseStatus;
    }
}
