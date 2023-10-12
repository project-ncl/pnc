/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.remotecoordinator.test.mock;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.GenericSetting;
import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.GenericSettingRepository;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.jboss.pnc.spi.datastore.repositories.api.SortInfo;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Alternative
@ApplicationScoped
public class GenericSettingsRepositoryMock implements GenericSettingRepository {

    @Override
    public GenericSetting queryByKey(String key) {
        return null;
    }

    @Override
    public List<GenericSetting> queryAll() {
        return List.of();
    }

    @Override
    public List<GenericSetting> queryWithPredicates(
            PageInfo pageInfo,
            SortInfo<GenericSetting> sortInfo,
            Predicate<GenericSetting>... predicates) {
        return List.of();
    }

    @Override
    public List<GenericSetting> queryWithPredicates(Predicate<GenericSetting>... predicates) {
        return List.of();
    }

    @Override
    public int count(
            Collection<Predicate<GenericSetting>> andPredicates,
            Collection<Predicate<GenericSetting>> orPredicates) {
        return 0;
    }

    @Override
    public int count(Predicate<GenericSetting>... predicates) {
        return 0;
    }

    @Override
    public GenericSetting queryByPredicates(Predicate<GenericSetting>... predicates) {
        return null;
    }

    @Override
    public GenericSetting queryById(Integer id) {
        return null;
    }

    @Override
    public List<GenericSetting> queryAll(PageInfo pageInfo, SortInfo<GenericSetting> sortInfo) {
        return null;
    }

    @Override
    public GenericSetting save(GenericSetting entity) {
        return null;
    }

    @Override
    public void delete(Integer id) {
    }

    @Override
    public void delete(GenericSetting id) {
    }

    @Override
    public void flushAndRefresh(GenericSetting entity) {
    }

    @Override
    public <N extends GenericEntity<Integer>> void cascadeUpdates(
            N managedNonOwning,
            N updatedNonOwning,
            Function<N, Collection<GenericSetting>> collectionGetter,
            BiConsumer<GenericSetting, N> owningSetter,
            java.util.function.Predicate<GenericSetting>... filters) {
    }
}
