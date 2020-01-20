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
package org.jboss.pnc.model.utils;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;

public class HibernateStatsUtils {

    /**
     * Get all the Hibernate Entities statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing all the Hibernate entities stats
     */
    public static SortedMap<String, Map<String, String>> getAllSecondLevelCacheEntitiesStats(Statistics statistics) {

        SortedMap<String, Map<String, String>> entitiesStatMap = new TreeMap<String, Map<String, String>>();
        String[] entityNames = statistics.getEntityNames();
        Stream.of(entityNames).forEach(eN -> {
            EntityStatistics entityStat = statistics.getEntityStatistics(eN);
            SortedMap<String, String> entityStatMap = new TreeMap<String, String>();
            entityStatMap.put("cacheHitCount", String.valueOf(entityStat.getCacheHitCount()));
            entityStatMap.put("cacheMissCount", String.valueOf(entityStat.getCacheMissCount()));
            entityStatMap.put("cacheHitRatio",
                    (entityStat.getCacheHitCount() + entityStat.getCacheMissCount()) != 0 ? String.valueOf(
                            entityStat.getCacheHitCount() / (entityStat.getCacheHitCount() + entityStat.getCacheMissCount()))
                            : "NaN");
            entityStatMap.put("cachePutCount", String.valueOf(entityStat.getCachePutCount()));
            entityStatMap.put("deleteCount", String.valueOf(entityStat.getDeleteCount()));
            entityStatMap.put("fetchCount", String.valueOf(entityStat.getFetchCount()));
            entityStatMap.put("insertCount", String.valueOf(entityStat.getInsertCount()));
            entityStatMap.put("loadCount", String.valueOf(entityStat.getLoadCount()));
            entityStatMap.put("optimisticFailureCount", String.valueOf(entityStat.getOptimisticFailureCount()));
            entityStatMap.put("updateCount", String.valueOf(entityStat.getUpdateCount()));
            entityStatMap.put("cacheRegionName", entityStat.getCacheRegionName());
            entitiesStatMap.put(eN, entityStatMap);
        });

        return entitiesStatMap;
    }

    /**
     * Get all the Hibernate Entities second level cache statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing all the Hibernate second level cache statistics
     */
    public static SortedMap<String, Map<String, String>> getAllSecondLevelCacheRegionsStats(Statistics statistics) {

        SortedMap<String, Map<String, String>> secondLevelCachesStatMap = new TreeMap<String, Map<String, String>>();
        String[] cacheRegionNames = statistics.getSecondLevelCacheRegionNames();
        Stream.of(cacheRegionNames).forEach(crN -> {
            CacheRegionStatistics secondLevelCacheStatistics = statistics.getDomainDataRegionStatistics(crN);
            SortedMap<String, String> secondLevelCacheStatMap = new TreeMap<String, String>();
            secondLevelCacheStatMap.put("elementCountInMemory",
                    String.valueOf(secondLevelCacheStatistics.getElementCountInMemory()));
            secondLevelCacheStatMap.put("elementCountOnDisk",
                    String.valueOf(secondLevelCacheStatistics.getElementCountOnDisk()));
            secondLevelCacheStatMap.put("hitCount", String.valueOf(secondLevelCacheStatistics.getHitCount()));
            secondLevelCacheStatMap.put("missCount", String.valueOf(secondLevelCacheStatistics.getMissCount()));
            secondLevelCacheStatMap.put("hitRatio",
                    (secondLevelCacheStatistics.getHitCount() + secondLevelCacheStatistics.getMissCount()) != 0
                            ? String.valueOf(secondLevelCacheStatistics.getHitCount()
                                    / (secondLevelCacheStatistics.getHitCount() + secondLevelCacheStatistics.getMissCount()))
                            : "NaN");
            secondLevelCacheStatMap.put("putCount", String.valueOf(secondLevelCacheStatistics.getPutCount()));
            secondLevelCacheStatMap.put("sizeInMemory", String.valueOf(secondLevelCacheStatistics.getSizeInMemory()));
            secondLevelCacheStatMap.put("regionName", secondLevelCacheStatistics.getRegionName());
            secondLevelCachesStatMap.put(crN, secondLevelCacheStatMap);
        });

        return secondLevelCachesStatMap;
    }

