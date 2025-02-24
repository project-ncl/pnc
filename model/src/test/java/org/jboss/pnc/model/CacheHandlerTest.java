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
package org.jboss.pnc.model;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;
import org.jboss.pnc.model.utils.HibernateMetric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import static org.jboss.pnc.model.utils.HibernateStatsUtils.COLLECTION_STATS_PREFIX;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.ENTITY_STATS_PREFIX;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.REGION_STATS_PREFIX;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.getGenericStats;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.getSecondLevelCacheCollectionsStats;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.getSecondLevelCacheEntitiesStats;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.getSecondLevelCacheRegionsStats;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CacheHandlerTest extends AbstractModelTest {

    protected Logger logger = LoggerFactory.getLogger(CacheHandlerTest.class);

    /** located in src/test/resources */
    final static String DBUNIT_DATASET_FILE = "basic-model-test-data.xml";

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
            .id(1)
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

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers
        // Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        SortedMap<String, Map<String, HibernateMetric>> entitiesStatMap = getSecondLevelCacheEntitiesStats(statistics);
        logger.debug("All entities stats: {}", entitiesStatMap);

        String[] mappedEntities = {
                // ENTITY_STATS_PREFIX + "org.jboss.pnc.model.Artifact",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigSetRecord",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigurationSet",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration_AUD",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildEnvironment",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.Product",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.ProductMilestone",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.ProductRelease",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.ProductVersion",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.Project",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.RepositoryConfiguration",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.TargetRepository",
                ENTITY_STATS_PREFIX + "org.jboss.pnc.model.User",
                ENTITY_STATS_PREFIX + "build_configuration_parameters_AUD" };
        Set<String> mappedEntitiesSet = new HashSet<>(Arrays.asList(mappedEntities));
        assertTrue(entitiesStatMap.keySet().containsAll(mappedEntitiesSet));
    }

    @Test
    public void testMappedSecondLevelCacheStats() {

        Session session = (Session) em_1.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers
        // Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        SortedMap<String, Map<String, HibernateMetric>> secondLevelCacheStatMap = getSecondLevelCacheRegionsStats(
                statistics);
        logger.debug("All second level cache stats: {}", secondLevelCacheStatMap);

        String[] mappedEntities = { REGION_STATS_PREFIX + "org.jboss.pnc.model.Artifact",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigSetRecord",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigurationSet",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildEnvironment",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.Product",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.ProductMilestone",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.ProductRelease",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.ProductVersion",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.Project",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.RepositoryConfiguration",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.TargetRepository",
                REGION_STATS_PREFIX + "org.jboss.pnc.model.User" };

        Set<String> mappedEntitiesSet = new HashSet<>(Arrays.asList(mappedEntities));
        assertTrue(secondLevelCacheStatMap.keySet().containsAll(mappedEntitiesSet));
    }

    @Test
    public void testMappedCollectionsStats() {

        Session session = (Session) em_1.getDelegate();
        SessionFactory sessionFactory = session.getSessionFactory();
        Statistics statistics = sessionFactory.getStatistics();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers
        // Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        SortedMap<String, Map<String, HibernateMetric>> collectionStatMap = getSecondLevelCacheCollectionsStats(
                statistics);
        logger.debug("All collection stats: {}", collectionStatMap);

        String[] mappedCollections = {
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigurationSet.buildConfigurations",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigSetRecord.buildRecords",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.ProductVersion.buildConfigurations",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.TargetRepository.artifacts",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration.genericParameters",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.ProductMilestone.performedBuilds",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration.dependants",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.RepositoryConfiguration.buildConfigurations",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration.dependencies",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.Artifact.deliveredInProductMilestones",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.ProductVersion.attributes",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.User.buildRecords",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildEnvironment.attributes",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigSetRecord.attributes",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.ProductMilestone.deliveredArtifacts",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.Project.buildConfigurations",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.ProductVersion.buildConfigurationSets",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.Artifact.dependantBuildRecords",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.Product.productVersions",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.ProductVersion.productMilestones",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration.buildConfigurationSets",
                COLLECTION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfigurationSet.buildConfigSetRecords" };
        Set<String> mappedCollectionsSet = new HashSet<>(Arrays.asList(mappedCollections));
        assertTrue(collectionStatMap.keySet().containsAll(mappedCollectionsSet));
    }

    @Test
    public void testSecondLevelCache() {

        // Session 1
        Session session_1 = (Session) em_1.getDelegate();
        SessionFactory sessionFactory_1 = session_1.getSessionFactory();

        sessionFactory_1.getStatistics().clear();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers
        // Auditing
        BuildConfiguration buildConfig1 = BuildConfiguration.Builder.newBuilder()
                .id(1)
                .name("Test Build Configuration 1")
                .description("Test Build Configuration 1 Description")
                .project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build())
                .build();

        BuildConfiguration buildConfig2 = BuildConfiguration.Builder.newBuilder()
                .id(2)
                .name("Test Build Configuration 2")
                .description("Test Build Configuration 2 Description")
                .project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build())
                .build();

        em_1.getTransaction().begin();
        em_1.persist(buildConfig1);
        em_1.persist(buildConfig2);
        em_1.getTransaction().commit();

        //
        printSessionStats(session_1.getStatistics());
        printSessionFactoryStats(sessionFactory_1.getStatistics(), "org.jboss.pnc.model.BuildConfiguration");

        session_1.load(BuildConfiguration.class, buildConfig1.getId());
        session_1.load(BuildConfiguration.class, buildConfig2.getId());

        SortedMap<String, Map<String, HibernateMetric>> entitiesStats = getSecondLevelCacheEntitiesStats(
                sessionFactory_1.getStatistics());
        SortedMap<String, Map<String, HibernateMetric>> secondLevelCacheStats = getSecondLevelCacheRegionsStats(
                sessionFactory_1.getStatistics());
        Map<String, HibernateMetric> bcFirstLevelCacheStats = entitiesStats
                .get(ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");
        Map<String, HibernateMetric> bcSecondLevelCacheStats = secondLevelCacheStats
                .get(REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");

        // 2 BuildConfigurations were inserted in SESSION_1, and should be inside 1st level cache of SESSION_1
        assertEquals(2, session_1.getStatistics().getEntityCount());

        // The 2 BuildConfigurations inserts in SESSION_1 should have been propagated to 2nd level cache
        // Also, no BuildConfiguration was searched in SESSION_1, so there should be no misses nor hits in the 2nd level
        // cache

        assertEquals("2.0", bcFirstLevelCacheStats.get("insert.count").getValue());
        assertEquals("2.0", bcFirstLevelCacheStats.get("cache.put.count").getValue());
        assertEquals("0.0", bcFirstLevelCacheStats.get("cache.hit.count").getValue());
        assertEquals("0.0", bcFirstLevelCacheStats.get("cache.miss.count").getValue());

        assertEquals("2.0", bcSecondLevelCacheStats.get("second-level-cache.element.count.in.memory").getValue());
        assertEquals("2.0", bcSecondLevelCacheStats.get("second-level-cache.put.count").getValue());
        assertEquals("0.0", bcSecondLevelCacheStats.get("second-level-cache.miss.count").getValue());
        assertEquals("0.0", bcSecondLevelCacheStats.get("second-level-cache.hit.count").getValue());

        // Searching a not existing BuildConfiguration --> 1 miss in SESSION_1
        session_1.find(BuildConfiguration.class, 13);
        // Searching an existing BuildConfiguration already in 1st level cache of SESSION_1 --> no hits in 2nd level
        // cache as
        // there is no need to go there
        session_1.find(BuildConfiguration.class, buildConfig1.getId());

        // Still 2 BuildConfigurations in 1st level cache of SESSION_1
        assertEquals(2, session_1.getStatistics().getEntityCount());

        // REFRESH STATS
        entitiesStats = getSecondLevelCacheEntitiesStats(sessionFactory_1.getStatistics());
        secondLevelCacheStats = getSecondLevelCacheRegionsStats(sessionFactory_1.getStatistics());
        bcFirstLevelCacheStats = entitiesStats.get(ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");
        bcSecondLevelCacheStats = secondLevelCacheStats
                .get(REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");

        // Still 2 BuildConfigurations in 2nd level cache
        // There should be 1 miss from 1st and 2nd level cache, and no hits (BuildConfiguration was found in 1st level
        // cache)
        assertEquals("2.0", bcFirstLevelCacheStats.get("insert.count").getValue());
        assertEquals("2.0", bcFirstLevelCacheStats.get("cache.put.count").getValue());
        assertEquals("0.0", bcFirstLevelCacheStats.get("cache.hit.count").getValue());
        assertEquals("1.0", bcFirstLevelCacheStats.get("cache.miss.count").getValue());

        assertEquals("2.0", bcSecondLevelCacheStats.get("second-level-cache.element.count.in.memory").getValue());
        assertEquals("2.0", bcSecondLevelCacheStats.get("second-level-cache.put.count").getValue());
        assertEquals("1.0", bcSecondLevelCacheStats.get("second-level-cache.miss.count").getValue());
        assertEquals("0.0", bcSecondLevelCacheStats.get("second-level-cache.hit.count").getValue());

        // Create another session SESSION_2
        em_2 = getEmFactory().createEntityManager();
        Session session_2 = (Session) em_2.getDelegate();
        SessionFactory sessionFactory_2 = session_2.getSessionFactory();

        // No BuildConfiguration were inserted in SESSION_2, 1st level cache of SESSION_2 should be empty
        assertEquals(0, session_2.getStatistics().getEntityCount());

        SortedMap<String, Map<String, HibernateMetric>> entitiesStats_2 = getSecondLevelCacheEntitiesStats(
                sessionFactory_1.getStatistics());
        SortedMap<String, Map<String, HibernateMetric>> secondLevelCacheStats_2 = getSecondLevelCacheRegionsStats(
                sessionFactory_1.getStatistics());
        Map<String, HibernateMetric> bcFirstLevelCacheStats_2 = entitiesStats_2
                .get(ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");
        Map<String, HibernateMetric> bcSecondLevelCacheStats_2 = secondLevelCacheStats_2
                .get(REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");

        // 2nd level cache is unique, so entitiesStats should be identical to entitiesStats_2, as secondLevelCacheStats
        // should
        // be identical to secondLevelCacheStats_2
        assertEquals(bcFirstLevelCacheStats.get("insert.count"), bcFirstLevelCacheStats_2.get("insert.count"));
        assertEquals(bcFirstLevelCacheStats.get("cache.put.count"), bcFirstLevelCacheStats_2.get("cache.put.count"));
        assertEquals(bcFirstLevelCacheStats.get("cache.hit.count"), bcFirstLevelCacheStats_2.get("cache.hit.count"));
        assertEquals(bcFirstLevelCacheStats.get("cache.miss.count"), bcFirstLevelCacheStats_2.get("cache.miss.count"));

        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.element.count.in.memory"),
                bcSecondLevelCacheStats_2.get("second-level-cache.element.count.in.memory"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.put.count"),
                bcSecondLevelCacheStats_2.get("second-level-cache.put.count"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.miss.count"),
                bcSecondLevelCacheStats_2.get("second-level-cache.miss.count"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.hit.count"),
                bcSecondLevelCacheStats_2.get("second-level-cache.hit.count"));

        // Searching an existing BuildConfiguration not in 1st level cache of SESSION_2 --> should be a hit in 2nd level
        // cache
        session_2.find(BuildConfiguration.class, buildConfig1.getId());

        //
        printSessionStats(session_2.getStatistics());
        printSessionFactoryStats(sessionFactory_2.getStatistics(), "org.jboss.pnc.model.BuildConfiguration");

        // REFRESH STATS
        entitiesStats = getSecondLevelCacheEntitiesStats(sessionFactory_1.getStatistics());
        secondLevelCacheStats = getSecondLevelCacheRegionsStats(sessionFactory_1.getStatistics());
        entitiesStats_2 = getSecondLevelCacheEntitiesStats(sessionFactory_2.getStatistics());
        secondLevelCacheStats_2 = getSecondLevelCacheRegionsStats(sessionFactory_2.getStatistics());
        bcFirstLevelCacheStats = entitiesStats.get(ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");
        bcSecondLevelCacheStats = secondLevelCacheStats
                .get(REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");
        bcFirstLevelCacheStats_2 = entitiesStats_2.get(ENTITY_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");
        bcSecondLevelCacheStats_2 = secondLevelCacheStats_2
                .get(REGION_STATS_PREFIX + "org.jboss.pnc.model.BuildConfiguration");

        // 2nd level cache should now contain a HIT (BuildConfiguration#100 was not found in SESSION_2 and so was taken
        // from 2nd
        // level cache)
        assertEquals("1.0", bcFirstLevelCacheStats.get("cache.hit.count").getValue());
        assertEquals("1.0", bcSecondLevelCacheStats.get("second-level-cache.hit.count").getValue());

        // All other stats should be identical to previous ones
        assertEquals("2.0", bcFirstLevelCacheStats.get("insert.count").getValue());
        assertEquals("2.0", bcFirstLevelCacheStats.get("cache.put.count").getValue());
        assertEquals("1.0", bcFirstLevelCacheStats.get("cache.miss.count").getValue());

        assertEquals("2.0", bcSecondLevelCacheStats.get("second-level-cache.element.count.in.memory").getValue());
        assertEquals("2.0", bcSecondLevelCacheStats.get("second-level-cache.put.count").getValue());
        assertEquals("1.0", bcSecondLevelCacheStats.get("second-level-cache.miss.count").getValue());

        // 2nd level cache is unique, so again entitiesStatMap_1 should be identical to entitiesStatMap_2
        assertEquals(bcFirstLevelCacheStats.get("insert.count"), bcFirstLevelCacheStats_2.get("insert.count"));
        assertEquals(bcFirstLevelCacheStats.get("cache.put.count"), bcFirstLevelCacheStats_2.get("cache.put.count"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.element.count.in.memory"),
                bcSecondLevelCacheStats_2.get("second-level-cache.element.count.in.memory"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.put.count"),
                bcSecondLevelCacheStats_2.get("second-level-cache.put.count"));
        assertEquals(bcFirstLevelCacheStats.get("cache.hit.count"), bcFirstLevelCacheStats_2.get("cache.hit.count"));
        assertEquals(bcFirstLevelCacheStats.get("cache.miss.count"), bcFirstLevelCacheStats_2.get("cache.miss.count"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.miss.count"),
                bcSecondLevelCacheStats_2.get("second-level-cache.miss.count"));
        assertEquals(
                bcSecondLevelCacheStats.get("second-level-cache.hit.count"),
                bcSecondLevelCacheStats_2.get("second-level-cache.hit.count"));
    }

    @Test
    public void testFirstLevelCacheEviction() {

        // Session 3
        em_3 = getEmFactory().createEntityManager();
        Session session_3 = (Session) em_3.getDelegate();
        SessionFactory sessionFactory_3 = session_3.getSessionFactory();

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers
        // Auditing
        insertExampleBuildConfigurations(em_1, basicRepositoryConfiguration);

        BuildConfiguration buildConfig3 = BuildConfiguration.Builder.newBuilder()
                .id(3)
                .name("Test Build Configuration 3")
                .description("Test Build Configuration 3 Description")
                .project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build())
                .build();

        // Persist in Session 1
        em_1.getTransaction().begin();
        em_1.persist(buildConfig3);
        em_1.getTransaction().commit();

        Integer newBCId = buildConfig3.getId();

        // Entity is fetched very first time
        BuildConfiguration bc = (BuildConfiguration) session_3.load(BuildConfiguration.class, newBCId);

        SortedMap<String, HibernateMetric> genericStats = getGenericStats(sessionFactory_3.getStatistics());
        double entityFetchCount1 = Double
                .parseDouble(genericStats.get("hibernate-orm.entities.fetch.count").getValue());
        double secondLevelCacheHitCount1 = Double
                .parseDouble(genericStats.get("hibernate-orm.second-level-cache.hit.count").getValue());

        // fetch the BuildConfiguration entity again, no change in fetch count from 1st level cache nor access to 2nd
        // level
        // cache as there is no need for it
        bc = (BuildConfiguration) session_3.load(BuildConfiguration.class, newBCId);

        SortedMap<String, HibernateMetric> genericStats_2 = getGenericStats(sessionFactory_3.getStatistics());
        double entityFetchCount2 = Double
                .parseDouble(genericStats_2.get("hibernate-orm.entities.fetch.count").getValue());
        double secondLevelCacheHitCount2 = Double
                .parseDouble(genericStats_2.get("hibernate-orm.second-level-cache.hit.count").getValue());

        // No change in fetch from 1st and 2nd level caches
        assertEquals((int) entityFetchCount1, (int) entityFetchCount2);
        assertEquals((int) secondLevelCacheHitCount2, (int) secondLevelCacheHitCount2);

        // Evict from first level cache
        session_3.evict(bc);

        // fetch one more time
        bc = (BuildConfiguration) session_3.load(BuildConfiguration.class, newBCId);

        SortedMap<String, HibernateMetric> genericStats_3 = getGenericStats(sessionFactory_3.getStatistics());
        double entityFetchCount3 = Double
                .parseDouble(genericStats_3.get("hibernate-orm.entities.fetch.count").getValue());
        double secondLevelCacheHitCount3 = Double
                .parseDouble(genericStats_3.get("hibernate-orm.second-level-cache.hit.count").getValue());

        // No change in fetch from 1st level cache as entity is not there anymore
        assertEquals((int) entityFetchCount2, (int) entityFetchCount3);
        // Change in fetch from 2nd level cache: the entity is not in 1st level cache anymore, so Hibernate gets it from
        // 2nd
        // level
        assertNotEquals(secondLevelCacheHitCount2, secondLevelCacheHitCount3);

        logger.debug(
                "Entity fetch count #1: {}, #2: {}, #3: {}",
                entityFetchCount1,
                entityFetchCount2,
                entityFetchCount3);
        logger.debug(
                "Second level cache hit count #1: {}, #2: {}, #3: {}",
                secondLevelCacheHitCount1,
                secondLevelCacheHitCount2,
                secondLevelCacheHitCount3);
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
        SortedMap<String, Map<String, HibernateMetric>> entitiesStatMap = getSecondLevelCacheEntitiesStats(statistics);
        SortedMap<String, Map<String, HibernateMetric>> secondLevelCacheStatMap = getSecondLevelCacheRegionsStats(
                statistics);
        SortedMap<String, Map<String, HibernateMetric>> collectionStatMap = getSecondLevelCacheCollectionsStats(
                statistics);

        logger.debug("All second level cache stats: {}", secondLevelCacheStatMap);
        logger.debug("All entities stats: {}", entitiesStatMap);
        logger.debug("All collection stats: {}", collectionStatMap);
    }

    private void printSessionFactoryStats(Statistics statistics, String regionName) {
        SortedMap<String, Map<String, HibernateMetric>> entitiesStatMap = getSecondLevelCacheEntitiesStats(statistics);
        SortedMap<String, Map<String, HibernateMetric>> secondLevelCacheStatMap = getSecondLevelCacheRegionsStats(
                statistics);
        SortedMap<String, Map<String, HibernateMetric>> collectionStatMap = getSecondLevelCacheCollectionsStats(
                statistics);

        logger.debug("--- SECOND LEVEL STATS ---");
        logger.debug("Entities stats of {}: {}", regionName, entitiesStatMap.get(ENTITY_STATS_PREFIX + regionName));
        logger.debug(
                "Collection stats of {} : {}",
                regionName,
                collectionStatMap.get(COLLECTION_STATS_PREFIX + regionName));
        logger.debug(
                "Second level cache stats of {}: {}",
                regionName,
                secondLevelCacheStatMap.get(REGION_STATS_PREFIX + regionName));
        logger.debug("--- --- --- --- --- ---");
    }

}
