package org.jboss.pnc.rest.provider;

import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildConfigurationSetRest;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;

import javax.ejb.Stateless;
import javax.inject.Inject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.datastore.predicates.BuildConfigurationPredicates.*;
import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class BuildConfigurationSetProvider extends BasePaginationProvider<BuildConfigurationSetRest, BuildConfigurationSet> {

    private BuildConfigurationSetRepository buildConfigurationSetRepository;

    public BuildConfigurationSetProvider() {
    }

    @Inject
    public BuildConfigurationSetProvider(BuildConfigurationSetRepository buildConfigurationSetRepository) {
        this.buildConfigurationSetRepository = buildConfigurationSetRepository;
    }

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super BuildConfigurationSet, ? extends BuildConfigurationSetRest> toRestModel() {
        return buildConfigurationSet -> {
            if (buildConfigurationSet != null) {
                return new BuildConfigurationSetRest(buildConfigurationSet);
            }
            return null;
        };
    }

    @Override
    public String getDefaultSortingField() {
        return BuildConfigurationSet.DEFAULT_SORTING_FIELD;
    }

    public List<BuildConfigurationSetRest> getAll() {
        return buildConfigurationSetRepository.findAll().stream().map(buildConfigurationSet -> new BuildConfigurationSetRest(buildConfigurationSet))
                .collect(Collectors.toList());
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting, String rsqls) {
        RSQLPredicate rsqlPredicate = RSQLPredicateProducer.fromRSQL(BuildConfiguration.class, rsqls);
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return nullableStreamOf(buildConfigurationSetRepository.findAll(rsqlPredicate.get())).map(toRestModel()).collect(
                    Collectors.toList());
        } else {
            return transform(buildConfigurationSetRepository.findAll(rsqlPredicate.get(),
                    buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
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

}
