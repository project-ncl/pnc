package org.jboss.pnc.spi.datastore.repositories;

public interface CacheHandlerRepository {
    
    String getCacheStatistics();
    
    String getCacheStatistics(Class entityClass);
    
    void clearCache();
    
    void clearCache(Class entityClass);

}
