package org.jboss.pnc.model;

import java.lang.invoke.MethodHandles;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheHandlerTest extends AbstractModelTest {

    protected Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /** located in src/test/resources */
    final static String DBUNIT_DATASET_FILE = "basic-model-test-data.xml";

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder
            .newBuilder().id(1).build();

    private EntityManager em;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();

        // Initialize data from xml dataset file
        EntityManager em = getEmFactory().createEntityManager();
        initDatabaseUsingDataset(em, DBUNIT_DATASET_FILE);

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers Auditing
        insertExampleBuildConfigurations(em, basicRepositoryConfiguration);
        em.close();
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
    }

    @Test
    public void testCache() {
        
        Session session = (Session) em.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();

        String[] collectionRoleNames = statistics.getCollectionRoleNames();
        Stream.of(collectionRoleNames).forEach(crN -> logger.debug("Collection role name: {}", crN));
        
        long connectionsCount = statistics.getConnectCount();
        logger.debug("Connections count: {}", connectionsCount);
        
        String[] entityNames = statistics.getEntityNames();
        Stream.of(entityNames).forEach(eN -> {
            EntityStatistics entityStatistics = statistics.getEntityStatistics(eN);
            logger.debug("Entity name: {}, "
                    + "cacheHitCount: {}, "
                    + "cacheMissCount: {}, "
                    + "cachePutCount: {}, "
                    + "cacheRegionName: {}, "
                    + "deleteCount: {}, "
                    + "fetchCount: {}, "
                    + "insertCount: {}, "
                    + "loadCount: {}, "
                    + "optimisticFailureCount: {}, "
                    + "updateCount: {}", 
                    eN,
                    entityStatistics.getCacheHitCount(),
                    entityStatistics.getCacheMissCount(),
                    entityStatistics.getCachePutCount(),
                    entityStatistics.getCacheRegionName(),
                    entityStatistics.getDeleteCount(),
                    entityStatistics.getFetchCount(),
                    entityStatistics.getInsertCount(),
                    entityStatistics.getLoadCount(),
                    entityStatistics.getOptimisticFailureCount(),
                    entityStatistics.getUpdateCount());
        });

        long queryExecutionMaxTime = statistics.getQueryExecutionMaxTime();
        String queryExecutionMaxTimeQueryString = statistics.getQueryExecutionMaxTimeQueryString();
        logger.debug("ExecutionMaxTime: {}, ExecutionMaxTimeQueryString : {}", queryExecutionMaxTime, queryExecutionMaxTimeQueryString);

        long secondLevelCacheHitCount = statistics.getSecondLevelCacheHitCount();
        long secondLevelCacheMissCount = statistics.getSecondLevelCacheMissCount();
        long secondLevelCachePutCount = statistics.getSecondLevelCachePutCount();
        double hitRatio = (double) secondLevelCacheHitCount / ( secondLevelCacheHitCount + secondLevelCacheMissCount );
        logger.debug("secondLevelCacheHitCount: {}, secondLevelCacheMissCount : {}, secondLevelCachePutCount: {}, hitRatio : {}", secondLevelCacheHitCount, secondLevelCacheMissCount, secondLevelCachePutCount, hitRatio);

        String[] cacheRegionNames = statistics.getSecondLevelCacheRegionNames();
        Stream.of(cacheRegionNames).forEach(crN -> {
            CacheRegionStatistics secondLevelCacheStatistics = statistics.getDomainDataRegionStatistics(crN);
            logger.debug("Cache region name: {}, "
                    + "elementCountInMemory: {}, "
                    + "elementCountOnDisk: {}, "
                    + "hitCount: {}, "
                    + "missCount: {}, "
                    + "putCount: {}, "
                    + "regionName: {}, "
                    + "sizeInMemory: {}", 
                    crN,
                    secondLevelCacheStatistics.getElementCountInMemory(),
                    secondLevelCacheStatistics.getElementCountOnDisk(),
                    secondLevelCacheStatistics.getHitCount(),
                    secondLevelCacheStatistics.getMissCount(),
                    secondLevelCacheStatistics.getPutCount(),
                    secondLevelCacheStatistics.getRegionName(),
                    secondLevelCacheStatistics.getSizeInMemory());
        });
    }



}
