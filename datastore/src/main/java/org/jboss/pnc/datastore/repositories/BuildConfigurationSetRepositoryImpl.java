/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import java.util.List;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.predicates.BuildConfigurationSetPredicates;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;

@Stateless
public class BuildConfigurationSetRepositoryImpl extends AbstractRepository<BuildConfigurationSet, Integer>
        implements BuildConfigurationSetRepository {

    public BuildConfigurationSetRepositoryImpl() {
        super(BuildConfigurationSet.class, Integer.class);
    }

    @Override
    public List<BuildConfigurationSet> withProductVersionId(Integer id) {
        return queryWithPredicates(BuildConfigurationSetPredicates.withProductVersionId(id));
    }
}
