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
package org.jboss.pnc.rest.provider;

import java.util.Map;
import java.util.SortedMap;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.model.utils.HibernateMetric;
import org.jboss.pnc.spi.datastore.repositories.CacheHandlerRepository;

import lombok.extern.slf4j.Slf4j;

@PermitAll
@Stateless
@Slf4j
public class CacheProvider {

    @Inject
    private CacheHandlerRepository cacheHandlerRepository;

    @Deprecated
    public CacheProvider() {
    }

    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheEntitiesStats() {
        log.debug("Get statistics of all entities in second-level cache.");
        return cacheHandlerRepository.getSecondLevelCacheEntitiesStats();
    }

    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheRegionsStats() {
        log.debug("Get statistics of all cache region names in second-level cache.");
        return cacheHandlerRepository.getSecondLevelCacheRegionsStats();
    }

    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheCollectionsStats() {
        log.debug("Get statistics of all collections in second-level cache.");
        return cacheHandlerRepository.getSecondLevelCacheCollectionsStats();
    }

    public SortedMap<String, HibernateMetric> getGenericStats() {
        log.debug("Get general statistics related to Hibernate.");
        return cacheHandlerRepository.getGenericStats();
    }

    @RolesAllowed("system-user")
    public void clearAllCache() {
        log.info("Evicting all content from second level cache...");
        cacheHandlerRepository.clearCache();
        log.info("Second level cache evicted");
    }

}
