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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.AfterClass;
import org.junit.BeforeClass;

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
            em.createNativeQuery("delete from BuildEnvironment").executeUpdate();
            em.createNativeQuery("delete from BuildRecord").executeUpdate();
            em.createNativeQuery("delete from License").executeUpdate();
            em.createNativeQuery("delete from Product").executeUpdate();
            em.createNativeQuery("delete from ProductMilestone").executeUpdate();
            em.createNativeQuery("delete from ProductRelease").executeUpdate();
            em.createNativeQuery("delete from ProductVersion").executeUpdate();
            em.createNativeQuery("delete from Project").executeUpdate();
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
}
