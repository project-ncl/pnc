package org.jboss.pnc.rest.provider;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.jboss.pnc.spi.datastore.repositories.CacheHandlerRepository;

import lombok.extern.slf4j.Slf4j;

@PermitAll
@Stateless
@Slf4j
public class CacheProvider {

    @Inject
    private CacheHandlerRepository cacheHandlerRepository; 
    
    @Deprecated
    public CacheProvider() {}

    @RolesAllowed("system-user")
    public String getStatistics() {
        log.debug("Get all statistics of second level cache");
        return cacheHandlerRepository.getCacheStatistics();
    }

    @RolesAllowed("system-user")
    public String getStatistics(Class entityClass) {
        log.debug("Get statistics of entity {} in second level cache", entityClass);
        return cacheHandlerRepository.getCacheStatistics(entityClass);
    }

    @RolesAllowed("system-user")
    public void clearAllCache() {
        log.debug("Evict all content from second level cache");
        cacheHandlerRepository.clearCache();
    }

    @RolesAllowed("system-user")
    public void clearCache(Class entityClass) {
        log.debug("Evict all content of entity {} from second level cache", entityClass);
        cacheHandlerRepository.clearCache(entityClass);
    }



}
