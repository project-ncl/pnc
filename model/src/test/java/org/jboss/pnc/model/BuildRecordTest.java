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
import javax.persistence.PersistenceException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * @author Jakub Bartecek
 */
public class BuildRecordTest extends AbstractModelTest {

    protected final RepositoryConfiguration REPOSITORY_CONFIGURATION_ID_1 = RepositoryConfiguration.Builder.newBuilder()
            .id(1)
            .build();

    private EntityManager em;

    private User user = null;

    private BuildEnvironment buildEnvironment = null;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();

        this.em = getEmFactory().createEntityManager();

        initDatabaseUsingDataset(em, BasicModelTest.DBUNIT_DATASET_FILE);
        insertExampleBuildConfigurations(em, REPOSITORY_CONFIGURATION_ID_1);

        if (user == null) {
            this.user = User.Builder.newBuilder().id(1).build();
        }

        if (buildEnvironment == null) {
            this.buildEnvironment = BuildEnvironment.Builder.newBuilder().id(1).build();
        }
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
        em.close();
    }

    @Test
    public void shouldProhibitDeletionOfNonTemporaryBuild() {
        // given
        Base32LongID brId = new Base32LongID("666");
        BuildRecord br = prepareBuildRecordBuilder().id(brId).temporaryBuild(false).build();

        em.getTransaction().begin();
        em.persist(br);
        em.getTransaction().commit();

        // when, then
        try {
            em.getTransaction().begin();
            em.remove(br);
            em.getTransaction().commit();
        } catch (PersistenceException ex) {
            em.getTransaction().rollback();
            BuildRecord obtainedBr = em.find(BuildRecord.class, brId);
            assertNotNull(obtainedBr);
            assertEquals(brId, obtainedBr.getId());
            return;
        }
        fail("Deletion of the standard BuildRecord should be prohibited.");
    }

    @Test
    public void shouldAllowDeletionOfTemporaryBuild() {
        // given
        Base32LongID brId = new Base32LongID("CERBB5D55GARK");
        BuildRecord br = prepareBuildRecordBuilder().id(brId).temporaryBuild(true).build();

        em.getTransaction().begin();
        em.persist(br);
        em.getTransaction().commit();

        // when
        em.getTransaction().begin();
        em.remove(br);
        em.getTransaction().commit();

        // then
        assertNull(em.find(BuildRecord.class, brId));
    }

    private BuildRecord.Builder prepareBuildRecordBuilder() {
        return BuildRecord.Builder.newBuilder()
                .buildConfigurationAuditedId(1)
                .buildConfigurationAuditedRev(1)
                .submitTime(new Date())
                .user(user)
                .buildEnvironment(buildEnvironment);
    }
}
