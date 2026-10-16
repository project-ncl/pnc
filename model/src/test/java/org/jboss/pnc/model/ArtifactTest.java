/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;
import org.jboss.pnc.enums.RepositoryType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
