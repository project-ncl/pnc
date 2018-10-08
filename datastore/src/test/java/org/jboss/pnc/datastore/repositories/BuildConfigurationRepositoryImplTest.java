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
package org.jboss.pnc.datastore.repositories;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.assertj.core.api.Assertions.fail;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 10/18/16
 * Time: 8:05 AM
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationRepositoryImplTest {
    @Inject
    private BuildConfigurationRepository repository;

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private BuildEnvironmentRepository environmentRepository;

    @Inject
    private ProjectRepository projectRepository;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Test
    public void shouldCreateBuildConfiguration() {
        repository.save(createValidBuildConfiguration(randomAlphabetic(10)));
    }

    @Test
    public void shouldNotCreateBuildConfigurationWithDuplicatedName() {
        String name = randomAlphabetic(10);
        repository.save(createValidBuildConfiguration(name));
        BuildConfiguration duplicatedConfiguration = createValidBuildConfiguration(name);
        assertThrows(
                () -> repository.save(duplicatedConfiguration),
                org.hibernate.exception.ConstraintViolationException.class
        );
    }

    @Test
    public void shouldCreateBuildConfigurationWithNameOfArchivedConfiguration() {
        String name = randomAlphabetic(10);
        BuildConfiguration archived = createValidBuildConfiguration(name);
        archived.setArchived(true);
        repository.save(archived);
        repository.save(createValidBuildConfiguration(name));
    }

    @Test
    public void shouldCreateBuildConfigurationWithTwoArchivedWithTheSameName() {
        String name = randomAlphabetic(10);
        BuildConfiguration config1 = createValidBuildConfiguration(name);
        BuildConfiguration config2 = createValidBuildConfiguration(name);
        config1.setArchived(true);
        repository.save(config1);
        config2.setArchived(true);
        repository.save(config2);
        repository.save(createValidBuildConfiguration(name));
    }


    private void assertThrows(Runnable runnable, Class<? extends Exception> exceptionClass) {
        try {
            runnable.run();
        } catch (Exception e) {
            if (exceptionsInStack(e).contains(exceptionClass)) {
                return;
            } else {
                fail("Unexpected exception thrown. Expecting: " + exceptionClass, e);
            }
        }
        fail("Expected exception not thrown");
    }

    private List<Class<? extends Throwable>> exceptionsInStack(Throwable e) {
        List<Class<? extends Throwable>> exceptionsInStack = new ArrayList<>();

        while (e != null) {
            exceptionsInStack.add(e.getClass());
            e = e.getCause();
        }
        return exceptionsInStack;
    }

    private BuildConfiguration createValidBuildConfiguration(String name) {
        return BuildConfiguration.Builder.newBuilder()
                .buildEnvironment(buildEnv())
                .project(project())
                .name(name)
                .repositoryConfiguration(repositoryConfiguration())
                .build();
    }

    private RepositoryConfiguration repositoryConfiguration() {
        RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder
                .newBuilder()
                .internalUrl(randomAlphabetic(20))
                .build();
        repositoryConfigurationRepository.save(repositoryConfiguration);

        return  repositoryConfiguration;
    }

    private Project project() {
        Project project = Project.Builder.newBuilder().name(randomAlphabetic(20)).build();
        projectRepository.save(project);
        return project;
    }

    private BuildEnvironment buildEnv() {
        BuildEnvironment environment = BuildEnvironment.Builder.newBuilder()
                .name(randomAlphabetic(10))
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .systemImageId(randomNumeric(10))
                .build();
        environmentRepository.save(environment);
        return environment;
    }

}