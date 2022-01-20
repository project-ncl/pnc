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

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.hsqldb.HsqldbDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.internal.SessionImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.io.InputStream;

public abstract class AbstractModelTest {

    private static EntityManagerFactory emFactory;

    /**
     * Load the entity manager factory using the settings in persistence.xml
     */
    @BeforeClass
    public static void initEntityManagerFactory() {
        emFactory = Persistence.createEntityManagerFactory("newcastle-test");
    }

    @AfterClass
    public static void closeEntityManagerFactory() {
        emFactory.close();
    }

    /**
     * Get the entity manager factory pointing to the test db
     */
    protected static EntityManagerFactory getEmFactory() {
        return emFactory;
    }

    /**
     * Delete data from all database tables
     */
    protected static void clearDatabaseTables() {

        EntityManager em = getEmFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createNativeQuery("SET DATABASE REFERENTIAL INTEGRITY FALSE").executeUpdate();
            em.createNativeQuery("delete from Artifact").executeUpdate();
            em.createNativeQuery("delete from BuildConfiguration").executeUpdate();
            em.createNativeQuery("delete from BuildConfiguration_aud").executeUpdate();
            em.createNativeQuery("delete from RepositoryConfiguration").executeUpdate();
            em.createNativeQuery("delete from BuildEnvironment").executeUpdate();
            em.createNativeQuery("delete from BuildRecord").executeUpdate();
            em.createNativeQuery("delete from Product").executeUpdate();
            em.createNativeQuery("delete from ProductMilestone").executeUpdate();
            em.createNativeQuery("delete from ProductRelease").executeUpdate();
            em.createNativeQuery("delete from ProductVersion").executeUpdate();
            em.createNativeQuery("delete from Project").executeUpdate();
            em.createNativeQuery("delete from UserTable").executeUpdate();
            em.createNativeQuery("delete from TargetRepository").executeUpdate();
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

    /**
     * Inserts data into database from the dbunit XML file
     * 
     * @param em Entity manager
     * @param datasetPath Path to DBunit dataset file
     * @throws Exception Thrown in case of any error during the operation
     */
    protected void initDatabaseUsingDataset(EntityManager em, String datasetPath) throws Exception {
        IDatabaseConnection connection = new DatabaseConnection(em.unwrap(SessionImpl.class).connection());
        connection.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new HsqldbDataTypeFactory());
        FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
        flatXmlDataSetBuilder.setColumnSensing(true);
        InputStream dataSetStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(datasetPath);
        IDataSet dataSet = flatXmlDataSetBuilder.build(dataSetStream);
        DatabaseOperation.INSERT.execute(connection, dataSet);
    }

    /**
     * Inserts example BuildConfigurations to the database
     *
     * @param em Entity manager
     * @param repositoryConfiguration RepositoryConfiguration object, which was already persisted to the database
     */
    protected void insertExampleBuildConfigurations(EntityManager em, RepositoryConfiguration repositoryConfiguration) {
        BuildConfiguration buildConfig1 = BuildConfiguration.Builder.newBuilder()
                .id(1)
                .name("Test Build Configuration 1")
                .description("Test Build Configuration 1 Description")
                .project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(repositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build())
                .build();

        BuildConfiguration buildConfig2 = BuildConfiguration.Builder.newBuilder()
                .id(2)
                .name("Test Build Configuration 2")
                .description("Test Build Configuration 2 Description")
                .project(Project.Builder.newBuilder().id(1).build())
                .repositoryConfiguration(repositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BuildEnvironment.Builder.newBuilder().id(1).build())
                .build();

        em.getTransaction().begin();
        em.persist(buildConfig1);
        em.persist(buildConfig2);
        em.getTransaction().commit();
    }
}
