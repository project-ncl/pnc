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

import org.jboss.pnc.model.License;
import org.jboss.pnc.rest.restmodel.LicenseRest;
import org.jboss.pnc.spi.datastore.repositories.LicenseRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

@PermitAll
@Stateless
public class LicenseProvider extends AbstractProvider<License, LicenseRest> {

    // needed for EJB/CDI
    public LicenseProvider() {
    }

    @Inject
    public LicenseProvider(LicenseRepository licenseRepository, RSQLPredicateProducer rsqlPredicateProducer, SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(licenseRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    @Override
    protected Function<? super License, ? extends LicenseRest> toRESTModel() {
        return license -> new LicenseRest(license);
    }

    @Override
    protected Function<? super LicenseRest, ? extends License> toDBModel() {
        return licenseRest -> licenseRest.toDBEntityBuilder().build();
    }

}
