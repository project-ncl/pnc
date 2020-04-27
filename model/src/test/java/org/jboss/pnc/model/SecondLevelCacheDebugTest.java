/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import static org.jboss.pnc.model.utils.HibernateStatsUtils.getSecondLevelCacheCollectionsStats;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.getSecondLevelCacheEntitiesStats;
import static org.jboss.pnc.model.utils.HibernateStatsUtils.getSecondLevelCacheRegionsStats;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.utils.HibernateMetric;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecondLevelCacheDebugTest extends AbstractModelTest {

    protected Logger logger = LoggerFactory.getLogger(SecondLevelCacheDebugTest.class);

    /** located in src/test/resources */
    final static String DBUNIT_NCL_5686_DATASET_FILE = "cache-model-test-data.xml";

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
            .id(1)
            .build();

    private EntityManager em_1;
    private EntityManager em_2;
    private EntityManager em_3;
    private EntityManager em_4;

    private static int dependencyBCId;
    private static int buildConfigurationId;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();

        // Initialize data from xml dataset file
        em_1 = getEmFactory().createEntityManager();
        em_2 = getEmFactory().createEntityManager();
        em_3 = getEmFactory().createEntityManager();
        em_4 = getEmFactory().createEntityManager();

        initDatabaseUsingDataset(em_1, DBUNIT_NCL_5686_DATASET_FILE);

        Session session = (Session) em_1.getDelegate();
        BuildEnvironment buildEnvironmentBC = session.load(BuildEnvironment.class, 8);
        BuildEnvironment buildEnvironmentDepBC = session.load(BuildEnvironment.class, 100);
        RepositoryConfiguration repositoryConfigurationBC = session.load(RepositoryConfiguration.class, 103);
        RepositoryConfiguration repositoryConfigurationDepBC = session.load(RepositoryConfiguration.class, 100);
        Project projectBC = session.load(Project.class, 103);
        Project projectDepBC = session.load(Project.class, 100);
        ProductVersion productVersionBC = session.load(ProductVersion.class, 100);

        em_1.getTransaction().begin();

        BuildConfiguration dependencyBC = BuildConfiguration.Builder.newBuilder()
                .buildEnvironment(buildEnvironmentDepBC)
                .project(projectDepBC)
                .repositoryConfiguration(repositoryConfigurationDepBC)
                .name("pnc-1.0.0.DR1")
                .description("Test build config for project newcastle")
                .buildScript("mvn clean deploy -DskipTests=true")
                .scmRevision("*/v0.2")
                .buildType(BuildType.MVN)
                .productVersion(productVersionBC)
                .build();

        em_1.persist(dependencyBC);

        BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                .buildEnvironment(buildEnvironmentBC)
                .project(projectBC)
                .repositoryConfiguration(repositoryConfigurationBC)
                .name("dependency-analysis-master")
                .description("Test config for Dependency Analysis.")
                .buildScript("mvn clean deploy -DskipTests=true")
                .scmRevision("master")
                .buildType(BuildType.MVN)
                .dependency(dependencyBC)
                .build();

        em_1.persist(buildConfiguration);
        em_1.getTransaction().commit();

        dependencyBCId = dependencyBC.getId();
        buildConfigurationId = buildConfiguration.getId();

        logger.debug("BuildConfiguration dependencyBC: {}", dependencyBC.getId());
        logger.debug("BuildConfiguration buildConfiguration: {}", buildConfiguration.getId());
    }

    @After
    public void cleanup() {
        clearDatabaseTables();

        em_1.close();
    }

    // Verifies NCL-5686
    @Test
    public void testBuildConfigurationInsertion() throws Exception {

        // Session 1
        Session session_1 = (Session) em_1.getDelegate();
        Session session_2 = (Session) em_2.getDelegate();
        Session session_3 = (Session) em_3.getDelegate();
        SessionFactory sessionFactory = session_1.getSessionFactory();
        sessionFactory.getStatistics().clear();

        BuildConfiguration bc = session_1.load(BuildConfiguration.class, buildConfigurationId);

        assertTrue(
                bc.getDependencies()
                        .stream()
                        .map(dependencyConfig -> dependencyConfig.getId())
                        .collect(Collectors.toSet())
                        .contains(dependencyBCId));

        BuildConfiguration bc2 = session_2.load(BuildConfiguration.class, buildConfigurationId);
        bc2.getDependencies().stream().map(dependencyConfig -> dependencyConfig.getId()).collect(Collectors.toSet());

        BuildConfiguration bc3 = session_3.find(BuildConfiguration.class, buildConfigurationId);
        bc3.getDependencies().stream().map(dependencyConfig -> dependencyConfig.getId()).collect(Collectors.toSet());

        printSessionStats(session_1.getStatistics(), "session_1");
        printSessionStats(session_2.getStatistics(), "session_2");
        printSessionStats(session_3.getStatistics(), "session_3");

        printSessionFactoryStats(sessionFactory.getStatistics());

    }

    @Test
    public void testBogusBuildConfigurationUpdate() {
        Session session_1 = (Session) em_1.getDelegate();
        SessionFactory sessionFactory = session_1.getSessionFactory();
        sessionFactory.getStatistics().clear();

        BuildConfiguration bc = session_1.load(BuildConfiguration.class, buildConfigurationId);
        RepositoryConfiguration rc = session_1
                .load(RepositoryConfiguration.class, bc.getRepositoryConfiguration().getId());
        Project p = session_1.load(Project.class, bc.getProject().getId());
        BuildEnvironment bE = session_1.load(BuildEnvironment.class, bc.getBuildEnvironment().getId());

        BuildConfiguration.Builder bcBuilder = BuildConfiguration.Builder.newBuilder()
                .id(bc.getId())
                .name(bc.getName())
                .description(bc.getDescription())
                .buildScript(bc.getBuildScript())
                .scmRevision(bc.getScmRevision())
                .archived(bc.isArchived())
                .genericParameters(bc.getGenericParameters())
                .buildType(bc.getBuildType());

        rebuildRepositoryConfiguration(rc, bcBuilder);
        rebuildProject(p, bcBuilder);
        rebuildBuildEnvironment(bE, bcBuilder);
        rebuildProductVersion(bc, bcBuilder);
        rebuildDependencies(bc, bcBuilder);

        BuildConfiguration buildConfigDB = session_1.get(BuildConfiguration.class, buildConfigurationId);
        // If updating an existing record, need to replace several fields from the rest entity with values from DB
        if (buildConfigDB != null) {
            bcBuilder.lastModificationTime(buildConfigDB.getLastModificationTime()); // Handled by JPA @Version
            bcBuilder.creationTime(buildConfigDB.getCreationTime()); // Immutable after creation
            if (bc.getDependencies() == null || bc.getDependencies().isEmpty()) {
                // If the client request does not include a list of dependencies, just keep the current set
                bcBuilder.dependencies(buildConfigDB.getDependencies());
            }
        }

        // Modify the buildScript
        bcBuilder.buildScript("mvn install");
        em_4.getTransaction().begin();
        BuildConfiguration rebuildBuildConfiguration = bcBuilder.build();

        em_4.merge(rebuildBuildConfiguration);
        em_4.getTransaction().commit();

        sessionFactory.getStatistics().logSummary();

    }

    /*
     * We don't have REST Entities classes, so I am rebuilding the Entities to mimic what happens in REST endpoints
     */
    private void rebuildRepositoryConfiguration(RepositoryConfiguration rc, BuildConfiguration.Builder bcBuilder) {
        bcBuilder.repositoryConfiguration(
                RepositoryConfiguration.Builder.newBuilder()
                        .id(rc.getId())
                        .internalUrl(rc.getInternalUrl())
                        .externalUrl(rc.getExternalUrl())
                        .preBuildSyncEnabled(Boolean.TRUE.equals(rc.isPreBuildSyncEnabled()))
                        .build());
    }

    private void rebuildProject(Project p, BuildConfiguration.Builder bcBuilder) {
        Project.Builder pBuilder = Project.Builder.newBuilder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .issueTrackerUrl(p.getIssueTrackerUrl())
                .projectUrl(p.getProjectUrl());
        List<Integer> buildConfigurationIds = nullableStreamOf(p.getBuildConfigurations())
                .map(buildConfig -> buildConfig.getId())
                .collect(Collectors.toList());
        nullableStreamOf(buildConfigurationIds).forEach(
                buildConfigurationId -> pBuilder
                        .buildConfiguration(BuildConfiguration.Builder.newBuilder().id(buildConfigurationId).build()));
        bcBuilder.project(pBuilder.build());
    }

    private void rebuildBuildEnvironment(BuildEnvironment bE, BuildConfiguration.Builder bcBuilder) {
        bcBuilder.buildEnvironment(
                BuildEnvironment.Builder.newBuilder()
                        .id(bE.getId())
                        .name(bE.getName())
                        .description(bE.getDescription())
                        .systemImageRepositoryUrl(bE.getSystemImageRepositoryUrl())
                        .systemImageId(bE.getSystemImageId())
                        .attributes(bE.getAttributes())
                        .systemImageType(bE.getSystemImageType())
                        .deprecated(bE.isDeprecated())
                        .build());
    }

    private void rebuildProductVersion(BuildConfiguration bc, BuildConfiguration.Builder bcBuilder) {
        if (bc.getProductVersion() != null) {
            bcBuilder.productVersion(ProductVersion.Builder.newBuilder().id(bc.getProductVersion().getId()).build());
        }
    }

    private void rebuildDependencies(BuildConfiguration bc, BuildConfiguration.Builder bcBuilder) {
        Set<Integer> dependencyIds = nullableStreamOf(bc.getDependencies())
                .map(dependencyConfig -> dependencyConfig.getId())
                .collect(Collectors.toSet());
        nullableStreamOf(dependencyIds).forEach(
                dependencyId -> bcBuilder.dependency(BuildConfiguration.Builder.newBuilder().id(dependencyId).build()));

    }

    @SuppressWarnings("unchecked")
    private void printSessionStats(SessionStatistics sessionStatistics, String sessionName) {
        System.out.println("\n--- FIRST LEVEL CACHE STATS ---");
        System.out.println(sessionName + " stats entityCount: " + sessionStatistics.getEntityCount());
        sessionStatistics.getEntityKeys().stream().forEach(ek -> {
            System.out.println("\t" + ek);
        });
        System.out.println(sessionName + " stats collectionCount: " + sessionStatistics.getCollectionCount());
        sessionStatistics.getCollectionKeys().stream().forEach(ck -> {
            System.out.println("\t" + ck);
        });
        System.out.println("--- --- --- --- --- ---");
    }

    private void printSessionFactoryStats(Statistics statistics) {
        SortedMap<String, Map<String, HibernateMetric>> entitiesStatMap = getSecondLevelCacheEntitiesStats(statistics);
        SortedMap<String, Map<String, HibernateMetric>> secondLevelCacheStatMap = getSecondLevelCacheRegionsStats(
                statistics);
        SortedMap<String, Map<String, HibernateMetric>> collectionStatMap = getSecondLevelCacheCollectionsStats(
                statistics);

        System.out.println("\n--- SECOND LEVEL CACHE STATS ---");
        secondLevelCacheStatMap.keySet().stream().forEach(emk -> {
            StringBuilder sb = new StringBuilder();
            Map<String, HibernateMetric> hMetrics = secondLevelCacheStatMap.get(emk);
            displayIfNotZero(hMetrics, "second-level-cache.element.count.in.memory", sb);
            displayIfNotZero(hMetrics, "second-level-cache.hit.count", sb);
            displayIfNotZero(hMetrics, "second-level-cache.miss.count", sb);
            displayIfNotZero(hMetrics, "second-level-cache.put.count", sb);

            if (sb.length() > 0) {
                sb.insert(0, "\n" + emk.replace("hibernate-orm.", ""));
                System.out.println(sb.toString());
            }
        });

        System.out.println("\n--- SECOND LEVEL ENTITY STATS ---");
        entitiesStatMap.keySet().stream().forEach(emk -> {
            StringBuilder sb = new StringBuilder();
            Map<String, HibernateMetric> hMetrics = entitiesStatMap.get(emk);
            displayIfNotZero(hMetrics, "cache.hit.count", sb);
            displayIfNotZero(hMetrics, "cache.miss.count", sb);
            displayIfNotZero(hMetrics, "cache.put.count", sb);
            displayIfNotZero(hMetrics, "delete.count", sb);
            displayIfNotZero(hMetrics, "fetch.count", sb);
            displayIfNotZero(hMetrics, "insert.count", sb);
            displayIfNotZero(hMetrics, "load.count", sb);
            displayIfNotZero(hMetrics, "update.count", sb);

            if (sb.length() > 0) {
                sb.insert(0, "\n" + emk.replace("hibernate-orm.", ""));
                System.out.println(sb.toString());
            }
        });

        System.out.println("\n--- SECOND LEVEL COLLECTION STATS ---");
        collectionStatMap.keySet().stream().forEach(emk -> {
            StringBuilder sb = new StringBuilder();
            Map<String, HibernateMetric> hMetrics = collectionStatMap.get(emk);
            displayIfNotZero(hMetrics, "cache.hit.count", sb);
            displayIfNotZero(hMetrics, "cache.miss.count", sb);
            displayIfNotZero(hMetrics, "cache.put.count", sb);
            displayIfNotZero(hMetrics, "remove.count", sb);
            displayIfNotZero(hMetrics, "fetch.count", sb);
            displayIfNotZero(hMetrics, "load.count", sb);
            displayIfNotZero(hMetrics, "recreate.count", sb);
            displayIfNotZero(hMetrics, "update.count", sb);

            if (sb.length() > 0) {
                sb.insert(0, "\n" + emk.replace("hibernate-orm.", ""));
                System.out.println(sb.toString());
            }
        });
    }

    private void displayIfNotZero(Map<String, HibernateMetric> hMetrics, String propertyName, StringBuilder sb) {
        double value = Double.valueOf(hMetrics.get(propertyName).getValue());
        if (value > 0) {
            sb.append("\n   " + propertyName + ": " + value);
        }
    }

    private static <T> Stream<T> nullableStreamOf(Collection<T> nullableCollection) {
        if (nullableCollection == null) {
            return Stream.empty();
        }
        return nullableCollection.stream();
    }

}
