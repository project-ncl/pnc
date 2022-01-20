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
package org.jboss.pnc.model.utils;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateStatsUtils {

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(HibernateStatsUtils.class);

    public static String ENTITY_STATS_PREFIX = "hibernate-orm.entity.";
    public static String REGION_STATS_PREFIX = "hibernate-orm.region.";
    public static String COLLECTION_STATS_PREFIX = "hibernate-orm.collection.";

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    /**
     * Get all the Hibernate Entities statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing all the Hibernate entities stats
     */
    public static SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheEntitiesStats(
            Statistics statistics) {

        SortedMap<String, Map<String, HibernateMetric>> entitiesStatMap = new TreeMap<>();

        if (statistics.isStatisticsEnabled()) {
            String[] entityNames = statistics.getEntityNames();
            Stream.of(entityNames).forEach(eN -> {
                EntityStatistics entityStat = statistics.getEntityStatistics(eN);
                SortedMap<String, HibernateMetric> entityStatMap = new TreeMap<>();

                // Entity cache stats
                entityStatMap.put(
                        "cache.region.name",
                        createHibernateMetricItem(
                                "cacheRegionName",
                                "The name of the region where this data is cached.",
                                entityStat.getCacheRegionName()));
                entityStatMap.put(
                        "cache.hit.count",
                        createHibernateMetricItem(
                                "cacheHitCount",
                                "The number of successful cache look-ups for this data from its configured cache region since the last Statistics clearing.",
                                entityStat.getCacheHitCount()));
                entityStatMap.put(
                        "cache.miss.count",
                        createHibernateMetricItem(
                                "cacheMissCount",
                                "The number of unsuccessful cache look-ups for this data from its configured cache region since the last Statistics clearing.",
                                entityStat.getCacheMissCount()));
                entityStatMap.put(
                        "cache.put.count",
                        createHibernateMetricItem(
                                "cachePutCount",
                                "The number of times this data has been into its configured cache region since the last Statistics clearing.",
                                entityStat.getCachePutCount()));
                double hitsRatio = (entityStat.getCacheHitCount() + entityStat.getCacheMissCount()) != 0
                        ? ((double) entityStat.getCacheHitCount()
                                / (entityStat.getCacheHitCount() + entityStat.getCacheMissCount()) * 100)
                        : -1;
                entityStatMap.put(
                        "cache.hit.ratio",
                        createHibernateMetricItem(
                                "cacheHitRatio",
                                "The ratio of successful cache look-ups for this data from its configured cache region since the last Statistics clearing.",
                                df2.format(hitsRatio)));

                // Entity stats
                entityStatMap.put(
                        "fetch.count",
                        createHibernateMetricItem(
                                "fetchCount",
                                "Number of times (since last Statistics clearing) this entity has been fetched.",
                                entityStat.getFetchCount()));
                entityStatMap.put(
                        "insert.count",
                        createHibernateMetricItem(
                                "insertCount",
                                "Number of times (since last Statistics clearing) this entity has been inserted.",
                                entityStat.getInsertCount()));
                entityStatMap.put(
                        "delete.count",
                        createHibernateMetricItem(
                                "deleteCount",
                                "Number of times (since last Statistics clearing) this entity has been deleted.",
                                entityStat.getDeleteCount()));
                entityStatMap.put(
                        "load.count",
                        createHibernateMetricItem(
                                "loadCount",
                                "Number of times (since last Statistics clearing) this entity has been loaded.",
                                entityStat.getLoadCount()));
                entityStatMap.put(
                        "optimistic.failure.count",
                        createHibernateMetricItem(
                                "optimisticFailureCount",
                                "Number of times (since last Statistics clearing) this entity has experienced an optimistic lock failure.",
                                entityStat.getOptimisticFailureCount()));
                entityStatMap.put(
                        "update.count",
                        createHibernateMetricItem(
                                "updateCount",
                                "Number of times (since last Statistics clearing) this entity has been updated.",
                                entityStat.getUpdateCount()));

                entitiesStatMap.put(ENTITY_STATS_PREFIX + eN, entityStatMap);
            });
        }

        return entitiesStatMap;
    }

    /**
     * Get all the Hibernate Entities second level cache statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing all the Hibernate second level cache statistics
     */
    public static SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheRegionsStats(
            Statistics statistics) {

        SortedMap<String, Map<String, HibernateMetric>> secondLevelCachesStatMap = new TreeMap<>();

        if (statistics.isStatisticsEnabled()) {
            String[] cacheRegionNames = statistics.getSecondLevelCacheRegionNames();
            Stream.of(cacheRegionNames).forEach(crN -> {
                try {
                    CacheRegionStatistics sLCStats = statistics.getDomainDataRegionStatistics(crN);
                    SortedMap<String, HibernateMetric> sLCStatMap = new TreeMap<>();

                    sLCStatMap.put(
                            "second-level-cache.cache.region.name",
                            createHibernateMetricItem(
                                    "regionName",
                                    "The name of the region where this data is cached.",
                                    sLCStats.getRegionName()));

                    sLCStatMap.put(
                            "second-level-cache.element.count.in.memory",
                            createHibernateMetricItem(
                                    "elementCountInMemory",
                                    "The number of elements currently in memory within the cache provider.",
                                    sLCStats.getElementCountInMemory() != CacheRegionStatistics.NO_EXTENDED_STAT_SUPPORT_RETURN
                                            ? sLCStats.getElementCountInMemory()
                                            : -1));
                    sLCStatMap.put(
                            "second-level-cache.element.count.on.disk",
                            createHibernateMetricItem(
                                    "elementCountOnDisk",
                                    "The number of elements currently stored to disk within the cache provider.",
                                    sLCStats.getElementCountOnDisk() != CacheRegionStatistics.NO_EXTENDED_STAT_SUPPORT_RETURN
                                            ? sLCStats.getElementCountOnDisk()
                                            : -1));
                    sLCStatMap.put(
                            "second-level-cache.size.in.memory",
                            createHibernateMetricItem(
                                    "sizeInMemory",
                                    "The size that the in-memory elements take up within the cache provider.",
                                    sLCStats.getSizeInMemory() != CacheRegionStatistics.NO_EXTENDED_STAT_SUPPORT_RETURN
                                            ? sLCStats.getSizeInMemory()
                                            : -1));

                    sLCStatMap.put(
                            "second-level-cache.hit.count",
                            createHibernateMetricItem(
                                    "hitCount",
                                    "The number of successful cache look-ups against the region since the last Statistics clearing.",
                                    sLCStats.getHitCount()));
                    sLCStatMap.put(
                            "second-level-cache.miss.count",
                            createHibernateMetricItem(
                                    "missCount",
                                    "The number of unsuccessful cache look-ups against the region since the last Statistics clearing.",
                                    sLCStats.getMissCount()));
                    double secondLvlCacheHitsRatio = (sLCStats.getHitCount() + sLCStats.getMissCount()) != 0
                            ? ((double) sLCStats.getHitCount() / (sLCStats.getHitCount() + sLCStats.getMissCount())
                                    * 100)
                            : -1;
                    sLCStatMap.put(
                            "second-level-cache.hit.ratio",
                            createHibernateMetricItem(
                                    "hitRatio",
                                    "The ratio of successful cache look-ups against the region since the last Statistics clearing.",
                                    df2.format(secondLvlCacheHitsRatio)));
                    sLCStatMap.put(
                            "second-level-cache.put.count",
                            createHibernateMetricItem(
                                    "putCount",
                                    "The number of cache puts into the region since the last Statistics clearing.",
                                    sLCStats.getPutCount()));

                    secondLevelCachesStatMap.put(REGION_STATS_PREFIX + crN, sLCStatMap);
                } catch (IllegalArgumentException e) {
                    // logger.error("The region name could not be resolved: {}", e);
                }
            });
        }

        return secondLevelCachesStatMap;
    }

    /**
     * Get all the Hibernate collections statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing all the Hibernate collections statistics
     */
    public static SortedMap<String, Map<String, HibernateMetric>> getSecondLevelCacheCollectionsStats(
            Statistics statistics) {

        SortedMap<String, Map<String, HibernateMetric>> collectionsStatMap = new TreeMap<>();

        if (statistics.isStatisticsEnabled()) {
            String[] collectionRoleNames = statistics.getCollectionRoleNames();
            Stream.of(collectionRoleNames).forEach(crN -> {
                CollectionStatistics cStats = statistics.getCollectionStatistics(crN);
                SortedMap<String, HibernateMetric> cStatMap = new TreeMap<>();

                // Collection cache stats
                cStatMap.put(
                        "cache.region.name",
                        createHibernateMetricItem(
                                "cacheRegionName",
                                "The name of the region where this data is cached.",
                                cStats.getCacheRegionName()));

                cStatMap.put(
                        "cache.hit.count",
                        createHibernateMetricItem(
                                "cacheHitCount",
                                "The number of successful cache look-ups for this data from its configured cache region since the last Statistics clearing.",
                                cStats.getCacheHitCount()));
                cStatMap.put(
                        "cache.miss.count",
                        createHibernateMetricItem(
                                "cacheMissCount",
                                "The number of unsuccessful cache look-ups for this data from its configured cache region since the last Statistics clearing.",
                                cStats.getCacheMissCount()));
                double secondLvlCacheHitsRatio = (cStats.getCacheHitCount() + cStats.getCacheMissCount()) != 0
                        ? ((double) cStats.getCacheHitCount() / (cStats.getCacheHitCount() + cStats.getCacheMissCount())
                                * 100)
                        : -1;
                cStatMap.put(
                        "cache.hit.ratio",
                        createHibernateMetricItem(
                                "cacheHitRatio",
                                "The ratio of successful cache look-ups for this data from its configured cache region since the last Statistics clearing.",
                                df2.format(secondLvlCacheHitsRatio)));
                cStatMap.put(
                        "cache.put.count",
                        createHibernateMetricItem(
                                "cachePutCount",
                                "The number of times this data has been into its configured cache region since the last Statistics clearing.",
                                cStats.getCachePutCount()));

                // Collection stats
                cStatMap.put(
                        "fetch.count",
                        createHibernateMetricItem(
                                "fetchCount",
                                "Number of times (since last Statistics clearing) this collection has been fetched.",
                                cStats.getFetchCount()));
                cStatMap.put(
                        "recreate.count",
                        createHibernateMetricItem(
                                "recreateCount",
                                "Number of times (since last Statistics clearing) this collection has been recreated (rows potentially deleted and then rows (re-)inserted).",
                                cStats.getRecreateCount()));
                cStatMap.put(
                        "remove.count",
                        createHibernateMetricItem(
                                "removeCount",
                                "Number of times (since last Statistics clearing) this collection has been removed.",
                                cStats.getRemoveCount()));
                cStatMap.put(
                        "load.count",
                        createHibernateMetricItem(
                                "loadCount",
                                "Number of times (since last Statistics clearing) this collection has been loaded.",
                                cStats.getLoadCount()));
                cStatMap.put(
                        "update.count",
                        createHibernateMetricItem(
                                "updateCount",
                                "Number of times (since last Statistics clearing) this collection has been updated.",
                                cStats.getUpdateCount()));

                collectionsStatMap.put(COLLECTION_STATS_PREFIX + crN, cStatMap);
            });
        }

        return collectionsStatMap;
    }

    /**
     * Get all the Hibernate generic statistics aggregated in a sorted Map
     * 
     * @param statistics
     * @return a sorted map containing Hibernate generic stats
     */
    public static SortedMap<String, HibernateMetric> getGenericStats(Statistics statistics) {

        SortedMap<String, HibernateMetric> genericStatsMap = new TreeMap<>();

        if (statistics.isStatisticsEnabled()) {

            // Sessions
            genericStatsMap.put(
                    "hibernate-orm.session.opened.count",
                    createHibernateMetricItem(
                            "sessionOpenCount",
                            "Global number of sessions opened.",
                            statistics.getSessionOpenCount()));
            genericStatsMap.put(
                    "hibernate-orm.session.closed.count",
                    createHibernateMetricItem(
                            "sessionCloseCount",
                            "Global number of sessions closed.",
                            statistics.getSessionCloseCount()));

            // Transactions
            genericStatsMap.put(
                    "hibernate-orm.transaction.count",
                    createHibernateMetricItem(
                            "transactionCount",
                            "The number of transactions we know to have completed.",
                            statistics.getTransactionCount()));
            genericStatsMap.put(
                    "hibernate-orm.transaction.successful.count",
                    createHibernateMetricItem(
                            "successfulTransactionCount",
                            "The number of transactions we know to have been successful.",
                            statistics.getSuccessfulTransactionCount()));

            // Connections
            genericStatsMap.put(
                    "hibernate-orm.connection.obtained.count",
                    createHibernateMetricItem(
                            "connectCount",
                            "Get the global number of connections asked by the sessions "
                                    + "(the actual number of connections used may be much smaller depending "
                                    + "whether you use a connection pool or not)",
                            statistics.getConnectCount()));

            // Optimistic lock failures
            genericStatsMap.put(
                    "hibernate-orm.optimistic.lock.failure.count",
                    createHibernateMetricItem(
                            "optimisticFailureCount",
                            "The number of Hibernate StaleObjectStateExceptions or JPA OptimisticLockExceptions that occurred.",
                            statistics.getOptimisticFailureCount()));

            // Flushes
            genericStatsMap.put(
                    "hibernate-orm.flush.count",
                    createHibernateMetricItem(
                            "flushCount",
                            "Global number of flush operations executed (either manual or automatic).",
                            statistics.getFlushCount()));

            // Prepared statements
            genericStatsMap.put(
                    "hibernate-orm.statement.prepared.count",
                    createHibernateMetricItem(
                            "prepareStatementCount",
                            "The number of prepared statements that were acquired.",
                            statistics.getPrepareStatementCount()));
            genericStatsMap.put(
                    "hibernate-orm.statement.closed.count",
                    createHibernateMetricItem(
                            "closeStatementCount",
                            "The number of prepared statements that were released.",
                            statistics.getCloseStatementCount()));

            // Second level cache
            genericStatsMap.put(
                    "hibernate-orm.second-level-cache.put.count",
                    createHibernateMetricItem(
                            "secondLevelCachePutCount",
                            "Global number of cacheable entities/collections put in the cache.",
                            statistics.getSecondLevelCachePutCount()));
            genericStatsMap.put(
                    "hibernate-orm.second-level-cache.hit.count",
                    createHibernateMetricItem(
                            "secondLevelCacheHitCount",
                            "Global number of cacheable entities/collections successfully retrieved from the cache.",
                            statistics.getSecondLevelCacheHitCount()));
            genericStatsMap.put(
                    "hibernate-orm.second-level-cache.miss.count",
                    createHibernateMetricItem(
                            "secondLevelCacheMissCount",
                            "Global number of cacheable entities/collections not found in the cache and loaded from the database.",
                            statistics.getSecondLevelCacheMissCount()));
            double secondLvlCacheHitsRatio = (statistics.getSecondLevelCacheHitCount() + statistics
                    .getSecondLevelCacheMissCount()) != 0 ? ((double) statistics.getSecondLevelCacheHitCount()
                            / (statistics.getSecondLevelCacheHitCount() + statistics.getSecondLevelCacheMissCount())
                            * 100) : -1;
            genericStatsMap.put(
                    "hibernate-orm.second-level-cache.hit.ratio",
                    createHibernateMetricItem(
                            "secondLevelCacheHitRatio",
                            "Ratio of number of cacheable entities/collections found in the cache and the not found in the cache and loaded from the database.",
                            df2.format(secondLvlCacheHitsRatio)));

            // Entities
            genericStatsMap.put(
                    "hibernate-orm.entities.load.count",
                    createHibernateMetricItem(
                            "entityLoadCount",
                            "Global number of entity loads.",
                            statistics.getEntityLoadCount()));
            genericStatsMap.put(
                    "hibernate-orm.entities.update.count",
                    createHibernateMetricItem(
                            "entityUpdateCount",
                            "Global number of entity updates.",
                            statistics.getEntityUpdateCount()));
            genericStatsMap.put(
                    "hibernate-orm.entities.insert.count",
                    createHibernateMetricItem(
                            "entityInsertCount",
                            "Global number of entity inserts.",
                            statistics.getEntityInsertCount()));
            genericStatsMap.put(
                    "hibernate-orm.entities.delete.count",
                    createHibernateMetricItem(
                            "entityDeleteCount",
                            "Global number of entity deletes.",
                            statistics.getEntityDeleteCount()));
            genericStatsMap.put(
                    "hibernate-orm.entities.fetch.count",
                    createHibernateMetricItem(
                            "entityFetchCount",
                            "Global number of entity fetches.",
                            statistics.getEntityFetchCount()));

            // Collections
            genericStatsMap.put(
                    "hibernate-orm.collections.update.count",
                    createHibernateMetricItem(
                            "collectionUpdateCount",
                            "Global number of collections updated.",
                            statistics.getCollectionUpdateCount()));
            genericStatsMap.put(
                    "hibernate-orm.collections.remove.count",
                    createHibernateMetricItem(
                            "collectionRemoveCount",
                            "Global number of collections removed.",
                            statistics.getCollectionRemoveCount()));
            genericStatsMap.put(
                    "hibernate-orm.collections.recreate.count",
                    createHibernateMetricItem(
                            "collectionRecreateCount",
                            "Global number of collections recreated.",
                            statistics.getCollectionRecreateCount()));
            genericStatsMap.put(
                    "hibernate-orm.collections.fetch.count",
                    createHibernateMetricItem(
                            "collectionFetchCount",
                            "Global number of collections fetched.",
                            statistics.getCollectionFetchCount()));
            genericStatsMap.put(
                    "hibernate-orm.collections.load.count",
                    createHibernateMetricItem(
                            "collectionLoadCount",
                            "Global number of collections loaded.",
                            statistics.getCollectionLoadCount()));

            // Natural id
            genericStatsMap.put(
                    "hibernate-orm.natural-id.cache.put.count",
                    createHibernateMetricItem(
                            "naturalIdCachePutCount",
                            "Global number of cacheable natural id lookups put in cache.",
                            statistics.getNaturalIdCachePutCount()));
            genericStatsMap.put(
                    "hibernate-orm.natural-id.cache.hit.count",
                    createHibernateMetricItem(
                            "naturalIdCacheHitCount",
                            "Global number of cached natural id lookups successfully retrieved from cache.",
                            statistics.getNaturalIdCacheHitCount()));
            genericStatsMap.put(
                    "hibernate-orm.natural-id.cache.miss.count",
                    createHibernateMetricItem(
                            "naturalIdCacheHitCount",
                            "Global number of cached natural id lookups *not* found in cache.",
                            statistics.getNaturalIdCacheMissCount()));
            double nIdHitsRatio = (statistics.getNaturalIdCacheHitCount()
                    + statistics.getNaturalIdCacheMissCount()) != 0
                            ? ((double) statistics.getNaturalIdCacheHitCount()
                                    / (statistics.getNaturalIdCacheHitCount() + statistics.getNaturalIdCacheMissCount())
                                    * 100)
                            : -1;
            genericStatsMap.put(
                    "hibernate-orm.natural-id.cache.hit.ratio",
                    createHibernateMetricItem(
                            "naturalIdCacheHitCount",
                            "Ratio of number of cacheable natural ids found in the cache and the not found in the cache and loaded from the database.",
                            df2.format(nIdHitsRatio)));

            // Natural id queries
            genericStatsMap.put(
                    "hibernate-orm.natural-id.query.execution.count",
                    createHibernateMetricItem(
                            "naturalIdQueryExecutionCount",
                            "Global number of natural id queries executed against the database.",
                            statistics.getNaturalIdQueryExecutionCount()));
            genericStatsMap.put(
                    "hibernate-orm.natural-id.query.execution.maxtime",
                    createHibernateMetricItem(
                            "naturalIdQueryExecutionMaxTime",
                            "Max time execution of natural id queries executed against the database.",
                            statistics.getNaturalIdQueryExecutionMaxTime()));
            genericStatsMap.put(
                    "hibernate-orm.natural-id.query.execution.maxtime.region",
                    createHibernateMetricItem(
                            "naturalIdQueryExecutionMaxTimeRegion",
                            "Max time region of natural id queries executed against the database.",
                            statistics.getNaturalIdQueryExecutionMaxTimeRegion()));
            genericStatsMap.put(
                    "hibernate-orm.natural-id.query.execution.maxtime.entity",
                    createHibernateMetricItem(
                            "naturalIdQueryExecutionMaxTimeEntity",
                            "Max time entity of natural id queries executed against the database.",
                            statistics.getNaturalIdQueryExecutionMaxTimeEntity()));

            // Queries
            genericStatsMap.put(
                    "hibernate-orm.query.execution.count",
                    createHibernateMetricItem(
                            "queryExecutionCount",
                            "Global number of executed queries.",
                            statistics.getQueryExecutionCount()));
            genericStatsMap.put(
                    "hibernate-orm.query.execution.maxtime",
                    createHibernateMetricItem(
                            "queryExecutionMaxTime",
                            "Max execution time of executed queries.",
                            statistics.getQueryExecutionMaxTime()));
            genericStatsMap.put(
                    "hibernate-orm.query.cache.put.count",
                    createHibernateMetricItem(
                            "queryCachePutCount",
                            "Global number of cacheable queries put in cache.",
                            statistics.getQueryCachePutCount()));
            genericStatsMap.put(
                    "hibernate-orm.query.cache.hit.count",
                    createHibernateMetricItem(
                            "queryCacheHitCount",
                            "Global number of cached queries successfully retrieved from cache.",
                            statistics.getQueryCacheHitCount()));
            genericStatsMap.put(
                    "hibernate-orm.query.cache.miss.count",
                    createHibernateMetricItem(
                            "queryCacheMissCount",
                            "Global number of cached queries *not* found in cache.",
                            statistics.getQueryCacheMissCount()));

            // Timestamp
            genericStatsMap.put(
                    "hibernate-orm.timestamp.cache.put.count",
                    createHibernateMetricItem(
                            "updateTimestampsCachePutCount",
                            "Global number of timestamps put in cache.",
                            statistics.getUpdateTimestampsCachePutCount()));
            genericStatsMap.put(
                    "hibernate-orm.timestamp.cache.hit.count",
                    createHibernateMetricItem(
                            "updateTimestampsCacheHitCount",
                            "Global number of timestamps successfully retrieved from cache.",
                            statistics.getUpdateTimestampsCacheHitCount()));
            genericStatsMap.put(
                    "hibernate-orm.timestamp.cache.miss.count",
                    createHibernateMetricItem(
                            "updateTimestampsCacheMissCount",
                            "Global number of timestamp requests that were not found in the cache.",
                            statistics.getUpdateTimestampsCacheMissCount()));

        }

        return genericStatsMap;
    }

    private static HibernateMetric createHibernateMetricItem(String name, String description, double value) {
        return new HibernateMetric(name, description, value);
    }

    private static HibernateMetric createHibernateMetricItem(String name, String description, String strValue) {
        return new HibernateMetric(name, description, strValue);
    }
}
