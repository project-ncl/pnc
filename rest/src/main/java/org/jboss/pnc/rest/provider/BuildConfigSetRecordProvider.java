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

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.rest.restmodel.BuildRecordRest;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigSetRecordProvider {

    private BuildConfigSetRecordRepository buildConfigSetRecordRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    public BuildConfigSetRecordProvider() {
    }

    @Inject
    public BuildConfigSetRecordProvider(BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer) {
        this.buildConfigSetRecordRepository = buildConfigSetRecordRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public List<BuildConfigSetRecordRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<BuildConfigSetRecord> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildConfigSetRecord.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildConfigSetRecordRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigSetRecordRest getSpecific(Integer id) {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(id);
        if (buildConfigSetRecord != null) {
            return new BuildConfigSetRecordRest(buildConfigSetRecord);
        }
        return null;
    }

    public List<BuildRecordRest> getBuildRecords(int pageIndex, int pageSize, String sortingRsql, String query, Integer buildConfigSetId) {
        BuildConfigSetRecord buildConfigSetRecord = buildConfigSetRecordRepository.queryById(buildConfigSetId);
        return nullableStreamOf(buildConfigSetRecord.getBuildRecords())
                .map(buildRecord -> new BuildRecordRest(buildRecord))
                .collect(Collectors.toList());
    }

    public Function<? super BuildConfigSetRecord, ? extends BuildConfigSetRecordRest> toRestModel() {
        return buildConfigSetRecord -> new BuildConfigSetRecordRest(buildConfigSetRecord);
    }
}
