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

import com.google.common.base.Preconditions;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.spi.datastore.repositories.*;
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
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordSetPredicates.withBuildRecordId;
import static org.jboss.pnc.spi.datastore.predicates.BuildRecordSetPredicates.withPerformedInProductMilestoneId;

@Stateless
public class BuildRecordSetProvider {

    private BuildRecordSetRepository buildRecordSetRepository;

    private ProductMilestoneRepository productMilestoneRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    public BuildRecordSetProvider() {
    }

    @Inject
    public BuildRecordSetProvider(BuildRecordSetRepository buildRecordSetRepository,
            ProductMilestoneRepository productMilestoneRepository,
            RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.buildRecordSetRepository = buildRecordSetRepository;
        this.productMilestoneRepository = productMilestoneRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    private Function<BuildRecordSet, BuildRecordSetRest> toRestModel() {
        return buildRecordSet -> new BuildRecordSetRest(buildRecordSet);
    }

    public List<BuildRecordSetRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<BuildRecordSet> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecordSet.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordSetRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public BuildRecordSetRest getSpecific(Integer id) {
        BuildRecordSet buildRecordSet = buildRecordSetRepository.queryById(id);
        if (buildRecordSet != null) {
            return new BuildRecordSetRest(buildRecordSet);
        }
        return null;
    }

    public Integer store(BuildRecordSetRest buildRecordSetRest) {
        Preconditions.checkArgument(buildRecordSetRest.getId() == null, "Id must be null");
        BuildRecordSet buildRecordSet = buildRecordSetRest.toBuildRecordSet();

        buildRecordSet = buildRecordSetRepository.save(buildRecordSet);

        return buildRecordSet.getId();
    }

    public Integer update(Integer id, BuildRecordSetRest buildRecordSetRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(buildRecordSetRest.getId() == null || buildRecordSetRest.getId().equals(id),
                "Entity id does not match the id to update");
        buildRecordSetRest.setId(id);
        BuildRecordSet buildRecordSet = buildRecordSetRepository.queryById(buildRecordSetRest.getId());
        Preconditions.checkArgument(buildRecordSet != null,
                "Couldn't find buildRecordSet with id " + buildRecordSetRest.getId());
        buildRecordSet = buildRecordSetRepository.save(buildRecordSetRest.toBuildRecordSet());
        return buildRecordSet.getId();
    }

    public void delete(Integer buildRecordSetId) {
        buildRecordSetRepository.delete(buildRecordSetId);
    }

    public List<BuildRecordSetRest> getAllForPerformedInProductMilestone(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer milestoneId) {
        Predicate<BuildRecordSet> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecordSet.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordSetRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withPerformedInProductMilestoneId(milestoneId)))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public List<BuildRecordSetRest> getAllForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer recordId) {
        Predicate<BuildRecordSet> rsqlPredicate = rsqlPredicateProducer.getPredicate(BuildRecordSet.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(buildRecordSetRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate, withBuildRecordId(recordId)))
                .map(toRestModel())
                .collect(Collectors.toList());

    }

}
