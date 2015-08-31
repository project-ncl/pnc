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

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;
import org.jboss.pnc.spi.datastore.repositories.EnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.SortInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.RSQLPredicateProducer;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.function.Function;

@Stateless
public class EnvironmentProvider extends AbstractProvider<Environment, EnvironmentRest> {

    // needed for EJB/CDI
    public EnvironmentProvider() {
    }

    @Inject
    public EnvironmentProvider(EnvironmentRepository environmentRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        super(environmentRepository, rsqlPredicateProducer, sortInfoProducer, pageInfoProducer);
    }

    @Override
    protected Function<? super Environment, ? extends EnvironmentRest> toRESTModel() {
        return environment -> new EnvironmentRest(environment);
    }

    @Override
    protected Function<? super EnvironmentRest, ? extends Environment> toDBModelModel() {
        return environment -> environment.toEnvironment();
    }
}
