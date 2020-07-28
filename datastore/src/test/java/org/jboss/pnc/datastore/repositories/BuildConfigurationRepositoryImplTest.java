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
package org.jboss.pnc.datastore.repositories;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 10/18/16 Time: 8:05 AM
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationRepositoryImplTest {

    private final String MVN_DEFAULT_ALIGN_PARAMS = "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true";
    private final String NPM_DEFAULT_ALIGN_PARAMS = "";
    private final String GRADLE_DEFAULT_ALIGN_PARAMS = "--info -DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DignoreUnresolvableDependencies=true";

    @Inject
    private BuildConfigurationRepository repository;

    @Inject
    Producers producers;

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Test
    public void shouldCreateBuildConfiguration() {
        repository.save(producers.createValidBuildConfiguration(randomAlphabetic(10)));
    }

    @Test
    public void shouldNotCreateBuildConfigurationWithDuplicatedName() {
        String name = randomAlphabetic(10);
        repository.save(producers.createValidBuildConfiguration(name));
        BuildConfiguration duplicatedConfiguration = producers.createValidBuildConfiguration(name);
        assertThrows(
                () -> repository.save(duplicatedConfiguration),
                org.hibernate.exception.ConstraintViolationException.class);
    }

    @Test
    public void shouldCreateBuildConfigurationWithNameOfArchivedConfiguration() {
        String name = randomAlphabetic(10);
        BuildConfiguration archived = producers.createValidBuildConfiguration(name);
        archived.setArchived(true);
        repository.save(archived);
        repository.save(producers.createValidBuildConfiguration(name));
    }

    @Test
    public void shouldCreateBuildConfigurationWithTwoArchivedWithTheSameName() {
        String name = randomAlphabetic(10);
        BuildConfiguration config1 = producers.createValidBuildConfiguration(name);
        BuildConfiguration config2 = producers.createValidBuildConfiguration(name);
        config1.setArchived(true);
        repository.save(config1);
        config2.setArchived(true);
        repository.save(config2);
        repository.save(producers.createValidBuildConfiguration(name));
    }

    @Test
    public void shouldCreateAndUpdateBuildConfigurationWithDefaultAlignmentParams() {
        String name = randomAlphabetic(10);
        BuildConfiguration config1 = producers.createValidBuildConfiguration(name);
        BuildConfiguration savedBC = repository.save(config1);
        assertThat(savedBC.getDefaultAlignmentParams().contains("-DversionSuffixStrip="));
        savedBC.setBuildType(BuildType.GRADLE);
        savedBC = repository.save(savedBC);
        assertThat(savedBC.getDefaultAlignmentParams().contains("-DignoreUnresolvableDependencies=true"));
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

}