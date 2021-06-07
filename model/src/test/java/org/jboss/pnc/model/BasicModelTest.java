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

import org.jboss.pnc.constants.Attributes;
import org.jboss.pnc.enums.RepositoryType;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.query.AuditEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.jboss.pnc.constants.ReposiotryIdentifier;

public class BasicModelTest extends AbstractModelTest {

    /** located in src/test/resources */
    final static String DBUNIT_DATASET_FILE = "basic-model-test-data.xml";

    private User pncUser;

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
            .id(1)
            .build();

    /**
     * Initialize a basic data set before each test run
     */
    @Before
    public void initTestData() throws Exception {
        // Initialize data from xml dataset file
        EntityManager em = getEmFactory().createEntityManager();
        initDatabaseUsingDataset(em, DBUNIT_DATASET_FILE);

        // Initialize sample build configurations, these cannot be done by DBUnit because of the Hibernate Envers
        // Auditing
        insertExampleBuildConfigurations(em, basicRepositoryConfiguration);

        em.close();
        this.pncUser = User.Builder.newBuilder().id(1).build();
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
    }

    @Test
    public void testDataInitializationIsWorking() {
        EntityManager em = getEmFactory().createEntityManager();
        Assert.assertEquals(2, em.createQuery("from Product").getResultList().size());
        Assert.assertEquals(2, em.createQuery("from ProductVersion").getResultList().size());
        Assert.assertEquals(2, em.createQuery("from ProductMilestone").getResultList().size());
        Assert.assertEquals(2, em.createQuery("from BuildConfiguration").getResultList().size());
        Assert.assertEquals(2, em.createQuery("from RepositoryConfiguration").getResultList().size());
    }

    @Test
    public void testSimpleProductInsertAndUpdate() {

        final String NEW_PRODUCT_INSERTED_NAME = "New Product Inserted";
        final String NEW_PRODUCT_UPDATED_NAME = "New Product Updated";

        EntityManager em = getEmFactory().createEntityManager();
        em.getTransaction().begin();
        Product newProduct = Product.Builder.newBuilder()
                .name(NEW_PRODUCT_INSERTED_NAME)
                .description("Product")
                .abbreviation("foo")
                .build();
        em.persist(newProduct);
        em.getTransaction().commit();
        int productId = newProduct.getId();

        em.getTransaction().begin();
        Product productLoad = em.find(Product.class, productId);
        Assert.assertEquals(NEW_PRODUCT_INSERTED_NAME, productLoad.getName());

        productLoad.setName(NEW_PRODUCT_UPDATED_NAME);
        em.persist(productLoad);
        em.getTransaction().commit();

        Product productReload = em.find(Product.class, productId);
        Assert.assertEquals(NEW_PRODUCT_UPDATED_NAME, productReload.getName());
    }

    @Test
    public void testCreateBuildRecordAndArtifacts() {
        EntityManager em = getEmFactory().createEntityManager();

        TargetRepository targetRepository = getTargetRepository("builds-untested");

        Artifact artifact1 = Artifact.Builder.newBuilder()
                .identifier("org.jboss:artifact1")
                .md5("md-fake-ABCD1234")
                .sha1("sha1-fake-ABCD1234")
                .sha256("sha256-fake-ABCD1234")
                .filename("artifact1.jar")
                .targetRepository(targetRepository)
                .build();
        Artifact artifact2 = Artifact.Builder.newBuilder()
                .identifier("org.jboss:artifact2")
                .md5("md-fake-BBCD1234")
                .sha1("sha1-fake-BBCD1234")
                .sha256("sha256-fake-BBCD1234")
                .filename("artifact2.jar")
                .originUrl("http://central/artifact2.jar")
                .importDate(Date.from(Instant.now()))
                .targetRepository(targetRepository)
                .build();
        Artifact artifact3 = Artifact.Builder.newBuilder()
                .identifier("org.jboss:artifact3")
                .md5("md-fake-CBCD1234")
                .sha1("sha1-fake-CBCD1234")
                .sha256("sha256-fake-CBCD1234")
                .filename("artifact3.jar")
                .originUrl("http://central/artifact3.jar")
                .importDate(Date.from(Instant.now()))
                .targetRepository(targetRepository)
                .build();

        BuildConfigurationAudited buildConfigAud = findBuildConfigurationAudited(em);

        BuildRecord buildRecord1 = BuildRecord.Builder.newBuilder()
                .id("1")
                .buildConfigurationAudited(buildConfigAud)
                .buildLog("Build Completed.")
                .buildContentId("foo")
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                .dependency(artifact3)
                .user(pncUser)
                .temporaryBuild(false)
                .build();

        em.getTransaction().begin();
        em.persist(targetRepository);
        em.persist(artifact1);
        em.persist(artifact2);
        em.persist(artifact3);
        em.persist(buildRecord1);
        em.getTransaction().commit();
    }

