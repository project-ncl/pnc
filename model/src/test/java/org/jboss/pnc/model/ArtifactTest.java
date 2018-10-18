/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.enums.ArtifactQuality;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jakub Bartecek
 */
public class ArtifactTest extends AbstractModelTest {
    private EntityManager em;

    private TargetRepository targetRepository = null;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();

        this.em = getEmFactory().createEntityManager();
        initDatabaseUsingDataset(em, BasicModelTest.DBUNIT_DATASET_FILE);
        insertBasicTargetRepository();
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
        em.close();
    }

    @Test
    public void shouldProhibitDeletionOfNonTemporaryArtifact() {
        // given
        Artifact artifact = prepareArtifactBuilder()
                .artifactQuality(ArtifactQuality.NEW)
                .build();

        em.getTransaction().begin();
        em.persist(artifact);
        em.getTransaction().commit();
        int artifactId = artifact.getId();

        // when, then
        try {
            em.getTransaction().begin();
            em.remove(artifact);
            em.getTransaction().commit();
        } catch (PersistenceException ex) {
            Artifact obtainedArtifact = em.find(Artifact.class, artifactId);
            assertNotNull(obtainedArtifact);
            assertEquals(artifactId, obtainedArtifact.getId().intValue());
            return;
        }
        fail("Deletion of the non Temporary artifact should be prohibited.");
    }

    @Test
    public void shouldAllowDeletionOfTemporaryArtifact() {
        // given
        Artifact artifact = prepareArtifactBuilder()
                .artifactQuality(ArtifactQuality.TEMPORARY)
                .build();

        em.getTransaction().begin();
        em.persist(artifact);
        em.getTransaction().commit();
        int artifactId = artifact.getId();
        assertTrue(artifact.getId() != null);
        assertTrue(artifact.getId() != 0);

        // when
        em.getTransaction().begin();
        em.remove(artifact);
        em.getTransaction().commit();

        // then
        assertTrue(em.find(Artifact.class, artifactId) == null);
    }

    private void insertBasicTargetRepository() {
        this.targetRepository = TargetRepository.newBuilder()
                .identifier("Indy")
                .repositoryPath("/api")
                .repositoryType(RepositoryType.MAVEN)
                .temporaryRepo(false)
                .build();
        em.getTransaction().begin();
        em.persist(targetRepository);
        em.getTransaction().commit();
    }

    private Artifact.Builder prepareArtifactBuilder() {
        return Artifact.Builder.newBuilder()
                .identifier("g:a:v")
                .targetRepository(targetRepository)
                .md5("md5")
                .sha1("sha1")
                .sha256("sha256");
    }
}
