/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.rest.provider;

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.datastore.repositories.LicenseRepository;
import org.jboss.pnc.model.License;
import org.jboss.pnc.rest.repository.RSQLAdapter;
import org.jboss.pnc.rest.repository.RSQLAdapterFactory;
import org.jboss.pnc.rest.restmodel.LicenseRest;

import com.google.common.base.Preconditions;

/**
 * Created by avibelli on Feb 5, 2015
 *
 */
@Stateless
public class LicenseProvider extends BasePaginationProvider<LicenseRest, License> {

    private LicenseRepository licenseRepository;

    // needed for EJB/CDI
    public LicenseProvider() {
    }

    @Inject
    public LicenseProvider(LicenseRepository licenseRepository) {
        this.licenseRepository = licenseRepository;
    }

    @Override
    public Function<? super License, ? extends LicenseRest> toRestModel() {
        return license -> new LicenseRest(license);
    }

    @Override
    public String getDefaultSortingField() {
        return License.DEFAULT_SORTING_FIELD;
    }

    public Object getAll(Integer pageIndex, Integer pageSize, String field, String sorting, String rsql) {
        RSQLAdapter<License> rsqlAdapter = RSQLAdapterFactory.fromRSQL(rsql);
        if (noPaginationRequired(pageIndex, pageSize, field, sorting)) {
            return licenseRepository.findAll(rsqlAdapter).stream().map(toRestModel()).collect(Collectors.toList());
        } else {
            return transform(licenseRepository.findAll(rsqlAdapter, buildPageRequest(pageIndex, pageSize, field, sorting)));
        }
    }

    public LicenseRest getSpecific(Integer id) {
        License license = licenseRepository.findOne(id);
        if (license != null) {
            return new LicenseRest(license);
        }
        return null;
    }

    public Integer store(LicenseRest licenseRest) {
        License license = licenseRest.getLicense(licenseRest);
        license = licenseRepository.save(license);
        return license.getId();
    }

    public Integer update(LicenseRest licenseRest) {
        License license = licenseRepository.findOne(licenseRest.getId());
        Preconditions.checkArgument(license != null, "Couldn't find license with id " + licenseRest.getId());

        // Applying the changes
        license.setFullContent(licenseRest.getFullContent());
        license.setFullName(licenseRest.getFullName());
        license.setRefUrl(licenseRest.getRefUrl());
        license.setShortName(licenseRest.getShortName());

        license = licenseRepository.saveAndFlush(license);
        return license.getId();
    }

}
