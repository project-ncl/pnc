/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.mapper.api.BuildMapper;
import org.jboss.pnc.facade.providers.api.BuildProvider;
import org.jboss.pnc.facade.rsql.mapper.BuildRSQLMapper;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

import static org.jboss.pnc.spi.datastore.predicates.BuildRecordPredicates.withPerformedInMilestone;

@Stateless
public class BuildProviderImpl extends AbstractProvider<BuildRecord, Build, BuildRef> implements BuildProvider {

    @Inject
    public BuildProviderImpl(BuildRecordRepository repository, BuildMapper mapper, BuildRSQLMapper rsql) {
        super(repository, mapper, rsql);
    }

    @Override
    public Page<Build> getPerformedBuildsForMilestone(int pageIndex,
                                                      int pageSize,
                                                      String sortingRsql,
                                                      String query,
                                                      Integer milestoneId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withPerformedInMilestone(milestoneId));
    }
}
