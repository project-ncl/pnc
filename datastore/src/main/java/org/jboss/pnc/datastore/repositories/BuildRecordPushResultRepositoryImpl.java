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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildRecordPushResultSpringRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.spi.datastore.predicates.BuildRecordPushResultPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordPushResultRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Stateless
public class BuildRecordPushResultRepositoryImpl extends AbstractRepository<BuildRecordPushResult, Long>
        implements BuildRecordPushResultRepository {

    @Inject
    public BuildRecordPushResultRepositoryImpl(BuildRecordPushResultSpringRepository springRepository) {
        super(springRepository, springRepository);
    }

    @Override
    public BuildRecordPushResult getLatestForBuildRecord(Base32LongID buildRecordId) {
        List<BuildRecordPushResult> buildRecordPushResults = queryWithPredicates(
                BuildRecordPushResultPredicates.forBuildRecordOrderByIdDesc(buildRecordId));
        if (buildRecordPushResults == null || buildRecordPushResults.size() == 0) {
            return null;
        } else {
            return buildRecordPushResults.get(0);
        }
    }

    @Override
    public List<BuildRecordPushResult> getAllSuccessfulForBuildRecord(Base32LongID buildRecordId) {
        return queryWithPredicates(BuildRecordPushResultPredicates.successForBuildRecord(buildRecordId));
    }
}