    /**
     * Get all the Hibernate collections statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing all the Hibernate collections statistics
     */
    public static SortedMap<String, Map<String, String>> getAllSecondLevelCacheCollectionsStats(Statistics statistics) {

        SortedMap<String, Map<String, String>> collectionsStatMap = new TreeMap<String, Map<String, String>>();
        
        String[] collectionRoleNames = statistics.getCollectionRoleNames();
        Stream.of(collectionRoleNames).forEach(crN -> {
            CollectionStatistics collectionStatistics = statistics.getCollectionStatistics(crN);
            SortedMap<String, String> collectionStatMap = new TreeMap<String, String>();
            collectionStatMap.put("cacheHitCount",
                    String.valueOf(collectionStatistics.getCacheHitCount()));
            collectionStatMap.put("cacheMissCount",
                    String.valueOf(collectionStatistics.getCacheMissCount()));
            collectionStatMap.put("cacheHitRatio",
                    (collectionStatistics.getCacheHitCount() + collectionStatistics.getCacheMissCount()) != 0
                            ? String.valueOf(collectionStatistics.getCacheHitCount()
                                    / (collectionStatistics.getCacheHitCount() + collectionStatistics.getCacheMissCount()))
                            : "NaN");
            collectionStatMap.put("cachePutCount", String.valueOf(collectionStatistics.getCachePutCount()));
            collectionStatMap.put("fetchCount", String.valueOf(collectionStatistics.getFetchCount()));
            collectionStatMap.put("loadCount", String.valueOf(collectionStatistics.getLoadCount()));
            collectionStatMap.put("recreateCount", String.valueOf(collectionStatistics.getRecreateCount()));
            collectionStatMap.put("removeCount", String.valueOf(collectionStatistics.getRemoveCount()));
            collectionStatMap.put("updateCount", String.valueOf(collectionStatistics.getUpdateCount()));
            collectionStatMap.put("regionName", collectionStatistics.getCacheRegionName());
            collectionsStatMap.put(crN, collectionStatMap);
        });

        return collectionsStatMap;
    }

