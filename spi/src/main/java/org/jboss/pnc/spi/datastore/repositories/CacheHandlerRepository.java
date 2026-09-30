/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import java.util.Map;
import java.util.SortedMap;

import org.jboss.pnc.model.utils.HibernateMetric;

public interface CacheHandlerRepository {

    SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheEntitiesStats();

    SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheRegionsStats();

    SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheCollectionsStats();

    SortedMap<String, HibernateMetric> getGenericStats();

    void clearCache();

}
