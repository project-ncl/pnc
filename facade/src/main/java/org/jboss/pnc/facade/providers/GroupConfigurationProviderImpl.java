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

import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.GroupConfigurationRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.mapper.api.GroupConfigurationMapper;
import org.jboss.pnc.facade.providers.api.GroupConfigurationProvider;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;

import javax.ejb.Stateless;
import javax.inject.Inject;

import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates.withProductVersionId;

@Stateless
public class GroupConfigurationProviderImpl extends AbstractProvider<BuildConfigurationSet, GroupConfiguration, GroupConfigurationRef> implements GroupConfigurationProvider {

    @Inject
    public GroupConfigurationProviderImpl(BuildConfigurationSetRepository repository, GroupConfigurationMapper mapper) {
        super(repository, mapper, BuildConfigurationSet.class);
    }


    @Override
    public Page<GroupConfiguration> getGroupConfigurationsForProductVersion(int pageIndex,
                                                                            int pageSize,
                                                                            String sortingRsql,
                                                                            String query,
                                                                            Integer productVersionId) {

        return queryForCollection(pageIndex, pageSize, sortingRsql, query, withProductVersionId(productVersionId));
    }
}
