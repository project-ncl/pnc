/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integrationrex.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.EnumSet;

import org.jboss.pnc.api.enums.RebuildMode;
import org.jboss.pnc.client.BuildClient;
import org.jboss.pnc.client.ClientException;
import org.jboss.pnc.client.GroupBuildClient;
import org.jboss.pnc.client.RemoteResourceNotFoundException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildUtils {

    private static final Logger logger = LoggerFactory.getLogger(BuildUtils.class);
    private BuildClient buildClient;
    private GroupBuildClient groupBuildClient;

    public BuildUtils(BuildClient buildClient, GroupBuildClient groupBuildClient) {
        this.buildClient = buildClient;
        this.groupBuildClient = groupBuildClient;
    }

    public BuildParameters getTemporaryParameters() {
        return getBuildParameters(true, false);
    }

    public BuildParameters getPersistentParameters() {
        return getBuildParameters(false, false);
    }

    public BuildParameters getTemporaryParameters(boolean force) {
        return getBuildParameters(true, force);
    }

    public BuildParameters getPersistentParameters(boolean force) {
        return getBuildParameters(false, force);
    }

    public BuildParameters getBuildParameters(boolean temporary, boolean force) {
        BuildParameters buildParameters = new BuildParameters();

        buildParameters.setTemporaryBuild(temporary);
        buildParameters.setBuildDependencies(true);
        if (force)
            buildParameters.setRebuildMode(RebuildMode.FORCE);

        return buildParameters;
    }

    public Boolean buildToFinish(String buildId) {
        return buildToFinish(buildId, null, null);
    }

    public Boolean buildToFinish(String buildId, EnumSet<BuildStatus> isIn, EnumSet<BuildStatus> isNotIn) {
        Build build = null;
        logger.debug("Waiting for build {} to finish", buildId);
        try {
            build = buildClient.getSpecific(buildId);
            assertThat(build).isNotNull();
            logger.debug("Gotten build with status: {}", build.getStatus());
            if (!build.getStatus().isFinal())
                return false;
        } catch (RemoteResourceNotFoundException e) {
            fail(String.format("Build with id:%s not present", buildId), e);
        } catch (ClientException e) {
            fail("Client has failed in an unexpected way.", e);
        }
        assertThat(build).isNotNull();
        assertThat(build.getStatus()).isNotNull();
        if (isIn != null && !isIn.isEmpty())
            assertThat(build.getStatus()).isIn(isIn);
        if (isNotIn != null && !isNotIn.isEmpty())
            assertThat(build.getStatus()).isNotIn(isNotIn);
        return true;
    }

    public Boolean groupBuildToFinish(String id) {
        return groupBuildToFinish(id, null, null);
    }

    public Boolean groupBuildToFinish(String groupBuildId, EnumSet<BuildStatus> isIn, EnumSet<BuildStatus> isNotIn) {
        if (isIn == null)
            isIn = EnumSet.noneOf(BuildStatus.class);
        if (isNotIn == null)
            isNotIn = EnumSet.noneOf(BuildStatus.class);

        GroupBuild build = null;
        logger.debug("Waiting for build {} to finish", groupBuildId);
        try {
            build = groupBuildClient.getSpecific(groupBuildId);
            assertThat(build).isNotNull();
            logger.debug("Gotten build with status: {}", build.getStatus());
            if (!build.getStatus().isFinal())
                return false;
        } catch (RemoteResourceNotFoundException e) {
            fail(String.format("Group Build with id:%s not present", groupBuildId), e);
        } catch (ClientException e) {
            fail("Client has failed in an unexpected way.", e);
        }
        assertThat(build.getStatus()).isNotIn(isNotIn).isIn(isIn);
        return true;
    }
}
