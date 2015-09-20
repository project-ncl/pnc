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

import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicModelTest {

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
            em.createNativeQuery("delete from BuildEnvironment").executeUpdate();
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
    public void testInsertProduct() throws Exception {
        
        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(ModelTestDataFactory.getInstance().getProduct1());
            tx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from Product product");
            Long count = (Long) rowCountQuery.getSingleResult();
            Assert.assertEquals(1, count.longValue());
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
    public void testInsertProductVersions() throws Exception {

        Product product1 = ModelTestDataFactory.getInstance().getProduct1();
        ProductVersion productVersion1 = ModelTestDataFactory.getInstance().getProductVersion1();
        productVersion1.setProduct(product1);
        BuildRecordSet buildRecordSet1 = ModelTestDataFactory.getInstance().getBuildRecordSet();
        ProductMilestone productMilestone1 = ModelTestDataFactory.getInstance().getProductMilestone1version1();
        productMilestone1.setProductVersion(productVersion1);
        productMilestone1.setPerformedBuildRecordSet(buildRecordSet1);
        BuildRecordSet buildRecordSet2 = ModelTestDataFactory.getInstance().getBuildRecordSet();
        ProductRelease productRelease1 = ModelTestDataFactory.getInstance().getProductRelease1();
        productRelease1.setProductMilestone(productMilestone1);

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(product1);
            em.persist(productVersion1);
            em.persist(buildRecordSet1);
            em.persist(productMilestone1);
            em.persist(buildRecordSet2);
            em.persist(productRelease1);
            tx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from ProductRelease product_release");
            Long count = (Long) rowCountQuery.getSingleResult();
            Assert.assertEquals(1, count.longValue());
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
    public void testInsertProjects() throws Exception {

        License licenseApache20 = ModelTestDataFactory.getInstance().getLicenseApache20();
        License licenseGPLv3 = ModelTestDataFactory.getInstance().getLicenseGPLv3();
        Project project1 = ModelTestDataFactory.getInstance().getProject1();
        project1.setLicense(licenseApache20);
        Project project2 = ModelTestDataFactory.getInstance().getProject2();
        project2.setLicense(licenseGPLv3);

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(licenseApache20);
            em.persist(project1);
            em.persist(licenseGPLv3);
            em.persist(project2);
            tx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from Project project");
            Long count = (Long) rowCountQuery.getSingleResult();
            Assert.assertEquals(2, count.longValue());
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
    public void testBuildConfigurationAudit() throws Exception {

        License licenseApache20 = ModelTestDataFactory.getInstance().getLicenseApache20();
        Project project1 = ModelTestDataFactory.getInstance().getProject1();
        project1.setLicense(licenseApache20);
        BuildEnvironment defaultBuildEnvironment = ModelTestDataFactory.getInstance().getBuildEnvironmentDefault();
        BuildConfiguration buildConfiguration1 = ModelTestDataFactory.getInstance().getBuildConfiguration1();
        buildConfiguration1.setProject(project1);
        buildConfiguration1.setBuildEnvironment(defaultBuildEnvironment);

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx1 = em.getTransaction();
        EntityTransaction tx2 = em.getTransaction();

        try {
            tx1.begin();
            em.persist(licenseApache20);
            em.persist(defaultBuildEnvironment);
            em.persist(project1);
            em.persist(buildConfiguration1);
            tx1.commit();

            tx2.begin();
            buildConfiguration1 = em.find(BuildConfiguration.class, buildConfiguration1.getId());
            buildConfiguration1.setDescription("Updated build config description");
            em.merge(buildConfiguration1);;
            tx2.commit();

            Query rowCountQuery = em.createQuery("select count(*) from BuildConfigurationAudited bca where id=" + buildConfiguration1.getId());
            Long count = (Long) rowCountQuery.getSingleResult();
            // Should have 2 audit records, 1 for insert, and 1 for update
            Assert.assertEquals(2, count.longValue());
            
        } catch (RuntimeException e) {
            if (tx1 != null && tx1.isActive()) {
                tx1.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @Test(expected=RollbackException.class)
    public void testProjectInsertConstraintFailure() throws Exception {
        
        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            // Expect this to fail because of missing license foreign key
            em.persist(ModelTestDataFactory.getInstance().getProject1());
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


    /**
     * Test validation of the version string regex
     * 
     * @throws Exception
     */
    @Test
    public void testVersionStringValidation() throws Exception {
        
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Product product = Product.Builder.newBuilder()
                .name("Test Product")
                .build();
        ProductVersion productVersion = ProductVersion.Builder.newBuilder()
                .product(product)
                .version("1.0")
                .build();

        // Test validation of product version
        Set<ConstraintViolation<ProductVersion>> productVersionViolations = validator.validate(productVersion);
        Assert.assertTrue(productVersionViolations.size() == 0);

        productVersion.setVersion("1.0.x");
        productVersionViolations = validator.validate(productVersion);
        Assert.assertTrue(productVersionViolations.size() == 1);

        productVersion.setVersion("foo");
        productVersionViolations = validator.validate(productVersion);
        Assert.assertTrue(productVersionViolations.size() == 1);

        // Test product milestone versions
        ProductMilestone milestone = ProductMilestone.Builder.newBuilder()
                .productVersion(productVersion)
                .version("1.0.0.ER1")
                .build();
        Set<ConstraintViolation<ProductMilestone>> milestoneVersionViolations = validator.validate(milestone);
        Assert.assertTrue(milestoneVersionViolations.size() == 0);

        milestone.setVersion("1.0");
        milestoneVersionViolations = validator.validate(milestone);
        Assert.assertTrue(milestoneVersionViolations.size() == 1);

        milestone.setVersion("1.0.DR1");
        milestoneVersionViolations = validator.validate(milestone);
        Assert.assertTrue(milestoneVersionViolations.size() == 1);

        milestone.setVersion("1.0.x");
        milestoneVersionViolations = validator.validate(milestone);
        Assert.assertTrue(milestoneVersionViolations.size() == 1);

        // Test product release versions
        ProductRelease release = ProductRelease.Builder.newBuilder()
                .productMilestone(milestone)
                .version("1.0.0.GA")
                .build();
        Set<ConstraintViolation<ProductRelease>> releaseVersionViolations = validator.validate(release);
        Assert.assertTrue(releaseVersionViolations.size() == 0);

        release.setVersion("1.0");
        releaseVersionViolations = validator.validate(release);
        Assert.assertTrue(releaseVersionViolations.size() == 1);

        release.setVersion("1.0.DR1");
        releaseVersionViolations = validator.validate(release);
        Assert.assertTrue(releaseVersionViolations.size() == 1);

        release.setVersion("1.0.x");
        releaseVersionViolations = validator.validate(release);
        Assert.assertTrue(releaseVersionViolations.size() == 1);

    }

    @Test
    public void testBeanValidationFailureOnCommit() throws Exception {
                
        Product product1 = ModelTestDataFactory.getInstance().getProduct1();
        ProductVersion productVersion1 = ProductVersion.Builder.newBuilder()
                .product(product1)
                .version("foo") // Invalid version string
                .build();

        EntityManager em = emFactory.createEntityManager();
        EntityTransaction tx1 = em.getTransaction();

        try {
            tx1.begin();
            em.persist(product1);
            em.persist(productVersion1);
            tx1.commit(); // This should throw a Rollback exception caused by the constraint violation

        } catch (RollbackException e) {
            if (tx1 != null && tx1.isActive()) {
                tx1.rollback();
            }
            Assert.assertTrue(e.getCause() instanceof ConstraintViolationException);
        } finally {
            em.close();
        }

    }

}
