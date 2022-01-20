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

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Tests for BuildConfiguration entity
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class BuildConfigurationTest extends AbstractModelTest {

    private Logger logger = LoggerFactory.getLogger(BuildConfigurationTest.class);

    protected final Map<String, String> GENERIC_PARAMETERS_EMPTY = new HashMap<>();

    protected final BuildEnvironment BUILD_ENVIRONMENT_WITH_ID_1;

    protected final Project PROJECT_WITH_ID_1;

    protected final RepositoryConfiguration REPOSITORY_CONFIGURATION_ID_1 = RepositoryConfiguration.Builder.newBuilder()
            .id(1)
            .build();

    private final String KEY1 = "key1";

    private final String VALUE1 = "value1";

    private final static String DBUNIT_DATASET_FILE = "basic-model-test-data.xml";

    private EntityManager em;

    private static AtomicInteger buildConfigurationSequence = new AtomicInteger();

    public BuildConfigurationTest() {
        BUILD_ENVIRONMENT_WITH_ID_1 = BuildEnvironment.Builder.newBuilder().id(1).build();
        PROJECT_WITH_ID_1 = Project.Builder.newBuilder().id(1).build();
    }

    @Before
    public void init() throws Exception {
        em = getEmFactory().createEntityManager();
        initDatabaseUsingDataset(em, DBUNIT_DATASET_FILE);
    }

    @After
    public void cleanup() {
        clearDatabaseTables();
        em.close();
    }

    @Test
    public void testAddEmptyGenericParameters() {
        BuildConfiguration original = BuildConfiguration.Builder.newBuilder()
                .id(buildConfigurationSequence.incrementAndGet())
                .name("Test Build Configuration 1")
                .description("Test Build Configuration 1 Description")
                .project(PROJECT_WITH_ID_1)
                .repositoryConfiguration(REPOSITORY_CONFIGURATION_ID_1)
                .buildScript("mvn install")
                .genericParameters(GENERIC_PARAMETERS_EMPTY)
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1)
                .build();

        em.getTransaction().begin();
        em.persist(original);
        em.getTransaction().commit();

        BuildConfiguration obtained = em.find(BuildConfiguration.class, original.getId());
        assertEquals(0, obtained.getGenericParameters().size());
    }

    @Test(expected = RollbackException.class)
    public void testFailToCreateBCWithoutRepoConfig() {
        BuildConfiguration bc = BuildConfiguration.Builder.newBuilder()
                .id(buildConfigurationSequence.incrementAndGet())
                .name("Test Build Configuration 1")
                .project(PROJECT_WITH_ID_1)
                .buildScript("mvn install")
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1)
                .build();

        em.getTransaction().begin();
        em.persist(bc);
        em.getTransaction().commit();
    }

    @Test
    public void testAllowToChangeRepoConfigInBC() {
        RepositoryConfiguration defaultRepositoryConfiguration = em.find(RepositoryConfiguration.class, 1);
        RepositoryConfiguration secondRepositoryConfiguration = em.find(RepositoryConfiguration.class, 2);

        BuildConfiguration bc = BuildConfiguration.Builder.newBuilder()
                .id(buildConfigurationSequence.incrementAndGet())
                .name("Test Build Configuration 1")
                .project(PROJECT_WITH_ID_1)
                .repositoryConfiguration(defaultRepositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1)
                .build();

        em.getTransaction().begin();
        em.persist(bc);
        em.getTransaction().commit();

        em.getTransaction().begin();
        BuildConfiguration loadedBc = em.find(BuildConfiguration.class, bc.getId());
        loadedBc.setRepositoryConfiguration(secondRepositoryConfiguration);
        em.getTransaction().commit();
        em.clear();

        assertEquals(
                2,
                em.find(BuildConfiguration.class, loadedBc.getId()).getRepositoryConfiguration().getId().intValue());
    }

    @Test
    public void testAddGenericParameters() {
        Map<String, String> genericParameters = new HashMap<>();
        genericParameters.put(KEY1, VALUE1);

        BuildConfiguration original = createBc(
                "Test Build Configuration 1",
                "Test Build Configuration 1 Description",
                genericParameters);

        em.getTransaction().begin();
        em.persist(original);
        em.getTransaction().commit();

        BuildConfiguration obtained = em.find(BuildConfiguration.class, original.getId());
        assertEquals(1, obtained.getGenericParameters().size());
        assertEquals(VALUE1, obtained.getGenericParameters().get(KEY1));
    }

    @Test
    public void testAddSameGenericParametersInto2BCs() {
        final String KEY2 = "key2";
        final String VALUE2 = "value2";
        Map<String, String> genericParameters = new HashMap<>();
        genericParameters.put(KEY1, VALUE1);
        genericParameters.put(KEY2, VALUE2);

        BuildConfiguration original1 = createBc(
                "Test Build Configuration 1",
                "Test Build Configuration 1 Description",
                genericParameters);
        BuildConfiguration original2 = createBc(
                "Test Build Configuration 2",
                "Test Build Configuration 2 Description",
                genericParameters);

        em.getTransaction().begin();
        logger.info("Saving {}", original1);
        em.persist(original1);
        logger.info("Saving {}", original2);
        em.persist(original2);
        em.getTransaction().commit();

        BuildConfiguration obtained1 = em.find(BuildConfiguration.class, original1.getId());
        assertEquals(2, obtained1.getGenericParameters().size());
        assertEquals(VALUE1, obtained1.getGenericParameters().get(KEY1));
        assertEquals(VALUE2, obtained1.getGenericParameters().get(KEY2));

        BuildConfiguration obtained2 = em.find(BuildConfiguration.class, original1.getId());
        assertEquals(2, obtained2.getGenericParameters().size());
        assertEquals(VALUE1, obtained2.getGenericParameters().get(KEY1));
        assertEquals(VALUE2, obtained2.getGenericParameters().get(KEY2));
    }

    @Test
    public void testRetrieveAuditedGenericParameters() {
        // given
        String key = "key";
        String initialValue = "initialValue";
        String updatedValue = "updatedValue";
        Map<String, String> initialParameters = new HashMap<>();
        initialParameters.put(key, initialValue);

        Map<String, String> updatedParameters = new HashMap<>();
        updatedParameters.put(key, updatedValue);

        // when
        BuildConfiguration buildConfiguration = createBc("auditing test", "description", initialParameters);
        em.getTransaction().begin();
        em.persist(buildConfiguration);
        em.getTransaction().commit();

        buildConfiguration.setGenericParameters(updatedParameters);
        buildConfiguration.setDescription("updated description");
        em.getTransaction().begin();
        em.persist(buildConfiguration);
        em.getTransaction().commit();

        // then
        BuildConfiguration obtained = em.find(BuildConfiguration.class, buildConfiguration.getId());

        AuditReader reader = AuditReaderFactory.get(em);
        List<Number> revisions = reader.getRevisions(BuildConfiguration.class, obtained.getId());

        assertEquals(2, revisions.size());

        Number firstRevision = revisions.get(0);
        BuildConfiguration oldBuildConfiguration = reader
                .find(BuildConfiguration.class, obtained.getId(), firstRevision);
        Number secondRevision = revisions.get(1);
        BuildConfiguration newBuildConfiguration = reader
                .find(BuildConfiguration.class, obtained.getId(), secondRevision);

        Assert.assertEquals(oldBuildConfiguration.getGenericParameters().get(key), initialValue);
        Assert.assertEquals(newBuildConfiguration.getGenericParameters().get(key), updatedValue);

        BuildConfiguration buildConfigurationOld = getByIdRev(buildConfiguration.getId(), firstRevision.intValue());
        BuildConfigurationAudited auditedOld = BuildConfigurationAudited
                .fromBuildConfiguration(buildConfigurationOld, firstRevision.intValue());

        Assert.assertEquals(auditedOld.getGenericParameters().get(key), initialValue);

        BuildConfiguration buildConfigurationNew = getByIdRev(buildConfiguration.getId(), secondRevision.intValue());
        BuildConfigurationAudited auditedNew = BuildConfigurationAudited
                .fromBuildConfiguration(buildConfigurationNew, secondRevision.intValue());

        Assert.assertEquals(auditedNew.getGenericParameters().get(key), updatedValue);

    }

    private BuildConfiguration getByIdRev(Integer buildConfigurationId, Integer revision) {
        return (BuildConfiguration) AuditReaderFactory.get(em)
                .createQuery()
                .forEntitiesAtRevision(BuildConfiguration.class, revision)
                .add(AuditEntity.id().eq(buildConfigurationId))
                .addOrder(AuditEntity.revisionNumber().desc())
                .getSingleResult();
    }

    private BuildConfiguration createBc(String name, String description, Map<String, String> genericParameters) {
        return BuildConfiguration.Builder.newBuilder()
                .id(buildConfigurationSequence.incrementAndGet())
                .name(name)
                .description(description)
                .project(PROJECT_WITH_ID_1)
                .repositoryConfiguration(REPOSITORY_CONFIGURATION_ID_1)
                .buildScript("mvn install")
                .genericParameters(genericParameters)
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1)
                .build();

    }

}
