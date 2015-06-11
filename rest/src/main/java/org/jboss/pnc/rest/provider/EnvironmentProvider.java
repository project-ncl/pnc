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
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.rest.restmodel.EnvironmentRest;
import org.jboss.pnc.spi.datastore.repositories.EnvironmentRepository;
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
public class EnvironmentProvider {

    private EnvironmentRepository environmentRepository;

    private RSQLPredicateProducer rsqlPredicateProducer;

    private SortInfoProducer sortInfoProducer;

    private PageInfoProducer pageInfoProducer;

    // needed for EJB/CDI
    public EnvironmentProvider() {
    }

    @Inject
    public EnvironmentProvider(EnvironmentRepository environmentRepository, RSQLPredicateProducer rsqlPredicateProducer,
            SortInfoProducer sortInfoProducer, PageInfoProducer pageInfoProducer) {
        this.environmentRepository = environmentRepository;
        this.rsqlPredicateProducer = rsqlPredicateProducer;
        this.sortInfoProducer = sortInfoProducer;
        this.pageInfoProducer = pageInfoProducer;
    }

    public List<EnvironmentRest> getAll(int pageIndex, int pageSize, String sortingRsql, String query) {
        Predicate<Environment> rsqlPredicate = rsqlPredicateProducer.getPredicate(Environment.class, query);
        PageInfo pageInfo = pageInfoProducer.getPageInfo(pageIndex, pageSize);
        SortInfo sortInfo = sortInfoProducer.getSortInfo(sortingRsql);
        return nullableStreamOf(environmentRepository.queryWithPredicates(pageInfo, sortInfo, rsqlPredicate))
                .map(toRestModel())
                .collect(Collectors.toList());
    }

    public EnvironmentRest getSpecific(Integer environmentId) {
        Environment environment = environmentRepository.queryById(environmentId);
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
        Environment license = environmentRepository.queryById(environmentRest.getId());
        Preconditions.checkArgument(license != null, "Couldn't find environment with id " + environmentRest.getId());
        environmentRepository.save(environmentRest.toEnvironment());
    }

    public void delete(Integer environmentId) {
        Preconditions.checkArgument(environmentRepository.queryById(environmentId) != null, "Couldn't find environment with id " + environmentId);
        environmentRepository.delete(environmentId);
    }

    public Function<? super Environment, ? extends EnvironmentRest> toRestModel() {
        return environment -> new EnvironmentRest(environment);
    }
}
