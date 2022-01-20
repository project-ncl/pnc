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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jakub Bartecek
 */
public class ProductMilestoneTest extends AbstractModelTest {

    private EntityManager em;

    private ProductVersion productVersion = null;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();
        this.em = getEmFactory().createEntityManager();
        initDatabaseUsingDataset(em, BasicModelTest.DBUNIT_DATASET_FILE);

        if (productVersion == null) {
            this.productVersion = em.find(ProductVersion.class, 1);
        }
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
        em.close();
    }

    @Test
    public void shouldCreateProductMilestoneWithStandardVersionFormat() {
        // given
        ProductMilestone productMilestone = ProductMilestone.Builder.newBuilder()
                .version("1.0.1.CR1")
                .productVersion(productVersion)
                .build();

        // when
        em.getTransaction().begin();
        em.persist(productMilestone);
        em.getTransaction().commit();

        // then
        assertNotNull(productMilestone.getId());
        assertTrue(productMilestone.getId() != 0);
    }

    @Test
    public void shouldCreateProductMilestoneWithCDVersionFormat() {
        // given
        ProductMilestone productMilestone = ProductMilestone.Builder.newBuilder()
                .version("1.0.0.CD1")
                .productVersion(productVersion)
                .build();

        // when
        em.getTransaction().begin();
        em.persist(productMilestone);
        em.getTransaction().commit();

        // then
        assertNotNull(productMilestone.getId());
        assertTrue(productMilestone.getId() != 0);
    }

    @Test
    public void shouldNotCreateProductMilestoneWithMalformedVersion() {
        // given
        ProductMilestone productMilestone = ProductMilestone.Builder.newBuilder()
                .version("1.0.0-CD1")
                .productVersion(productVersion)
                .build();

        // when-then
        try {
            em.getTransaction().begin();
            em.persist(productMilestone);
            em.getTransaction().commit();
        } catch (RollbackException ex) {
            if (!(ex.getCause() instanceof ConstraintViolationException))
                fail("Creation of ProductMilestones with malformed version should not be allowed");
        }
    }
}
