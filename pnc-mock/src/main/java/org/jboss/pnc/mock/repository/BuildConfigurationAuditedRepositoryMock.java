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
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 9/22/16
 * Time: 12:25 PM
 */
public class BuildConfigurationAuditedRepositoryMock implements BuildConfigurationAuditedRepository {

    private final AtomicInteger idSequence = new AtomicInteger(0);
    protected final List<BuildConfigurationAudited> data = new ArrayList<>();
    
    @Override
    public BuildConfigurationAudited save(BuildConfigurationAudited entity) {
        IdRev id = entity.getId();
        if (id == null) {
            throw new IllegalStateException("auto-setting " + this.getClass().getSimpleName() + " entity id is not supported");
        }
        getOptionalById(id).ifPresent(data::remove);
        data.add(entity);
        return entity;
    }

    @Override
    public List<BuildConfigurationAudited> queryAll() {
        return data;
    }

    @Override
    public List<BuildConfigurationAudited> queryAll(PageInfo pageInfo, SortInfo sortInfo) {
        return null;  
    }

    @Override
    public List<BuildConfigurationAudited> queryWithPredicates(PageInfo pageInfo, SortInfo sortInfo, Predicate<BuildConfigurationAudited>... predicates) {
        return null;  
    }

    @Override
    public List<BuildConfigurationAudited> queryWithPredicates(Predicate<BuildConfigurationAudited>... predicates) {
        return null;
    }

    @Override
    public int count(Predicate<BuildConfigurationAudited>... predicates) {
        return 0;
    }

    @Override
    public BuildConfigurationAudited queryByPredicates(Predicate<BuildConfigurationAudited>... predicates) {
        return null;
    }

    @Override
    public void delete(IdRev id) {
        data.removeIf(c -> c.getId().equals(id));
    }

    @Override
    public List<BuildConfigurationAudited> findAllByIdOrderByRevDesc(Integer id) {
        return data.stream()
                .filter(c -> c.getId().getId().equals(id))
                .sorted((c1, c2) -> c2.getId().getRev().compareTo(c1.getId().getRev()))
                .collect(Collectors.toList());
    }

    private Optional<BuildConfigurationAudited> getOptionalById(IdRev id) {
        return data.stream()
                .filter(m -> id.equals(m.getId()))
                .findAny();
    }

    @Override
    public BuildConfigurationAudited queryById(IdRev id) {
        return getOptionalById(id).orElseThrow(() -> new RuntimeException("Didn't find entity for id: " + id));
    }
}
