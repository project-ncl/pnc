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
package org.jboss.pnc.facade;

import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.model.Base32LongID;

import java.util.Set;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface BrewPusher {

    Set<BuildPushOperation> pushGroup(String id, String tagPrefix);

    BuildPushOperation pushBuild(String id, BuildPushParameters buildPushParameters);

    BuildPushOperation pushBuild(Base32LongID id, BuildPushParameters buildPushParameters, String milestoneId);

    void cancelPushOfBuild(String buildId);

    void cancelPushOfMilestone(String milestoneId);

    void brewPushComplete(String buildId, BuildPushCompleted buildPushResult);

    /**
     * Gets generated in progress brew push result or the latest completed one. If there is one in progress for given
     * build id, it takes priority over a completed one. For an in progress it generates an empty result with status
     * {@link BuildPushStatus#ACCEPTED} meaning that pusher accepted the push request.
     *
     * @param buildId build record id
     * @return generated or loaded push result, {@code null} in case there is no completed nor in progress
     */
    @Deprecated
    BuildPushReport getBrewPushResult(String buildId);

    BuildPushReport getBrewPushReport(String operationId);

}
