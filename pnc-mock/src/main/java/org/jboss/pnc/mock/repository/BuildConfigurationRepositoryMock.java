/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import java.util.Collections;
import java.util.List;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:06 PM
 */
public class BuildConfigurationRepositoryMock extends IntIdRepositoryMock<BuildConfiguration>
        implements BuildConfigurationRepository {

    List<BuildConfiguration> returnOnQuery = Collections.emptyList();

    @Override
    public List<BuildConfiguration> queryWithPredicates(Predicate<BuildConfiguration>... predicates) {
        return returnOnQuery;
    }

    public void returnAllDataOnQuery() {
        returnOnQuery = data;
    }

    public void returnWhenQueryingWithPredicates(List<BuildConfiguration> toReturn) {
        returnOnQuery = toReturn;
    }
}
