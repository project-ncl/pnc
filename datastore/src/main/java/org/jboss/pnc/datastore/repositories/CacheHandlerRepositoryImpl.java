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
package org.jboss.pnc.datastore.repositories;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.Statistics;
import org.jboss.pnc.spi.datastore.repositories.CacheHandlerRepository;

@Dependent
public class CacheHandlerRepositoryImpl implements CacheHandlerRepository {
    
    public CacheHandlerRepositoryImpl() {

    }

    private EntityManager entityManager;

    @Inject
    public CacheHandlerRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public String getCacheStatistics() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCacheStatistics(Class entityClass) {
        
//        Session session = (Session) entityManager.getDelegate();
//        SessionFactory sessionFactory = session.getSessionFactory();
//        Statistics statistics = sessionFactory.getStatistics();
//
//        
//        String[] collectionRoleNames = statistics.getCollectionRoleNames();
//        long connectionsCount = statistics.getConnectCount();
//        String[] entityNames = statistics.getEntityNames();
//        EntityStatistics entityStatistics = statistics.getEntityStatistics("java.lang.String entityName");
//        
//        long queryExecutionMaxTime = statistics.getQueryExecutionMaxTime();
//        String queryExecutionMaxTimeQueryString = statistics.getQueryExecutionMaxTimeQueryString();
//
//        long secondLevelCacheHitCount = statistics.getSecondLevelCacheHitCount();
//        long secondLevelCacheMissCount = statistics.getSecondLevelCacheMissCount();
//        long secondLevelCachePutCount = statistics.getSecondLevelCachePutCount();
//        String[] cacheRegionNames = statistics.getSecondLevelCacheRegionNames();
//        double hitRatio = (double) secondLevelCacheHitCount / ( secondLevelCacheHitCount + secondLevelCacheMissCount );
//        
//        CacheRegionStatistics secondLevelCacheStatistics = statistics.getDomainDataRegionStatistics("java.lang.String regionName");
//        secondLevelCacheStatistics.getElementCountInMemory();
//        secondLevelCacheStatistics.getElementCountOnDisk();
//        secondLevelCacheStatistics.getHitCount();
//        secondLevelCacheStatistics.getMissCount();
//        secondLevelCacheStatistics.getPutCount();
//        secondLevelCacheStatistics.getRegionName();
//        secondLevelCacheStatistics.getSizeInMemory();
//        
//        statistics.getDomainDataRegionStatistics("");
//        SecondLevelCacheStatistics secondLevelCacheStatistics =
//                statistics.getSecondLevelCacheStatistics( "query.cache.person" );
//        long hitCount = secondLevelCacheStatistics.getHitCount();
//        long missCount = secondLevelCacheStatistics.getMissCount();
//        double hitRatio = (double) hitCount / ( hitCount + missCount );
//        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearCache() {
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Override
    public void clearCache(Class entityClass) {
        entityManager.getEntityManagerFactory().getCache().evict(entityClass);
    }

//  public void getCacheStats() {
//  Session session = (Session) entityManager.getDelegate();
//  SessionFactory sessionFactory = session.getSessionFactory();
//  sessionFactory.getStatistics().
//}
    
    
}
