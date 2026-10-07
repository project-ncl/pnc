/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade;

import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.BuildRequestException;
import org.jboss.pnc.spi.exception.CoreException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
public interface BuildTriggerer {

    String triggerBuild(int buildConfigId, OptionalInt rev, BuildOptions buildOptions)
            throws BuildConflictException, CoreException, BuildRequestException;

    String triggerGroupBuild(int groupConfigId, Optional<GroupBuildRequest> revs, BuildOptions buildOptions)
            throws BuildConflictException, CoreException, BuildRequestException;

    /**
     * Cancels a running build
     *
     * @param buildId ID of a running build
     * @return True if the cancel request is successfully accepted, false if if there is no running build with such ID
     * @throws CoreException Thrown if cancellation fails due to any internal error
     */
    boolean cancelBuild(String buildId) throws CoreException;
}
