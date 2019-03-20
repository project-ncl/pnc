/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.mapper.api.SCMRepositoryMapper;
import org.jboss.pnc.facade.providers.api.SCMRepositoryProvider;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.matchByScmUrl;
import static org.jboss.pnc.spi.datastore.predicates.RepositoryConfigurationPredicates.searchByScmUrl;

@Stateless
public class SCMRepositoryProviderImpl
        extends AbstractProvider<RepositoryConfiguration, SCMRepository, SCMRepository> implements SCMRepositoryProvider {

    @Inject
    public SCMRepositoryProviderImpl(RepositoryConfigurationRepository repository,
                                     SCMRepositoryMapper mapper) {
        super(repository, mapper, RepositoryConfiguration.class);
    }

    public Page<SCMRepository> getAllWithMatchAndSearchUrl(int pageIndex,
                                                           int pageSize,
                                                           String sortingRsql,
                                                           String query,
                                                           String matchUrl,
                                                           String searchUrl) {

        List<Predicate<RepositoryConfiguration>> predicates = new ArrayList<>();

        addToListIfStringNotNullAndNotEmpty(predicates, matchUrl, matchByScmUrl(matchUrl));
        addToListIfStringNotNullAndNotEmpty(predicates, searchUrl, searchByScmUrl(searchUrl));

        // transform list to array for 'predicates' varargs in 'queryForCollection' method
        Predicate<RepositoryConfiguration>[] predicatesArray = new Predicate[predicates.size()];
        predicatesArray = predicates.toArray(predicatesArray);

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, predicatesArray);
    }

    /**
     * Add item to the list if the string is not null *and* not empty
     *
     * @param list
     * @param str
     * @param item
     * @param <T>
     */
    private <T> void addToListIfStringNotNullAndNotEmpty(List<T> list, String str, T item) {

        if (str != null && !str.isEmpty()) {
            list.add(item);
        }
    }
}
