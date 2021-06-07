/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;

import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildRecordPushResultRepositoryMock extends LongIdRepositoryMock<BuildRecordPushResult>
        implements BuildRecordPushResultRepository {

    @Override
    public BuildRecordPushResult getLatestForBuildRecord(Base32LongID buildRecordId) {
        return data.stream()
                .filter(buildRecordPushResult -> buildRecordPushResult.getBuildRecord().getId().equals(buildRecordId))
                .sorted(Comparator.comparing(BuildRecordPushResult::getId).reversed())
                .findFirst()
                .get();
    }

    @Override
    public List<BuildRecordPushResult> getAllSuccessfulForBuildRecord(Base32LongID buildRecordId) {
        throw new RuntimeException("Not implemented!");
    }

}
