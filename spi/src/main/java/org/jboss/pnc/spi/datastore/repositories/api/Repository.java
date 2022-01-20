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
package org.jboss.pnc.spi.datastore.repositories.api;

import org.jboss.pnc.model.GenericEntity;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface Repository<T extends GenericEntity<ID>, ID extends Serializable> extends ReadOnlyRepository<T, ID> {
    T save(T entity);

    void delete(ID id);

    void flushAndRefresh(T entity);

    /**
     * This method cascades updates for @OneToMany(@ManyToOne) DB relationships. The method supports both deleting and
     * adding single/multiple relations between entities. The method calculates difference between current data in DB
     * and proposed data from request and deletes/adds relations to the owning side of relationship accordingly.
     *
     * ONLY for @OneToMany (not @ManyToMany) relationships
     * 
     * @param managedNonOwning current version of entity from DB (MUST be Hibernate-managed (due to LAZY fetching))
     * @param updatedNonOwning proposed version of entity from request
     * @param collectionGetter getter with collection of owning side (f.e ProductVersion::getBuildConfigurations)
     * @param owningSetter setter which updates the owning side (f.e BuildConfiguration::setProductVersion)
     * @param filters optional filters
     * @param <N> type for non-owning side of relationship (f.e ProductVersion)
     * @param <T> type for owning side of relationship (f.e BuildConfiguration)
     */
    <N extends GenericEntity<ID>> void cascadeUpdates(
            N managedNonOwning,
            N updatedNonOwning,
            Function<N, Collection<T>> collectionGetter,
            BiConsumer<T, N> owningSetter,
            java.util.function.Predicate<T>... filters);
}
