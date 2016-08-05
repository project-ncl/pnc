/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.jboss.pnc.datastore.repositories.internal.BuildRecordSpringRepository;
import org.jboss.pnc.datastore.repositories.internal.PageableMapper;
import org.jboss.pnc.datastore.repositories.internal.SpecificationsMapper;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

@Stateless
public class BuildRecordRepositoryImpl extends AbstractRepository<BuildRecord, Integer> implements BuildRecordRepository {

    private BuildRecordSpringRepository repository;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildRecordRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildRecordRepositoryImpl(BuildRecordSpringRepository buildRecordSpringRepository) {
        super(buildRecordSpringRepository, buildRecordSpringRepository);
        repository = buildRecordSpringRepository;
    }

    @Override
    public BuildRecord findByIdFetchAllProperties(Integer id) {
        return repository.findByIdFetchAllProperties(id);
    }


    @Override
    public List<BuildRecord> queryWithPredicatesUsingCursor(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildRecord>... predicates) {
        return repository.findAll(SpecificationsMapper.map(predicates), PageableMapper.mapCursored(pageInfo, sortInfo)).getContent();
       // return springSpecificationsExecutor.findAll(SpecificationsMapper.map(predicates), PageableMapper.mapCursored(pageInfo, sortInfo));
    }



}
