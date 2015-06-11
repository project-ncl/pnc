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
import org.jboss.pnc.model.License;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.jboss.pnc.spi.datastore.repositories.LicenseRepository;
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
public class LicenseProvider {

    private LicenseRepository licenseRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    // needed for EJB/CDI
    public LicenseProvider() {
    }

    @Inject
    public LicenseProvider(LicenseRepository licenseRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.licenseRepository = licenseRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public List<LicenseRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<License> rsqlPredicate = rsqlPredicateProducer.getPredicate(License.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);

        return nullableStreamOf(licenseRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public LicenseRest getSpecific(Integer id) {
        License license = licenseRepository.queryById(id);
        if (license != null) {
            return new LicenseRest(license);
        }
        return null;
    }

    public Integer store(LicenseRest licenseRest) {
        Preconditions.checkArgument(licenseRest.getId() == null, "Id must be null");
        return licenseRepository.save(licenseRest.toLicense()).getId();
    }

    public void update(Integer id, LicenseRest licenseRest) {
        Preconditions.checkArgument(id != null, "Id must not be null");
        Preconditions.checkArgument(licenseRest.getId() == null || licenseRest.getId().equals(id),
                "Entity id does not match the id to update");
        licenseRest.setId(id);
        License license = licenseRepository.queryById(id);
        Preconditions.checkArgument(license != null, "Couldn't find license with id " + licenseRest.getId());
        licenseRepository.save(licenseRest.toLicense());
    }

    public void delete(Integer id) {
        Preconditions.checkArgument(licenseRepository.queryById(id) != null, "Couldn't find license with id " + id);
        licenseRepository.delete(id);
    }

    public Function<? super License, ? extends LicenseRest> toRestModel() {
        return license -> new LicenseRest(license);
    }

}
