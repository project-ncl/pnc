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

import org.jboss.pnc.enums.SystemImageType;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class BuildConfigurationDependencyTest extends AbstractModelTest {

    protected final RepositoryConfiguration basicRepositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
            .id(1)
            .build();

    @After
    public void cleanup() {
        clearDatabaseTables();
    }

    public BuildEnvironment getBuildEnvironment() {
        return BuildEnvironment.Builder.newBuilder()
                .name("Test build system")
                .description("Test build system description")
                .systemImageId("123456")
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .build();
    }

    public Project getProject() {
        return Project.Builder.newBuilder()
                .name("Test Project 1")
                .description("Test Project 1 Description")
                .issueTrackerUrl("http://isssues.jboss.org")
                .build();
    }

    public BuildConfiguration.Builder getBuildConfigBuilder() {
        return BuildConfiguration.Builder.newBuilder()
                .name("Generic Build Configuration")
                .description("Generic Build Configuration Description")
                .repositoryConfiguration(basicRepositoryConfiguration)
                .buildScript("mvn install");
    }

    @Test
    public void testBuildConfigurationDependencies() throws Exception {

        // Set up sample build configurations, the id needs to be set manually
        // because the configs are not stored to the database.
        BuildConfiguration buildConfig1 = getBuildConfigBuilder().id(1).build();
        BuildConfiguration buildConfig2 = getBuildConfigBuilder().id(2).build();
        BuildConfiguration buildConfig3 = getBuildConfigBuilder().id(3).build();
        BuildConfiguration buildConfig4 = getBuildConfigBuilder().id(4).build();
        BuildConfiguration buildConfig5 = getBuildConfigBuilder().id(5).build();
        BuildConfiguration buildConfig6 = getBuildConfigBuilder().id(6).build();
        BuildConfiguration buildConfig7 = getBuildConfigBuilder().id(7).build();
        BuildConfiguration buildConfig8 = getBuildConfigBuilder().id(8).build();

        // Set up the dependency relationships
        buildConfig1.addDependency(buildConfig2);
        buildConfig1.addDependency(buildConfig3);
        buildConfig2.addDependency(buildConfig4);
        buildConfig2.addDependency(buildConfig5);

        // Verify that at this point buildConfig1 has 2 indirect dependencies, and 4 total dependencies
        Assert.assertEquals(2, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(4, buildConfig1.getAllDependencies().size());

        // Add more indirect dependencies onto config 3 and 4
        buildConfig4.addDependency(buildConfig6);
        buildConfig4.addDependency(buildConfig7);
        buildConfig3.addDependency(buildConfig8);
        Assert.assertEquals(5, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(7, buildConfig1.getAllDependencies().size());

        // Add an indirect dependency which is also a direct dependency
        buildConfig7.addDependency(buildConfig3);
        Assert.assertEquals(6, buildConfig1.getIndirectDependencies().size());
        Assert.assertEquals(7, buildConfig1.getAllDependencies().size());
    }

    @Test(expected = PersistenceException.class)
    public void testBuildConfigurationDependenciesInDatabase() throws Exception {

        Project project1 = getProject();
        BuildEnvironment buildEnvironment = getBuildEnvironment();

        BuildConfiguration buildConfig1 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig2 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig3 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig4 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig5 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig6 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig7 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();
        BuildConfiguration buildConfig8 = getBuildConfigBuilder().project(project1)
                .buildEnvironment(buildEnvironment)
                .build();

        EntityManager em = getEmFactory().createEntityManager();
        EntityTransaction insertConfigTx = em.getTransaction();

        try {
            insertConfigTx.begin();
            em.persist(buildEnvironment);
            em.persist(project1);
            em.persist(buildConfig1);
            em.persist(buildConfig2);
            em.persist(buildConfig3);
            em.persist(buildConfig4);
            em.persist(buildConfig5);
            em.persist(buildConfig6);
            em.persist(buildConfig7);
            em.persist(buildConfig8);
            insertConfigTx.commit();

            Query rowCountQuery = em.createQuery("select count(*) from BuildConfiguration bc ");
            Long count = (Long) rowCountQuery.getSingleResult();
            // Should have 8 build configurations
            Assert.assertEquals(8, count.longValue());
        } catch (RuntimeException e) {
            if (insertConfigTx != null && insertConfigTx.isActive()) {
                insertConfigTx.rollback();
            }
            throw e;
        }

        // Set up the dependency relationships
        buildConfig1.addDependency(buildConfig2);
        buildConfig1.addDependency(buildConfig3);
        buildConfig2.addDependency(buildConfig4);
        buildConfig2.addDependency(buildConfig5);
        buildConfig4.addDependency(buildConfig6);
        buildConfig4.addDependency(buildConfig7);
        buildConfig3.addDependency(buildConfig8);
        buildConfig7.addDependency(buildConfig3);

        // Next check that the dependency relationships can be stored to the db
        // and that the dependency calculations are correct
        EntityTransaction updateDependenciesTx = em.getTransaction();
        try {
            updateDependenciesTx.begin();
            em.persist(buildConfig1);
            em.persist(buildConfig2);
            em.persist(buildConfig3);
            em.persist(buildConfig4);
            em.persist(buildConfig5);
            em.persist(buildConfig6);
            em.persist(buildConfig7);
            em.persist(buildConfig8);
            updateDependenciesTx.commit();

            buildConfig1 = em.find(BuildConfiguration.class, buildConfig1.getId());
            Assert.assertEquals(6, buildConfig1.getIndirectDependencies().size());
            Assert.assertEquals(7, buildConfig1.getAllDependencies().size());

        } catch (RuntimeException e) {
            if (updateDependenciesTx != null && updateDependenciesTx.isActive()) {
                updateDependenciesTx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }

        // Next, attempt to add a circular dependency this should throw a PersistenceException
        buildConfig8.addDependency(buildConfig1);
    }

    @Test(expected = PersistenceException.class)
    public void testBuildConfigurationSelfReferenceCheck() throws Exception {
        BuildConfiguration buildConfig1 = getBuildConfigBuilder().id(1).build();
        buildConfig1.addDependency(buildConfig1);
    }

    @Test
    public void testBuildConfigurationCircularDependencies() throws Exception {
        Project project1 = getProject();
        BuildEnvironment buildEnvironmentDefault = getBuildEnvironment();

        // Set up sample build configurations, the id needs to be set manually
        // because the configs are not stored to the database.
        BuildConfiguration buildConfig1 = getBuildConfigBuilder().id(1)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig2 = getBuildConfigBuilder().id(2)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig3 = getBuildConfigBuilder().id(3)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig4 = getBuildConfigBuilder().id(4)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig5 = getBuildConfigBuilder().id(5)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig6 = getBuildConfigBuilder().id(6)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig7 = getBuildConfigBuilder().id(7)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();
        BuildConfiguration buildConfig8 = getBuildConfigBuilder().id(8)
                .project(project1)
                .buildEnvironment(buildEnvironmentDefault)
                .build();

        // Set up the dependency relationships
        buildConfig1.addDependency(buildConfig2);
        buildConfig1.addDependency(buildConfig3);
        buildConfig2.addDependency(buildConfig4);
        buildConfig2.addDependency(buildConfig5);
        buildConfig4.addDependency(buildConfig6);
        buildConfig4.addDependency(buildConfig7);
        buildConfig3.addDependency(buildConfig8);

        List<BuildConfiguration> depPath = buildConfig2.dependencyDepthFirstSearch(buildConfig1);
        Assert.assertEquals(1, depPath.size());

        // Add circular dependency, by directly modifying the dependency relation
        buildConfig8.getDependencies().add(buildConfig1);
        buildConfig1.getDependants().add(buildConfig8);
        depPath = buildConfig1.dependencyDepthFirstSearch(buildConfig1);
        for (BuildConfiguration dep : depPath) {
            System.out.print(dep.getName() + " " + dep.getId() + " -> ");
        }
        System.out.println();
        Assert.assertEquals(4, depPath.size());

    }
}
