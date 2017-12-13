package org.jboss.pnc.model;

import org.apache.maven.model.Build;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Jakub Bartecek
 */
public class BuildRecordTest extends AbstractModelTest {

    protected final RepositoryConfiguration REPOSITORY_CONFIGURATION_ID_1 = RepositoryConfiguration.Builder
            .newBuilder().id(1).build();

    private EntityManager em;

    private User user = null;

    private BuildEnvironment buildEnvironment = null;

    @Before
    public void init() throws Exception {
        clearDatabaseTables();

        this.em = getEmFactory().createEntityManager();

        initDatabaseUsingDataset(em, BasicModelTest.DBUNIT_DATASET_FILE);
        insertExampleBuildConfigurations(em, REPOSITORY_CONFIGURATION_ID_1);

        if(user == null) {
            this.user = User.Builder.newBuilder()
                    .id(1)
                    .build();
        }

        if (buildEnvironment == null) {
            this.buildEnvironment = BuildEnvironment.Builder
                    .newBuilder()
                    .id(1)
                    .build();
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
        int brId = 666;
        BuildRecord br = prepareBuildRecordBuilder()
                .id(brId)
                .temporaryBuild(false)
                .build();

        em.getTransaction().begin();
        em.persist(br);
        em.getTransaction().commit();

        // when, then
        try {
            em.getTransaction().begin();
            em.remove(br);
            em.getTransaction().commit();
        } catch (PersistenceException ex) {
            BuildRecord obtainedBr = em.find(BuildRecord.class, brId);
            assertNotNull(obtainedBr);
            assertEquals(brId, obtainedBr.getId().intValue());
            return;
        }
        fail("Deletion of the standard BuildRecord should be prohibited.");
    }

    @Test
    public void shouldAllowDeletionOfTemporaryBuild() {
        // given
        int brId = 666;
        BuildRecord br = prepareBuildRecordBuilder()
                .id(brId)
                .temporaryBuild(true)
                .build();

        // when
        em.getTransaction().begin();
        em.persist(br);
        em.getTransaction().commit();

        // then
        assertTrue(br.getId() != null);
        assertTrue(br.getId() != 0);
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
