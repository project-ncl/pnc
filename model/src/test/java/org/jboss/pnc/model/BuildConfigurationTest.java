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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for BuildConfiguration entity
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public class BuildConfigurationTest extends AbstractModelTest {

    protected final Map<String, String> GENERIC_PARAMETERS_EMPTY = new HashMap<>();

    protected final BuildEnvironment BUILD_ENVIRONMENT_WITH_ID_1;

    protected final Project PROJECT_WITH_ID_1;

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder
            .newBuilder().id(1).build();

    private final String KEY1 = "key1";

    private final String VALUE1 = "value1";

    private final static String DBUNIT_DATASET_FILE = "basic-model-test-data.xml";

    private EntityManager em;

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
                .name("Test Build Configuration 1")
                .description("Test Build Configuration 1 Description").project(PROJECT_WITH_ID_1)
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install")
                .genericParameters(GENERIC_PARAMETERS_EMPTY)
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1).build();

        em.getTransaction().begin();
        em.persist(original);
        em.getTransaction().commit();

        BuildConfiguration obtained = em.find(BuildConfiguration.class, original.getId());
        assertEquals(0, obtained.getGenericParameters().size());
    }

    @Test(expected = RollbackException.class)
    public void testFailToCreateBCWithoutRepoConfig() {
        BuildConfiguration bc = BuildConfiguration.Builder.newBuilder()
                .name("Test Build Configuration 1")
                .project(PROJECT_WITH_ID_1)
                .buildScript("mvn install")
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1).build();

        em.getTransaction().begin();
        em.persist(bc);
        em.getTransaction().commit();
    }

    @Test(expected = RollbackException.class)
    @Ignore // TODO Unignore, once the relationship constraint is added
    public void testFailToChangeRepoConfigInBC() {
        BuildConfiguration bc = BuildConfiguration.Builder.newBuilder()
                .name("Test Build Configuration 1")
                .project(PROJECT_WITH_ID_1)
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install")
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1).build();

        RepositoryConfiguration repoConfig = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl("example.com")
                .build();

        em.getTransaction().begin();
        em.persist(bc);
        em.persist(repoConfig);
        bc.setRepositoryConfiguration(repoConfig);
        em.getTransaction().commit();
    }

    @Test
    public void testAddGenericParameters() {
        Map<String, String> genericParameters = new HashMap<>();
        genericParameters.put(KEY1, VALUE1);

        BuildConfiguration original = createBc("Test Build Configuration 1",
                "Test Build Configuration 1 Description", genericParameters);

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

        BuildConfiguration original1 = createBc("Test Build Configuration 1",
                "Test Build Configuration 1 Description", genericParameters);
        BuildConfiguration original2 = createBc("Test Build Configuration 2",
                "Test Build Configuration 2 Description", genericParameters);

        em.getTransaction().begin();
        em.persist(original1);
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

    private BuildConfiguration createBc(String name, String description,
            Map<String, String> genericParameters) {
        return BuildConfiguration.Builder.newBuilder().name(name)
                .description(description).project(PROJECT_WITH_ID_1)
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install")
                .genericParameters(genericParameters)
                .buildEnvironment(BUILD_ENVIRONMENT_WITH_ID_1).build();

    }

}
