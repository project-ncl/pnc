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
package org.jboss.pnc.rest.provider;

import com.google.common.base.Strings;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.core.builder.BuildTask;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.StreamingOutput;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildConfigSetRecordPredicates.*;
import static org.jboss.pnc.datastore.predicates.BuildRecordPredicates.withBuildConfigurationId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigSetRecordProvider {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    public BuildConfigSetRecordProvider() {
    }

    @Inject
    public BuildConfigSetRecordProvider(BuildConfigSetRecordRepository buildConfigSetRecordRepository) {
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
    }

    public List<BuildConfigSetRecordRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfigSetRecord.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildConfigSetRecordRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigSetRecordRest getSpecific(Integer id) {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.findOne(id);
        if (buildConfigSetRecord != null) {
            return new BuildConfigSetRecordRest(buildConfigSetRecord);
        }
        return null;
    }

    public List<BuildRecordRest> getBuildRecords(int pageIndex, int pageSize, String sortingRsql, String query, Integer buildConfigSetId) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecord.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.findOne(buildConfigSetId);
        return nullableStreamOf(buildConfigSetRecord.getBuildRecords())
                .map(buildRecord -> new BuildRecordRest(buildRecord))
                .collect(Collectors.toList());
    }

    public Function<? super BuildConfigSetRecord, ? extends BuildConfigSetRecordRest> toRestModel() {
        return buildConfigSetRecord -> new BuildConfigSetRecordRest(buildConfigSetRecord);
    }
}
