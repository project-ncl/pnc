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
package org.jboss.pnc.dto.internal.bpm;

import org.jboss.pnc.dto.BuildPushResult;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.enums.ReleaseStatus;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeName;

import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonTypeName(value = MilestoneReleaseResult.BREW_IMPORT_SUCCESS)
public class MilestoneReleaseResult extends BPMNotification {
    static final String BREW_IMPORT_SUCCESS = "BREW_IMPORT_SUCCESS";

    private final Integer milestoneId;
    private final ReleaseStatus releaseStatus;
    private final String errorMessage;

    private final List<BuildPushResult> builds;

    public MilestoneReleaseResult(Integer milestoneId, ReleaseStatus releaseStatus, String errorMessage, List<BuildPushResult> builds) {
        super(BREW_IMPORT_SUCCESS);
        this.milestoneId = milestoneId;
        this.releaseStatus = releaseStatus;
        this.errorMessage = errorMessage;
        this.builds = builds;
    }

    @JsonIgnore
    public boolean isSuccessful() {
        return !builds.isEmpty() && allBuildsSuccessful();
    }

    private boolean allBuildsSuccessful() {
        return builds.stream().allMatch(r -> r.getStatus() == BuildPushStatus.SUCCESS);
    }
}
