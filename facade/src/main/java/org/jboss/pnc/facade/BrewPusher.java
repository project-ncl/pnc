/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade;

import java.util.Set;

import org.jboss.pnc.api.causeway.dto.push.BuildPushCompleted;
import org.jboss.pnc.dto.BuildPushOperation;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.requests.BuildPushParameters;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.model.Base32LongID;

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
