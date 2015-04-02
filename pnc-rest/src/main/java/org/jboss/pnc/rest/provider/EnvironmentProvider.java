package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.EnvironmentRepository;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class EnvironmentProvider {

    private EnvironmentRepository environmentRepository;

    // needed for EJB/CDI
    public EnvironmentProvider() {
    }

    @Inject
    public EnvironmentProvider(EnvironmentRepository environmentRepository) {
        this.environmentRepository = environmentRepository;
    }

    public List<EnvironmentRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(Environment.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(environmentRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public EnvironmentRest getSpecific(Integer environmentId) {
        Environment environment = environmentRepository.findOne(environmentId);
        if (environment != null) {
            return toRestModel().apply(environment);
        }
        return null;
    }

    public Integer store(EnvironmentRest environmentRest) {
        Preconditions.checkArgument(environmentRest.getId() == null, "Id must be null");
        return environmentRepository.save(environmentRest.toEnvironment()).getId();
    }

    public void update(Integer id, EnvironmentRest environmentRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(environmentRest.getId() == null || environmentRest.getId().equals(id),
                "Entity id does not match the id to update");
        environmentRest.setId(id);
        Environment license = environmentRepository.findOne(environmentRest.getId());
        Preconditions.checkArgument(license != null, "Couldn't find environment with id " + environmentRest.getId());
        environmentRepository.saveAndFlush(environmentRest.toEnvironment());
    }

    public void delete(Integer environmentId) {
        Preconditions.checkArgument(environmentRepository.exists(environmentId), "Couldn't find environment with id " + environmentId);
        environmentRepository.delete(environmentId);
    }

    public Function<? super Environment, ? extends EnvironmentRest> toRestModel() {
        return environment -> new EnvironmentRest(environment);
    }
}
