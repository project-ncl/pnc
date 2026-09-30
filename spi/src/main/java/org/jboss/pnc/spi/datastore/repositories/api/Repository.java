/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories.api;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jboss.pnc.model.GenericEntity;

public interface Repository<T extends GenericEntity<ID>, ID extends Serializable> extends ReadOnlyRepository<T, ID> {
    T save(T entity);

    void delete(ID id);

    void delete(T id);

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
