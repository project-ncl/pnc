package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.datastore.repositories.ProductVersionRepository;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildRecordSetPredicates.withBuildRecordId;
import static org.jboss.pnc.datastore.predicates.BuildRecordSetPredicates.withProductVersionId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildRecordSetProvider {

    private BuildRecordSetRepository buildRecordSetRepository;
    private ProductVersionRepository productVersionRepository;

    public BuildRecordSetProvider() {
    }

    @Inject
    public BuildRecordSetProvider(BuildRecordSetRepository buildRecordSetRepository,
            ProductVersionRepository productVersionRepository) {
        this.buildRecordSetRepository = buildRecordSetRepository;
        this.productVersionRepository = productVersionRepository;
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
        ProductVersion productVersion = null;

        if (buildRecordSet.getProductVersion() != null) {
            productVersion = productVersionRepository.findOne(buildRecordSet.getProductVersion().getId());
            buildRecordSet.setProductVersion(productVersion);
        }

        buildRecordSet = buildRecordSetRepository.saveAndFlush(buildRecordSet);

        if (productVersion != null) {
            productVersion.setBuildRecordSet(buildRecordSet);
            productVersionRepository.saveAndFlush(productVersion);
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

    public List<BuildRecordSetRest> getAllForProductVersion(int pageIndex, int pageSize, String sortingRsql,
            String query, Integer versionId) {

        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildRecordSet.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(
                buildRecordSetRepository.findAll(withProductVersionId(versionId).and(filteringCriteria.get()), paging)).map(
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
