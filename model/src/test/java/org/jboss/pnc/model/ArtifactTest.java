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

import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.enums.ArtifactQuality;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        Artifact artifact = prepareArtifactBuilder().artifactQuality(ArtifactQuality.NEW).build();
        int artifactId = storeArtifact(artifact);

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
        Artifact artifact = prepareArtifactBuilder().artifactQuality(ArtifactQuality.TEMPORARY).build();
        int artifactId = storeArtifact(artifact);

        // when
        em.getTransaction().begin();
        em.remove(artifact);
        em.getTransaction().commit();

        // then
        assertNull(em.find(Artifact.class, artifactId));
    }

    @Test
    public void shouldAllowDeletionOfDeletedQualityArtifacts() {
        // given
        Artifact artifact = prepareArtifactBuilder().artifactQuality(ArtifactQuality.DELETED).build();
        int artifactId = storeArtifact(artifact);

        // when
        em.getTransaction().begin();
        em.remove(artifact);
        em.getTransaction().commit();

        // then
        assertNull(em.find(Artifact.class, artifactId));
    }

    @Test
    public void shouldSetDefaultBuildCategory() {
        // given
        Artifact artifact = prepareArtifactBuilder().build();

        // when
        int artifactId = storeArtifact(artifact);

        // then
        Artifact foundArtifact = em.find(Artifact.class, artifactId);
        assertNotNull(foundArtifact);
        assertEquals(BuildCategory.STANDARD, foundArtifact.getBuildCategory());
    }

    @Test
    public void shouldUpdateBuildCategory() {
        // given
        Artifact artifact = prepareArtifactBuilder().build();
        int artifactId = storeArtifact(artifact);

        // when
        Artifact updatableArtifact = em.find(Artifact.class, artifactId);
        updatableArtifact.setBuildCategory(BuildCategory.SERVICE);

        em.getTransaction().begin();
        em.merge(updatableArtifact);
        em.getTransaction().commit();

        // then
        Artifact foundArtifact = em.find(Artifact.class, artifactId);
        assertNotNull(foundArtifact);
        assertEquals(BuildCategory.SERVICE, foundArtifact.getBuildCategory());
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

    private int storeArtifact(Artifact artifact) {
        em.getTransaction().begin();
        em.persist(artifact);
        em.getTransaction().commit();
        int artifactId = artifact.getId();
        assertNotNull(artifact.getId());
        assertTrue(artifact.getId() != 0);
        return artifactId;
    }
}
