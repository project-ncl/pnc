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
package org.jboss.pnc.facade.providers;

import java.util.Map;
import java.util.SortedMap;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.facade.providers.api.CacheProvider;
import org.jboss.pnc.model.utils.HibernateMetric;
import org.jboss.pnc.spi.datastore.repositories.CacheHandlerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PermitAll
@Stateless
public class CacheProviderImpl implements CacheProvider {

    private static final Logger logger = LoggerFactory.getLogger(CacheProviderImpl.class);

    private CacheHandlerRepository cacheHandlerRepository;

    @Inject
    public CacheProviderImpl(CacheHandlerRepository cacheHandlerRepository) {
        this.cacheHandlerRepository = cacheHandlerRepository;
    }

    @Override
    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheEntitiesStats() {
        logger.debug("Get statistics of all entities in second-level cache.");
        return cacheHandlerRepository.getSecondLevelCacheEntitiesStats();
    }

    @Override
    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheRegionsStats() {
        logger.debug("Get statistics of all cache region names in second-level cache.");
        return cacheHandlerRepository.getSecondLevelCacheRegionsStats();
    }

    @Override
    public SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheCollectionsStats() {
        logger.debug("Get statistics of all collections in second-level cache.");
        return cacheHandlerRepository.getSecondLevelCacheCollectionsStats();
    }

    @Override
    public SortedMap<String, HibernateMetric> getGenericStats() {
        logger.debug("Get general statistics related to Hibernate.");
        return cacheHandlerRepository.getGenericStats();
    }

    @Override
    public void clearAllCache() {
        logger.info("Evicting all content from second level cache...");
        cacheHandlerRepository.clearCache();
        logger.info("Second level cache evicted");
    }

}
