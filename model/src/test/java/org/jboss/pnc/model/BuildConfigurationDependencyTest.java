package org.jboss.pnc.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.RollbackException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class BuildConfigurationDependencyTest {

    private static EntityManagerFactory emFactory;

    @BeforeClass
    public static void initEntityManagerFactory() {
        emFactory = Persistence.createEntityManagerFactory("newcastle-test");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        emFactory.close();
    }

    /**
     * Clean up all the tables after each test run
     */
    @After
    public void cleanupDatabaseTables() {

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("SET DATABASE REFERENTIAL INTEGRITY FALSE").executeUpdate();
            em.createNativeQuery("delete from ProductRelease").executeUpdate();
            em.createNativeQuery("delete from ProductMilestone").executeUpdate();
            em.createNativeQuery("delete from ProductVersion").executeUpdate();
            em.createNativeQuery("delete from Product").executeUpdate();
            em.createNativeQuery("delete from BuildRecordSet").executeUpdate();
            em.createNativeQuery("delete from BuildConfiguration_aud").executeUpdate();
            em.createNativeQuery("delete from BuildConfiguration").executeUpdate();
            em.createNativeQuery("delete from Project").executeUpdate();
            em.createNativeQuery("delete from Environment").executeUpdate();
            em.createNativeQuery("delete from License").executeUpdate();
            em.createNativeQuery("SET DATABASE REFERENTIAL INTEGRITY TRUE").executeUpdate();
            tx.commit();

        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test
    public void testBuildConfigurationDependencies() throws Exception {

        // Set up sample build configurations, the id needs to be set manually
        // because the configs are not stored to the database.
        BuildConfiguration buildConfig1 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(1).build();
        BuildConfiguration buildConfig2 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(2).build();
        BuildConfiguration buildConfig3 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(3).build();
        BuildConfiguration buildConfig4 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(4).build();
        BuildConfiguration buildConfig5 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(5).build();
        BuildConfiguration buildConfig6 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(6).build();
        BuildConfiguration buildConfig7 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(7).build();
        BuildConfiguration buildConfig8 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(8).build();

        // Set up the dependency relationships
        buildConfig1.addDependency(buildConfig2);
        buildConfig1.addDependency(buildConfig3);
        buildConfig2.addDependency(buildConfig4);
        buildConfig2.addDependency(buildConfig5);

        // Verify that at this point buildConfig1 has 2 indirect dependencies, and 4 total dependencies
        Assert.assertEquals(2, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(4, buildConfig1.getAllDependencies().size());

        // Add two more indirect dependencies onto config 4
        buildConfig4.addDependency(buildConfig6);
        buildConfig4.addDependency(buildConfig7);
        buildConfig3.addDependency(buildConfig8);
        Assert.assertEquals(5, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(7, buildConfig1.getAllDependencies().size());

        // Add an indirect dependency which is also a direct dependency
        buildConfig7.addDependency(buildConfig3);
        Assert.assertEquals(6, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(7, buildConfig1.getAllDependencies().size());
    }

    @Test
    public void testBuildConfigurationDependencyChecks() throws Exception {

        // Set up sample build configurations, the id needs to be set manually
        // because the configs are not stored to the database.
        BuildConfiguration buildConfig1 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(1).build();
        BuildConfiguration buildConfig2 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(2).build();
        BuildConfiguration buildConfig3 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(3).build();
        BuildConfiguration buildConfig4 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(4).build();
        BuildConfiguration buildConfig5 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(5).build();
        BuildConfiguration buildConfig6 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(6).build();
        BuildConfiguration buildConfig7 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(7).build();
        BuildConfiguration buildConfig8 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .id(8).build();

        // Set up the dependency relationships
        buildConfig1.addDependency(buildConfig2);
        buildConfig1.addDependency(buildConfig3);
        buildConfig2.addDependency(buildConfig4);
        buildConfig2.addDependency(buildConfig5);

        // Verify that at this point buildConfig1 has 2 indirect dependencies, and 4 total dependencies
        Assert.assertEquals(2, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(4, buildConfig1.getAllDependencies().size());

        // Add two more indirect dependencies onto config 4
        buildConfig4.addDependency(buildConfig6);
        buildConfig4.addDependency(buildConfig7);
        buildConfig3.addDependency(buildConfig8);
        Assert.assertEquals(5, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(7, buildConfig1.getAllDependencies().size());

        // Add an indirect dependency which is also a direct dependency
        buildConfig7.addDependency(buildConfig3);
        Assert.assertEquals(6, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(7, buildConfig1.getAllDependencies().size());
    }

    @Test(expected=PersistenceException.class)
    public void testBuildConfigurationAudit() throws Exception {

        License licenseApache20 = ModelTestDataFactory.getInstance().getLicenseApache20();
        Project project1 = ModelTestDataFactory.getInstance().getProject1();
        project1.setLicense(licenseApache20);
        Environment environmentDefault = ModelTestDataFactory.getInstance().getEnvironmentDefault();

        // Set up sample build configurations, the id needs to be set manually
        // because the configs are not stored to the database.
        BuildConfiguration buildConfig1 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig2 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig3 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig4 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig5 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig6 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig7 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();
        BuildConfiguration buildConfig8 = ModelTestDataFactory.getInstance().getGenericBuildConfigurationBuilderGeneric()
                .project(project1).environment(environmentDefault).build();

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction insertConfigTx = em.getTransaction();

        try {
            insertConfigTx.begin();
            em.persist(licenseApache20);
            em.persist(environmentDefault);
            em.persist(project1);
            em.persist(buildConfig1);
            em.persist(buildConfig2);
            em.persist(buildConfig3);
            em.persist(buildConfig4);
            em.persist(buildConfig5);
            em.persist(buildConfig6);
            em.persist(buildConfig7);
            em.persist(buildConfig8);
            insertConfigTx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from BuildConfiguration bc ");
            Long count = (Long) rowCountQuery.getSingleResult();
            // Should have 8 build configurations
            Assert.assertEquals(8, count.longValue());
            
        } catch (RuntimeException e) {
            if (insertConfigTx != null && insertConfigTx.isActive()) {
                insertConfigTx.rollback();
            }
            throw e;
        }

        // Set up the dependency relationships
        buildConfig1.addDependency(buildConfig2);
        buildConfig1.addDependency(buildConfig3);
        buildConfig2.addDependency(buildConfig4);
        buildConfig2.addDependency(buildConfig5);
        buildConfig4.addDependency(buildConfig6);
        buildConfig4.addDependency(buildConfig7);
        buildConfig3.addDependency(buildConfig8);
        buildConfig7.addDependency(buildConfig3);

        // Next check that the dependency relationships can be stored to the db
        // and that the dependency calculations are correct
        EntityTransaction updateDependenciesTx = em.getTransaction();
        try {
            updateDependenciesTx.begin();
            em.persist(buildConfig1);
            em.persist(buildConfig2);
            em.persist(buildConfig3);
            em.persist(buildConfig4);
            em.persist(buildConfig5);
            em.persist(buildConfig6);
            em.persist(buildConfig7);
            em.persist(buildConfig8);
            updateDependenciesTx.commit();

            buildConfig1 = em.find(BuildConfiguration.class, buildConfig1.getId());
            Assert.assertEquals(6, buildConfig1.getIndirectDependencies().size());
            Assert.assertEquals(7, buildConfig1.getAllDependencies().size());
            
        } catch (RuntimeException e) {
            if (updateDependenciesTx != null && updateDependenciesTx.isActive()) {
                updateDependenciesTx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }

        // Next, attempt to add a circular dependency this should throw a PersistenceException
        buildConfig8.addDependency(buildConfig1);
    }

}
