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
package org.jboss.pnc.bpm.model.causeway;

import org.jboss.pnc.bpm.model.BpmEvent;
import org.jboss.pnc.enums.ReleaseStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/25/16 Time: 7:34 AM
 */
@Data
public class MilestoneReleaseResultRest extends BpmEvent {
    private Integer milestoneId;
    private ReleaseStatus releaseStatus;
    private String errorMessage;

    private List<BuildImportResultRest> builds = new ArrayList<>();

    @JsonIgnore
    public boolean isSuccessful() {
        return !builds.isEmpty() && allBuildsSuccessful();
    }

    private boolean allBuildsSuccessful() {
        return builds.stream().allMatch(r -> r.getStatus() == BuildImportStatus.SUCCESSFUL);
    }
}
