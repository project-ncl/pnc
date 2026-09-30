/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.mock;

import javax.enterprise.context.Dependent;

import org.jboss.pnc.coordinator.maintenance.RemoteBuildsCleaner;
import org.jboss.pnc.enums.ResultStatus;
import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.coordinator.Result;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class RemoteBuildsCleanerMock implements RemoteBuildsCleaner {

    @Override
    public Result deleteRemoteBuilds(BuildRecord buildRecord) {
        return new Result(BuildMapper.idMapper.toDto(buildRecord.getId()), ResultStatus.SUCCESS);
    }
}
