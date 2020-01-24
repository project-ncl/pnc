package org.jboss.pnc.facade.providers;

import static org.jboss.pnc.facade.providers.api.UserRoles.SYSTEM_USER;

import java.util.Map;
import java.util.SortedMap;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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

    @RolesAllowed(SYSTEM_USER)
    @Override
    public void clearAllCache() {
        logger.debug("Evict all content from second level cache");
        cacheHandlerRepository.clearCache();
    }

}