    private TargetRepository getTargetRepository(String path) {
        return TargetRepository.newBuilder()
                .temporaryRepo(false)
                .identifier(ReposiotryIdentifier.INDY_MAVEN)
                .repositoryPath(path)
                .repositoryType(RepositoryType.MAVEN)
                .build();
    }

    private BuildConfigurationAudited findBuildConfigurationAudited(EntityManager em) {
        List<Object[]> result = AuditReaderFactory.get(em)
                .createQuery()
                .forRevisionsOfEntity(BuildConfiguration.class, false, false)
                .addOrder(AuditEntity.revisionNumber().desc())
                .getResultList();

        Object[] second = result.get(1);
        BuildConfiguration buildConfiguration = (BuildConfiguration) second[0];
        return BuildConfigurationAudited
                .fromBuildConfiguration(buildConfiguration, ((DefaultRevisionEntity) second[1]).getId());
    }

    @Test
    public void testBuildRecordPreventsAddingDuplicateArtifacts() {

        EntityManager em = getEmFactory().createEntityManager();

        TargetRepository targetRepository = getTargetRepository("builds-untested2");

        Artifact builtArtifact = Artifact.Builder.newBuilder()
                .identifier("org.jboss:builtArtifact")
                .md5("md-fake-12345678")
                .sha1("sha1-fake-12345678")
                .sha256("sha256-fake-12345678")
                .filename("buildArtifact.jar")
                .targetRepository(targetRepository)
                .build();
        Artifact importedArtifact = Artifact.Builder.newBuilder()
                .identifier("org.jboss:importedArtifact")
                .md5("md-fake-12345678")
                .sha1("sha1-fake-12345678")
                .sha256("sha256-fake-12345678")
                .filename("importedArtifact.jar")
                .originUrl("http://central/importedArtifact.jar")
                .importDate(Date.from(Instant.now()))
                .targetRepository(targetRepository)
                .build();

        BuildConfigurationAudited buildConfigAud = findBuildConfigurationAudited(em);

        BuildRecord buildRecord = BuildRecord.Builder.newBuilder()
                .id("CERBB5D55GARK")
                .buildConfigurationAudited(buildConfigAud)
                .buildLog("Bulid Complete")
                .buildContentId("foo")
                .submitTime(Date.from(Instant.now()))
                .startTime(Date.from(Instant.now()))
                .endTime(Date.from(Instant.now()))
                // Add the built artifact and dependency artifact twice
                .dependency(importedArtifact)
                .dependency(importedArtifact)
                .user(pncUser)
                .temporaryBuild(false)
                .build();

        em.getTransaction().begin();
        em.persist(targetRepository);
        em.persist(builtArtifact);
        em.persist(importedArtifact);
        em.persist(buildRecord);
        em.getTransaction().commit();

        em.close();
    }

    @Test
    public void testProductMilestoneAndRelease() throws Exception {

        EntityManager em = getEmFactory().createEntityManager();
        ProductMilestone productMilestone1 = em.find(ProductMilestone.class, 1);

        TargetRepository targetRepository = getTargetRepository("builds-untested3");

        Artifact artifact = Artifact.Builder.newBuilder()
                .identifier("org.test:artifact1:1.0:jar")
                .md5("md-fake-987654321")
                .sha1("sha1-fake-987654321")
                .sha256("sha256-fake-987654321")
                .filename("artifact1.jar")
                .originUrl("http://central.maven.org/maven2/test.jar")
                .importDate(Date.from(Instant.now()))
                .targetRepository(targetRepository)
                .build();
        productMilestone1.addDistributedArtifact(artifact);
        ProductRelease productRelease1 = ProductRelease.Builder.newBuilder()
                .version("1.0.0.Beta1")
                .productMilestone(productMilestone1)
                .build();

        productRelease1.setProductMilestone(productMilestone1);

        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            em.persist(targetRepository);
            em.persist(artifact);
            em.persist(productMilestone1);
            em.persist(productRelease1);
            tx.commit();

            ProductRelease release = em.find(ProductRelease.class, productRelease1.getId());
            Assert.assertEquals(1, release.getProductMilestone().getDistributedArtifacts().size());
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
    public void testProductVersionBrewTagGeneration() {
        EntityManager em = getEmFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();

        final String version = "10.1";
        Product product = Product.Builder.newBuilder().id(1).build();

        ProductVersion productVersionOriginal = ProductVersion.Builder.newBuilder()
                .version(version)
                .product(product)
                .generateBrewTagPrefix("TP1", version, "${product_short_name}-${product_version}-pnc")
                .build();

        tx.begin();
        em.persist(productVersionOriginal);
        tx.commit();

        ProductVersion productVersionLoaded = em.find(ProductVersion.class, productVersionOriginal.getId());
        Assert.assertEquals(
                "tp1-" + version + "-pnc",
                productVersionLoaded.getAttributes().get(Attributes.BREW_TAG_PREFIX));

    }
}
