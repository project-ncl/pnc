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
package org.jboss.pnc.rest.provider;

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.BuildConfigSetRecordRest;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigSetRecordPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigSetRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

@Stateless
public class BuildConfigSetRecordProvider extends AbstractProvider<BuildConfigSetRecord, BuildConfigSetRecordRest> {

    public BuildConfigSetRecordProvider() {
    }

    @Inject
    public BuildConfigSetRecordProvider(
            BuildConfigSetRecordRepository buildConfigSetRecordRepository,
            RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer,
            PageInfoProducer pageInfoProducer) {
        super(buildConfigSetRecordRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    @Override
    protected Function<? super BuildConfigSetRecord, ? extends BuildConfigSetRecordRest> toRESTModel() {
        return buildConfigSetRecord -> new BuildConfigSetRecordRest(buildConfigSetRecord);
    }

    @Override
    protected Function<? super BuildConfigSetRecordRest, ? extends BuildConfigSetRecord> toDBModel() {
        throw new UnsupportedOperationException("Not supported by this provider");
    }

    public CollectionInfo<BuildConfigSetRecordRest> getAllForBuildConfigSet(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String rsql,
            Integer buildConfigSetId) {
        return queryForCollection(
                pageIndex,
                pageSize,
                sortingRsql,
                rsql,
                BuildConfigSetRecordPredicates.withBuildConfigSetId(buildConfigSetId));
    }
}
