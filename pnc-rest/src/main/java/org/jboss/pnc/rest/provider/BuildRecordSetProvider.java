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

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.datastore.repositories.ProductReleaseRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.model.ProductRelease;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildRecordSetPredicates.withBuildRecordId;
import static org.jboss.pnc.datastore.predicates.BuildRecordSetPredicates.withProductMilestoneId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildRecordSetProvider {

    private BuildRecordSetRepository buildRecordSetRepository;
    private ProductMilestoneRepository productMilestoneRepository;
    private ProductReleaseRepository productReleaseRepository;

    public BuildRecordSetProvider() {
    }

    @Inject
    public BuildRecordSetProvider(BuildRecordSetRepository buildRecordSetRepository,
            ProductMilestoneRepository productMilestoneRepository,
            ProductReleaseRepository productReleaseRepository) {
        this.buildRecordSetRepository = buildRecordSetRepository;
        this.productMilestoneRepository = productMilestoneRepository;
        this.productReleaseRepository = productReleaseRepository;
    }

    private Function<BuildRecordSet, BuildRecordSetRest> toRestModel() {
        return buildRecordSet -> new BuildRecordSetRest(buildRecordSet);
    }

    public List<BuildRecordSetRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecordSet.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildRecordSetRepository.findAll(filteringCriteria.get(), paging)).map(toRestModel()).collect(
                Collectors.toList());
    }

    public BuildRecordSetRest getSpecific(Integer id) {
        BuildRecordSet buildRecordSet = buildRecordSetRepository.findOne(id);
        if (buildRecordSet != null) {
            return new BuildRecordSetRest(buildRecordSet);
        }
        return null;
    }

    public Integer store(BuildRecordSetRest buildRecordSetRest) {
        Preconditions.checkArgument(buildRecordSetRest.getId() == null, "Id must be null");
        BuildRecordSet buildRecordSet = buildRecordSetRest.toBuildRecordSet();
        ProductMilestone productMilestone = null;
        ProductRelease productRelease = null;

        if (buildRecordSet.getProductRelease() != null) {
            productMilestone = productMilestoneRepository.findOne(buildRecordSet.getProductMilestone().getId());
            productRelease = productReleaseRepository.findOne(buildRecordSet.getProductRelease().getId());
            buildRecordSet.setProductRelease(productRelease);
        }

        buildRecordSet = buildRecordSetRepository.saveAndFlush(buildRecordSet);

        if (productMilestone != null) {
            productMilestone.setBuildRecordSet(buildRecordSet);
            productMilestoneRepository.saveAndFlush(productMilestone);
        }

        if (productRelease != null) {
            productRelease.setBuildRecordSet(buildRecordSet);
            productReleaseRepository.saveAndFlush(productRelease);
        }

        return buildRecordSet.getId();
    }

    public Integer update(Integer id, BuildRecordSetRest buildRecordSetRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(buildRecordSetRest.getId() == null || buildRecordSetRest.getId().equals(id),
                "Entity id does not match the id to update");
        buildRecordSetRest.setId(id);
        BuildRecordSet buildRecordSet = buildRecordSetRepository.findOne(buildRecordSetRest.getId());
        Preconditions.checkArgument(buildRecordSet != null,
                "Couldn't find buildRecordSet with id " + buildRecordSetRest.getId());
        buildRecordSet = buildRecordSetRepository.save(buildRecordSetRest.toBuildRecordSet());
        return buildRecordSet.getId();
    }

    public void delete(Integer buildRecordSetId) {
        buildRecordSetRepository.delete(buildRecordSetId);
    }

    public List<BuildRecordSetRest> getAllForProductMilestone(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer milestoneId) {

        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecordSet.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(
                buildRecordSetRepository.findAll(withProductMilestoneId(milestoneId).and(filteringCriteria.get()), paging)).map(
                buildRecordSet -> new BuildRecordSetRest(buildRecordSet)).collect(Collectors.toList());
    }

    public List<BuildRecordSetRest> getAllForBuildRecord(int pageIndex, int pageSize, String sortingRsql, String query,
            Integer recordId) {

        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecordSet.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(
                buildRecordSetRepository.findAll(withBuildRecordId(recordId).and(filteringCriteria.get()), paging)).map(
                buildRecordSet -> new BuildRecordSetRest(buildRecordSet)).collect(Collectors.toList());

    }

}
