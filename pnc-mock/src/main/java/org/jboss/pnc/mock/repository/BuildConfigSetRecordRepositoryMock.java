/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:06 PM
 */
public class BuildConfigSetRecordRepositoryMock extends Base32LongIdRepositoryMock<BuildConfigSetRecord>
        implements BuildConfigSetRecordRepository {

    @Override
    public List<BuildConfigSetRecord> findTemporaryBuildConfigSetRecordsOlderThan(Date date) {
        return null;
    }

    @Override
    public List<BuildConfigSetRecord> findBuildConfigSetRecordsInProgress() {
        return data.stream().filter(r -> !r.getStatus().isFinal()).collect(Collectors.toList());
    }
}
