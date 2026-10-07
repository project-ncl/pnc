/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.List;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.spi.datastore.repositories.api.Repository;

/**
 * Interface for manipulating {@link org.jboss.pnc.model.BuildConfigurationSet} entity.
 */
public interface BuildConfigurationSetRepository extends Repository<BuildConfigurationSet, Integer> {

    List<BuildConfigurationSet> withProductVersionId(Integer id);
}
