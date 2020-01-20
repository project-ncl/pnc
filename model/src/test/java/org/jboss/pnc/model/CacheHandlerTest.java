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
package org.jboss.pnc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;
import org.jboss.pnc.model.utils.HibernateStatsUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheHandlerTest extends AbstractModelTest {

    protected Logger logger = LoggerFactory.getLogger(CacheHandlerTest.class);

    /** located in src/test/resources */
    final static String DBUNIT_DATASET_FILE = "basic-model-test-data.xml";

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder.newBuilder().id(1)
            .build();

    private EntityManager em_1;
    private EntityManager em_2;
    private EntityManager em_3;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();

        // Initialize data from xml dataset file
        em_1 = getEmFactory().createEntityManager();
        initDatabaseUsingDataset(em_1, DBUNIT_DATASET_FILE);

    }

    @After
    public void cleanup() {
        clearDatabaseTables();

        em_1.close();
        if (em_2 != null) {
            em_2.close();
        }
        if (em_3 != null) {
            em_3.close();
        }
    }

    @Test
    public void testMappedEntitiesStats() {

        Session session = (Session) em_1.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        SortedMap<String, Map<String, String>> entitiesStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheEntitiesStats(statistics);
        logger.debug("All entities stats: {}", entitiesStatMap);

        String[] mappedEntities = { "org.hibernate.envers.DefaultRevisionEntity", "org.jboss.pnc.model.Artifact",
                "org.jboss.pnc.model.BuildConfigSetRecord", "org.jboss.pnc.model.BuildConfiguration",
                "org.jboss.pnc.model.BuildConfigurationSet", "org.jboss.pnc.model.BuildConfiguration_AUD",
                "org.jboss.pnc.model.BuildEnvironment", "org.jboss.pnc.model.BuildRecord",
                "org.jboss.pnc.model.BuildRecordPushResult", "org.jboss.pnc.model.License", "org.jboss.pnc.model.Product",
                "org.jboss.pnc.model.ProductMilestone", "org.jboss.pnc.model.ProductMilestoneRelease",
                "org.jboss.pnc.model.ProductRelease", "org.jboss.pnc.model.ProductVersion", "org.jboss.pnc.model.Project",
                "org.jboss.pnc.model.RepositoryConfiguration", "org.jboss.pnc.model.TargetRepository",
                "org.jboss.pnc.model.User", "build_configuration_parameters_AUD" };
        Set<String> mappedEntitiesSet = new HashSet<String>(Arrays.asList(mappedEntities));
        assertTrue(entitiesStatMap.keySet().containsAll(mappedEntitiesSet));
    }

    @Test
    public void testMappedSecondLevelCacheStats() {

        Session session = (Session) em_1.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        SortedMap<String, Map<String, String>> secondLevelCacheStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheRegionsStats(statistics);
        logger.debug("All second level cache stats: {}", secondLevelCacheStatMap);

        String[] mappedEntities = { "org.jboss.pnc.model.Artifact", "org.jboss.pnc.model.BuildConfigSetRecord",
                "org.jboss.pnc.model.BuildConfiguration", "org.jboss.pnc.model.BuildConfigurationSet",
                "org.jboss.pnc.model.BuildEnvironment", "org.jboss.pnc.model.BuildRecord",
                "org.jboss.pnc.model.BuildRecordPushResult", "org.jboss.pnc.model.License", "org.jboss.pnc.model.Product",
                "org.jboss.pnc.model.ProductMilestone", "org.jboss.pnc.model.ProductMilestoneRelease",
                "org.jboss.pnc.model.ProductRelease", "org.jboss.pnc.model.ProductVersion", "org.jboss.pnc.model.Project",
                "org.jboss.pnc.model.RepositoryConfiguration", "org.jboss.pnc.model.TargetRepository",
                "org.jboss.pnc.model.User" };

        Set<String> mappedEntitiesSet = new HashSet<String>(Arrays.asList(mappedEntities));
        assertTrue(secondLevelCacheStatMap.keySet().containsAll(mappedEntitiesSet));
    }

    @Test
    public void testMappedCollectionsStats() {

        Session session = (Session) em_1.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        SortedMap<String, Map<String, String>> collectionStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheCollectionsStats(statistics);
        logger.debug("All collection stats: {}", collectionStatMap);

        String[] mappedCollections = { "org.jboss.pnc.model.BuildConfigurationSet.buildConfigurations",
                "org.jboss.pnc.model.BuildConfigSetRecord.buildRecords",
                "org.jboss.pnc.model.ProductVersion.buildConfigurations", "org.jboss.pnc.model.BuildRecord.attributes",
                "org.jboss.pnc.model.TargetRepository.artifacts", "org.jboss.pnc.model.BuildConfiguration.genericParameters",
                "org.jboss.pnc.model.ProductMilestone.performedBuilds", "org.jboss.pnc.model.BuildConfiguration.dependants",
                "org.jboss.pnc.model.BuildRecord.dependencies",
                "org.jboss.pnc.model.RepositoryConfiguration.buildConfigurations",
                "org.jboss.pnc.model.BuildConfiguration.dependencies",
                "org.jboss.pnc.model.Artifact.distributedInProductMilestones", "org.jboss.pnc.model.ProductVersion.attributes",
                "org.jboss.pnc.model.User.buildRecords", "org.jboss.pnc.model.BuildEnvironment.attributes",
                "org.jboss.pnc.model.BuildRecord.builtArtifacts", "org.jboss.pnc.model.BuildConfigSetRecord.attributes",
                "org.jboss.pnc.model.ProductMilestone.distributedArtifacts", "org.jboss.pnc.model.Project.buildConfigurations",
                "org.jboss.pnc.model.ProductVersion.buildConfigurationSets",
                "org.jboss.pnc.model.BuildRecord.buildRecordPushResults", "org.jboss.pnc.model.Artifact.dependantBuildRecords",
                "org.jboss.pnc.model.Artifact.buildRecords", "org.jboss.pnc.model.Product.productVersions",
                "org.jboss.pnc.model.ProductVersion.productMilestones",
                "org.jboss.pnc.model.BuildConfiguration.buildConfigurationSets",
                "org.jboss.pnc.model.BuildConfigurationSet.buildConfigSetRecords" };
        Set<String> mappedCollectionsSet = new HashSet<String>(Arrays.asList(mappedCollections));
        assertTrue(collectionStatMap.keySet().containsAll(mappedCollectionsSet));
    }

    @Test
    public void testSecondLevelCache() {

        // Session 1
        Session session_1 = (Session) em_1.getDelegate();
        SessionFactory sessionFactory_1 = session_1.getSessionFactory();

        sessionFactory_1.getStatistics().clear();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers Auditing
        BuildConfiguration buildConfig1 = BuildConfiguration.Builder.newBuilder().name("Test Build Configuration 1")
                .description("Test Build Configuration 1 Description").project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(basicRepositoryConfiguration).buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build()).build();

        BuildConfiguration buildConfig2 = BuildConfiguration.Builder.newBuilder().name("Test Build Configuration 2")
                .description("Test Build Configuration 2 Description").project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(basicRepositoryConfiguration).buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build()).build();

        em_1.getTransaction().begin();
        em_1.persist(buildConfig1);
        em_1.persist(buildConfig2);
        em_1.getTransaction().commit();

        //
        printSessionStats(session_1.getStatistics());
        printSessionFactoryStats(sessionFactory_1.getStatistics(), "org.jboss.pnc.model.BuildConfiguration");

        SortedMap<String, Map<String, String>> entitiesStatMap_1 = HibernateStatsUtils
                .getAllSecondLevelCacheEntitiesStats(sessionFactory_1.getStatistics());
        SortedMap<String, Map<String, String>> secondLevelCacheStatMap_1 = HibernateStatsUtils
                .getAllSecondLevelCacheRegionsStats(sessionFactory_1.getStatistics());

        session_1.load(BuildConfiguration.class, buildConfig1.getId());
        session_1.load(BuildConfiguration.class, buildConfig2.getId());

        // 2 BuildConfigurations were inserted in SESSION_1, and should be inside 1st level cache of SESSION_1
        assertEquals(2, session_1.getStatistics().getEntityCount());

        // The 2 BuildConfigurations inserts in SESSION_1 should have been propagated to 2nd level cache
        assertEquals("2", entitiesStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("insertCount"));
        assertEquals("2", entitiesStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("cachePutCount"));
        assertEquals("2", secondLevelCacheStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("elementCountInMemory"));
        assertEquals("2", secondLevelCacheStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("putCount"));

        // No BuildConfiguration was searched in SESSION_1, so there should be no misses nor hits in the 2nd level cache
        assertEquals("0", entitiesStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("cacheHitCount"));
        assertEquals("0", entitiesStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("cacheMissCount"));
        assertEquals("0", secondLevelCacheStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("missCount"));
        assertEquals("0", secondLevelCacheStatMap_1.get("org.jboss.pnc.model.BuildConfiguration").get("hitCount"));

        // Searching a not existing BuildConfiguration --> 1 miss in SESSION_1
        session_1.find(BuildConfiguration.class, 13);
        // Searching an existing BuildConfiguration already in 1st level cache of SESSION_1 --> no hits in 2nd level cache as
        // there is no need to go there
        session_1.find(BuildConfiguration.class, buildConfig1.getId());

        // Still 2 BuildConfigurations in 1st level cache of SESSION_1
        assertEquals(2, session_1.getStatistics().getEntityCount());

        entitiesStatMap_1 = HibernateStatsUtils.getAllSecondLevelCacheEntitiesStats(sessionFactory_1.getStatistics());
        secondLevelCacheStatMap_1 = HibernateStatsUtils.getAllSecondLevelCacheRegionsStats(sessionFactory_1.getStatistics());

        // Still 2 BuildConfigurations in 2nd level cache
        Map<String, String> firstLvlCache1_bc = entitiesStatMap_1.get("org.jboss.pnc.model.BuildConfiguration");
        Map<String, String> secondLvlCache1_bc = secondLevelCacheStatMap_1.get("org.jboss.pnc.model.BuildConfiguration");
        assertEquals("2", firstLvlCache1_bc.get("insertCount"));
        assertEquals("2", firstLvlCache1_bc.get("cachePutCount"));
        assertEquals("2", secondLvlCache1_bc.get("elementCountInMemory"));
        assertEquals("2", secondLvlCache1_bc.get("putCount"));

        // There should be 1 miss from 1st and 2nd level cache, and no hits (BuildConfiguration was found in 1st level cache)
        assertEquals("0", firstLvlCache1_bc.get("cacheHitCount"));
        assertEquals("1", firstLvlCache1_bc.get("cacheMissCount"));
        assertEquals("1", secondLvlCache1_bc.get("missCount"));
        assertEquals("0", secondLvlCache1_bc.get("hitCount"));

        // Create another session SESSION_2
        em_2 = getEmFactory().createEntityManager();
        Session session_2 = (Session) em_2.getDelegate();
        SessionFactory sessionFactory_2 = session_2.getSessionFactory();

        // No BuildConfiguration were inserted in SESSION_2, 1st level cache of SESSION_2 should be empty
        assertEquals(0, session_2.getStatistics().getEntityCount());

        SortedMap<String, Map<String, String>> entitiesStatMap_2 = HibernateStatsUtils
                .getAllSecondLevelCacheEntitiesStats(sessionFactory_2.getStatistics());
        SortedMap<String, Map<String, String>> secondLevelCacheStatMap_2 = HibernateStatsUtils
                .getAllSecondLevelCacheRegionsStats(sessionFactory_2.getStatistics());

        Map<String, String> firstLvlCache2_bc = entitiesStatMap_2.get("org.jboss.pnc.model.BuildConfiguration");
        Map<String, String> secondLvlCache2_bc = secondLevelCacheStatMap_2.get("org.jboss.pnc.model.BuildConfiguration");

        // 2nd level cache is unique, so entitiesStatMap_1 should be identical to entitiesStatMap_2
        assertEquals(firstLvlCache1_bc.get("insertCount"), firstLvlCache2_bc.get("insertCount"));
        assertEquals(firstLvlCache1_bc.get("cachePutCount"), firstLvlCache2_bc.get("cachePutCount"));
        assertEquals(secondLvlCache1_bc.get("elementCountInMemory"), secondLvlCache2_bc.get("elementCountInMemory"));
        assertEquals(secondLvlCache1_bc.get("putCount"), secondLvlCache2_bc.get("putCount"));
        assertEquals(firstLvlCache1_bc.get("cacheHitCount"), firstLvlCache2_bc.get("cacheHitCount"));
        assertEquals(firstLvlCache1_bc.get("cacheMissCount"), firstLvlCache2_bc.get("cacheMissCount"));
        assertEquals(secondLvlCache1_bc.get("missCount"), secondLvlCache2_bc.get("missCount"));
        assertEquals(secondLvlCache1_bc.get("hitCount"), secondLvlCache2_bc.get("hitCount"));

        // Searching an existing BuildConfiguration not in 1st level cache of SESSION_2 --> should be a hit in 2nd level cache
        session_2.find(BuildConfiguration.class, buildConfig1.getId());

        //
        printSessionStats(session_2.getStatistics());
        printSessionFactoryStats(sessionFactory_2.getStatistics(), "org.jboss.pnc.model.BuildConfiguration");

        entitiesStatMap_1 = HibernateStatsUtils.getAllSecondLevelCacheEntitiesStats(sessionFactory_1.getStatistics());
        secondLevelCacheStatMap_1 = HibernateStatsUtils.getAllSecondLevelCacheRegionsStats(sessionFactory_1.getStatistics());
        entitiesStatMap_2 = HibernateStatsUtils.getAllSecondLevelCacheEntitiesStats(sessionFactory_2.getStatistics());
        secondLevelCacheStatMap_2 = HibernateStatsUtils.getAllSecondLevelCacheRegionsStats(sessionFactory_2.getStatistics());

        firstLvlCache1_bc = entitiesStatMap_1.get("org.jboss.pnc.model.BuildConfiguration");
        secondLvlCache1_bc = secondLevelCacheStatMap_1.get("org.jboss.pnc.model.BuildConfiguration");
        firstLvlCache2_bc = entitiesStatMap_2.get("org.jboss.pnc.model.BuildConfiguration");
        secondLvlCache2_bc = secondLevelCacheStatMap_2.get("org.jboss.pnc.model.BuildConfiguration");

        // 2nd level cache should now contain a HIT (BuildConfiguration#100 was not found in SESSION_2 and so was taken from 2nd
        // level cache)
        assertEquals("1", firstLvlCache1_bc.get("cacheHitCount"));
        assertEquals("1", secondLvlCache1_bc.get("hitCount"));

        // All other stats should be identical to previous ones
        assertEquals("2", firstLvlCache1_bc.get("insertCount"));
        assertEquals("2", firstLvlCache1_bc.get("cachePutCount"));
        assertEquals("2", secondLvlCache1_bc.get("elementCountInMemory"));
        assertEquals("2", secondLvlCache1_bc.get("putCount"));
        assertEquals("1", firstLvlCache1_bc.get("cacheMissCount"));
        assertEquals("1", secondLvlCache1_bc.get("missCount"));

        // 2nd level cache is unique, so again entitiesStatMap_1 should be identical to entitiesStatMap_2
        assertEquals(firstLvlCache1_bc.get("insertCount"), firstLvlCache2_bc.get("insertCount"));
        assertEquals(firstLvlCache1_bc.get("cachePutCount"), firstLvlCache2_bc.get("cachePutCount"));
        assertEquals(secondLvlCache1_bc.get("elementCountInMemory"), secondLvlCache2_bc.get("elementCountInMemory"));
        assertEquals(secondLvlCache1_bc.get("putCount"), secondLvlCache2_bc.get("putCount"));
        assertEquals(firstLvlCache1_bc.get("cacheHitCount"), firstLvlCache2_bc.get("cacheHitCount"));
        assertEquals(firstLvlCache1_bc.get("cacheMissCount"), firstLvlCache2_bc.get("cacheMissCount"));
        assertEquals(secondLvlCache1_bc.get("missCount"), secondLvlCache2_bc.get("missCount"));
        assertEquals(secondLvlCache1_bc.get("hitCount"), secondLvlCache2_bc.get("hitCount"));
    }

    @Test
    public void testFirstLevelCacheEviction() {

        // Session 3
        em_3 = getEmFactory().createEntityManager();
        Session session_3 = (Session) em_3.getDelegate();
        SessionFactory sessionFactory_3 = session_3.getSessionFactory();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        BuildConfiguration buildConfig3 = BuildConfiguration.Builder.newBuilder().name("Test Build Configuration 3")
                .description("Test Build Configuration 3 Description").project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(basicRepositoryConfiguration).buildScript("mvn clean install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build()).build();

        // Persist in Session 1
        em_1.getTransaction().begin();
        em_1.persist(buildConfig3);
        em_1.getTransaction().commit();

        Integer newBCId = buildConfig3.getId();

        // Entity is fetched very first time
        BuildConfiguration bc = (BuildConfiguration) session_3.load(BuildConfiguration.class, newBCId);

        SortedMap<String, String> genericStatMap_1 = HibernateStatsUtils.getGenericStats(sessionFactory_3.getStatistics());
        int entityFetchCount1 = Integer.parseInt(genericStatMap_1.get("entityFetchCount"));
        int secondLevelCacheHitCount1 = Integer.parseInt(genericStatMap_1.get("secondLevelCacheHitCount"));

        // fetch the BuildConfiguration entity again, no change in fetch count from 1st level cache nor access to 2nd level
        // cache as there is no need for it
        bc = (BuildConfiguration) session_3.load(BuildConfiguration.class, newBCId);

        SortedMap<String, String> genericStatMap_2 = HibernateStatsUtils.getGenericStats(sessionFactory_3.getStatistics());
        int entityFetchCount2 = Integer.parseInt(genericStatMap_2.get("entityFetchCount"));
        int secondLevelCacheHitCount2 = Integer.parseInt(genericStatMap_2.get("secondLevelCacheHitCount"));

        // No change in fetch from 1st and 2nd level caches
        assertEquals(entityFetchCount1, entityFetchCount2);
        assertEquals(secondLevelCacheHitCount2, secondLevelCacheHitCount2);

        // Evict from first level cache
        session_3.evict(bc);

        // fetch one more time
        bc = (BuildConfiguration) session_3.load(BuildConfiguration.class, newBCId);

        SortedMap<String, String> genericStatMap_3 = HibernateStatsUtils.getGenericStats(sessionFactory_3.getStatistics());
        int entityFetchCount3 = Integer.parseInt(genericStatMap_3.get("entityFetchCount"));
        int secondLevelCacheHitCount3 = Integer.parseInt(genericStatMap_3.get("secondLevelCacheHitCount"));

        // No change in fetch from 1st level cache as entity is not there anymore
        assertEquals(entityFetchCount2, entityFetchCount3);
        // Change in fetch from 2nd level cache: the entity is not in 1st level cache anymore, so Hibernate gets it from 2nd
        // level
        assertNotEquals(secondLevelCacheHitCount2, secondLevelCacheHitCount3);

        logger.debug("Entity fetch count #1: {}, #2: {}, #3: {}", entityFetchCount1, entityFetchCount2, entityFetchCount3);
        logger.debug("Second level cache hit count #1: {}, #2: {}, #3: {}", secondLevelCacheHitCount1,
                secondLevelCacheHitCount2, secondLevelCacheHitCount3);
    }

    private void printSessionStats(SessionStatistics sessionStatistics) {
        logger.debug("--- FIRST LEVEL STATS ---");
        logger.debug("Session stats collectionCount: {}", sessionStatistics.getCollectionCount());
        logger.debug("Session stats collectionKeys: {}", sessionStatistics.getCollectionKeys());
        logger.debug("Session stats entityCount: {}", sessionStatistics.getEntityCount());
        logger.debug("Session stats entityKeys: {}", sessionStatistics.getEntityKeys());
        logger.debug("--- --- --- --- --- ---");
    }

    @SuppressWarnings("unused")
    private void printSessionFactoryStats(Statistics statistics) {
        SortedMap<String, Map<String, String>> entitiesStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheEntitiesStats(statistics);
        SortedMap<String, Map<String, String>> secondLevelCacheStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheRegionsStats(statistics);
        SortedMap<String, Map<String, String>> collectionStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheCollectionsStats(statistics);

        logger.debug("All second level cache stats: {}", secondLevelCacheStatMap);
        logger.debug("All entities stats: {}", entitiesStatMap);
        logger.debug("All collection stats: {}", collectionStatMap);
    }

    private void printSessionFactoryStats(Statistics statistics, String regionName) {
        SortedMap<String, Map<String, String>> entitiesStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheEntitiesStats(statistics);
        SortedMap<String, Map<String, String>> secondLevelCacheStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheRegionsStats(statistics);
        SortedMap<String, Map<String, String>> collectionStatMap = HibernateStatsUtils
                .getAllSecondLevelCacheCollectionsStats(statistics);

        logger.debug("--- SECOND LEVEL STATS ---");
        logger.debug("Entities stats of {}: {}", regionName, entitiesStatMap.get(regionName));
        logger.debug("Collection stats of {} : {}", regionName, collectionStatMap.get(regionName));
        logger.debug("Second level cache stats of {}: {}", regionName, secondLevelCacheStatMap.get(regionName));
        logger.debug("--- --- --- --- --- ---");
    }

}
