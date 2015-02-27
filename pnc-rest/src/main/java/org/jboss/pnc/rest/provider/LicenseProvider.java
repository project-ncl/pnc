package org.jboss.pnc.rest.provider;

import com.google.common.base.Preconditions;
import org.jboss.pnc.datastore.limits.RSQLPageLimitAndSortingProducer;
import org.jboss.pnc.datastore.predicates.RSQLPredicate;
import org.jboss.pnc.datastore.predicates.RSQLPredicateProducer;
import org.jboss.pnc.datastore.repositories.LicenseRepository;
import org.jboss.pnc.model.License;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.springframework.data.domain.Pageable;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@Stateless
public class LicenseProvider {

    private LicenseRepository licenseRepository;

    // needed for EJB/CDI
    public LicenseProvider() {
    }

    @Inject
    public LicenseProvider(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    public List<LicenseRest> getAll(Integer pageIndex, Integer pageSize, String sortingRsql, String query) {
        RSQLPredicate filteringCriteria = RSQLPredicateProducer.fromRSQL(License.class, query);
        Pageable paging = RSQLPageLimitAndSortingProducer.fromRSQL(pageSize, pageIndex, sortingRsql);

        return nullableStreamOf(licenseRepository.findAll(filteringCriteria.get(), paging))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public LicenseRest getSpecific(Integer id) {
        License license = licenseRepository.findOne(id);
        if (license != null) {
            return new LicenseRest(license);
        }
        return null;
    }

    public Integer store(LicenseRest licenseRest) {
        return licenseRepository.save(licenseRest.toLicense()).getId();
    }

    public void update(LicenseRest licenseRest) {
        License license = licenseRepository.findOne(licenseRest.getId());
        Preconditions.checkArgument(license != null, "Couldn't find license with id " + licenseRest.getId());
        licenseRepository.saveAndFlush(licenseRest.toLicense());
    }

    public void delete(Integer id) {
        Preconditions.checkArgument(licenseRepository.exists(id), "Couldn't find license with id " + id);
        licenseRepository.delete(id);
    }

    public Function<? super License, ? extends LicenseRest> toRestModel() {
        return license -> new LicenseRest(license);
    }

}
