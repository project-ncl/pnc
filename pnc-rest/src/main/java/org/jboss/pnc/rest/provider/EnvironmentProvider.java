package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.EnvironmentRepository;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.User;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class EnvironmentProvider extends BasePaginationProvider<EnvironmentRest, Environment> {

    private EnvironmentRepository environmentRepository;

    // needed for EJB/CDI
    public EnvironmentProvider() {
    }

    @Inject
    public EnvironmentProvider(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    // Needed to map the Entity into the proper REST object
    @Override
    public Function<? super Environment, ? extends EnvironmentRest> toRestModel() {
        return environment -> new EnvironmentRest(environment);
    }

    @Override
    public String getDefaultSortingField() {
        return Environment.DEFAULT_SORTING_FIELD;
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(User.class, query);
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return nullableStreamOf(environmentRepository.findAll(filteringCriteria.get())).map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(environmentRepository.findAll(filteringCriteria.get(), buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public EnvironmentRest getSpecific(Integer environmentId) {
        Environment environment = environmentRepository.findOne(environmentId);
        if (environment != null) {
            return toRestModel().apply(environment);
        }
        return null;
    }

    public Integer store(EnvironmentRest environmentRest) {
        return environmentRepository.save(environmentRest.toEnvironment()).getId();
    }

    public void update(EnvironmentRest environmentRest) {
        Environment license = environmentRepository.findOne(environmentRest.getId());
        Preconditions.checkArgument(license != null, "Couldn't find environment with id " + environmentRest.getId());
        environmentRepository.saveAndFlush(environmentRest.toEnvironment());
    }

    public void delete(Integer environmentId) {
        Preconditions.checkArgument(environmentRepository.exists(environmentId), "Couldn't find environment with id " + environmentId);
        environmentRepository.delete(environmentId);
    }
}
