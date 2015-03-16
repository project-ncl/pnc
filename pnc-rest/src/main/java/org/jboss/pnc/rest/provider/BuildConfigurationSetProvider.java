package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildConfigurationPredicates.*;
import static org.jboss.pnc.datastore.predicates.BuildConfigurationSetPredicates.withBuildConfigurationSetId;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationSetProvider {

    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    public BuildConfigurationSetProvider() {
    }

    @Inject
    public BuildConfigurationSetProvider(BuildConfigurationSetRepository buildConfigurationSetRepository) {
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
    }

    public Function<? super BuildConfigurationSet, ? extends BuildConfigurationSetRest> toRestModel() {
        return buildConfigurationSet -> {
            if (buildConfigurationSet != null) {
                return new BuildConfigurationSetRest(buildConfigurationSet);
            }
            return null;
        };
    }

    public String getDefaultSortingField() {
        return BuildConfigurationSet.DEFAULT_SORTING_FIELD;
    }

    public List<BuildConfigurationSetRest> getAll() {
        return buildConfigurationSetRepository.findAll().stream().map(buildConfigurationSet -> new BuildConfigurationSetRest(buildConfigurationSet))
                .collect(Collectors.toList());
    }

    public List<BuildConfigurationSetRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(buildConfigurationSetRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public BuildConfigurationSetRest getSpecific(Integer id) {
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRepository.findOne(id);
        if (buildConfigurationSet != null) {
            return new BuildConfigurationSetRest(buildConfigurationSet);
        }
        return null;
    }

    public Integer store(BuildConfigurationSetRest buildConfigurationSetRest) {
        BuildConfigurationSet buildConfigurationSet = buildConfigurationSetRest.toBuildConfigurationSet();
        buildConfigurationSet = buildConfigurationSetRepository.save(buildConfigurationSet);
        return buildConfigurationSet.getId();
    }

    private Function<BuildConfiguration, BuildConfigurationRest> buildConfigToRestModel() {
        return buildConfig -> new BuildConfigurationRest(buildConfig);
    }

    public List<BuildConfigurationRest> getBuildConfigurations(Integer configurationSetId) {
        BuildConfigurationSet buildConfigSet = buildConfigurationSetRepository.findOne(withBuildConfigurationSetId(configurationSetId));
        Set<BuildConfiguration> buildConfigs = buildConfigSet.getBuildConfigurations();
        return nullableStreamOf(buildConfigs)
                .map(buildConfigToRestModel())
                .collect(Collectors.toList());
    }

}
