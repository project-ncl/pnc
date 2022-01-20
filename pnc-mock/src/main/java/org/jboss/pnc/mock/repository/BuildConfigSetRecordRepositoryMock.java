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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;

import java.util.Date;
import java.util.List;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:06 PM
 */
public class BuildConfigSetRecordRepositoryMock extends IntIdRepositoryMock<BuildConfigSetRecord>
        implements BuildConfigSetRecordRepository {

    @Override
    public List<BuildConfigSetRecord> findTemporaryBuildConfigSetRecordsOlderThan(Date date) {
        return null;
    }
}