    /**
     * Get all the Hibernate generic statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing Hibernate generic stats
     */
    public static SortedMap<String, String> getGenericStats(Statistics statistics) {

        SortedMap<String, String> genericStatsMap = new TreeMap<String, String>();
        genericStatsMap.put("collectionFetchCount", String.valueOf(statistics.getCollectionFetchCount()));
        genericStatsMap.put("collectionLoadCount", String.valueOf(statistics.getCollectionLoadCount()));
        genericStatsMap.put("collectionRecreateCount", String.valueOf(statistics.getCollectionRecreateCount()));
        genericStatsMap.put("collectionRemoveCount", String.valueOf(statistics.getCollectionRemoveCount()));
        genericStatsMap.put("collectionUpdateCount", String.valueOf(statistics.getCollectionUpdateCount()));

        genericStatsMap.put("connectCount", String.valueOf(statistics.getConnectCount()));
        genericStatsMap.put("closeStatementCount", String.valueOf(statistics.getCloseStatementCount()));
        genericStatsMap.put("flushCount", String.valueOf(statistics.getFlushCount()));
        genericStatsMap.put("optimisticFailureCount", String.valueOf(statistics.getOptimisticFailureCount()));
        genericStatsMap.put("prepareStatementCount", String.valueOf(statistics.getPrepareStatementCount()));
        genericStatsMap.put("sessionCloseCount", String.valueOf(statistics.getSessionCloseCount()));
        genericStatsMap.put("sessionOpenCount", String.valueOf(statistics.getSessionOpenCount()));
        genericStatsMap.put("successfulTransactionCount", String.valueOf(statistics.getSuccessfulTransactionCount()));
        genericStatsMap.put("transactionCount", String.valueOf(statistics.getTransactionCount()));

        genericStatsMap.put("entityDeleteCount", String.valueOf(statistics.getEntityDeleteCount()));
        genericStatsMap.put("entityFetchCount", String.valueOf(statistics.getEntityFetchCount()));
        genericStatsMap.put("entityInsertCount", String.valueOf(statistics.getEntityInsertCount()));
        genericStatsMap.put("entityLoadCount", String.valueOf(statistics.getEntityLoadCount()));
        genericStatsMap.put("entityUpdateCount", String.valueOf(statistics.getEntityUpdateCount()));

        genericStatsMap.put("naturalIdCacheHitCount", String.valueOf(statistics.getNaturalIdCacheHitCount()));
        genericStatsMap.put("naturalIdCacheMissCount", String.valueOf(statistics.getNaturalIdCacheMissCount()));
        genericStatsMap.put("naturalIdCachePutCount", String.valueOf(statistics.getNaturalIdCachePutCount()));
        genericStatsMap
                .put("naturalIdCacheHitRatio",
                        (statistics.getNaturalIdCacheHitCount() + statistics.getNaturalIdCacheMissCount()) != 0
                                ? String.valueOf(statistics.getNaturalIdCacheHitCount()
                                        / (statistics.getNaturalIdCacheHitCount() + statistics.getNaturalIdCacheMissCount()))
                                : "NaN");
        genericStatsMap.put("naturalIdQueryExecutionCount", String.valueOf(statistics.getNaturalIdQueryExecutionCount()));
        genericStatsMap.put("naturalIdQueryExecutionMaxTime", String.valueOf(statistics.getNaturalIdQueryExecutionMaxTime()));
        genericStatsMap.put("naturalIdQueryExecutionMaxTimeRegion",
                String.valueOf(statistics.getNaturalIdQueryExecutionMaxTimeRegion()));
        genericStatsMap.put("naturalIdQueryExecutionMaxTimeEntity",
                String.valueOf(statistics.getNaturalIdQueryExecutionMaxTimeEntity()));

        genericStatsMap.put("queryCacheHitCount", String.valueOf(statistics.getQueryCacheHitCount()));
        genericStatsMap.put("queryCacheMissCount", String.valueOf(statistics.getQueryCacheMissCount()));
        genericStatsMap.put("queryCachePutCount", String.valueOf(statistics.getQueryCachePutCount()));
        genericStatsMap.put("queryExecutionCount", String.valueOf(statistics.getQueryExecutionCount()));
        genericStatsMap.put("queryExecutionMaxTime", String.valueOf(statistics.getQueryExecutionMaxTime()));

        genericStatsMap.put("secondLevelCacheHitCount", String.valueOf(statistics.getSecondLevelCacheHitCount()));
        genericStatsMap.put("secondLevelCacheMissCount", String.valueOf(statistics.getSecondLevelCacheMissCount()));
        genericStatsMap.put("secondLevelCachePutCount", String.valueOf(statistics.getSecondLevelCachePutCount()));
        genericStatsMap.put("secondLevelCacheHitRatio",
                (statistics.getSecondLevelCacheHitCount() + statistics.getSecondLevelCacheMissCount()) != 0
                        ? String.valueOf(statistics.getSecondLevelCacheHitCount()
                                / (statistics.getSecondLevelCacheHitCount() + statistics.getSecondLevelCacheMissCount()))
                        : "NaN");

        genericStatsMap.put("updateTimestampsCacheHitCount", String.valueOf(statistics.getUpdateTimestampsCacheHitCount()));
        genericStatsMap.put("updateTimestampsCacheMissCount", String.valueOf(statistics.getUpdateTimestampsCacheMissCount()));
        genericStatsMap.put("updateTimestampsCachePutCount", String.valueOf(statistics.getUpdateTimestampsCachePutCount()));

        return genericStatsMap;
    }
    

}
