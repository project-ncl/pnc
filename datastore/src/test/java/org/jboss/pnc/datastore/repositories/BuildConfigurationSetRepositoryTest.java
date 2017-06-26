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
package org.jboss.pnc.datastore.repositories;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.pnc.datastore.DeploymentFactory;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.SystemImageType;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.isNotArchived;
import static org.jboss.pnc.spi.datastore.predicates.BuildConfigurationPredicates.withBuildConfigurationSetId;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildConfigurationSetRepositoryTest {

    @Deployment
    public static Archive<?> getDeployment() {
        return DeploymentFactory.createDatastoreDeployment();
    }

    @Inject
    BuildConfigurationSetRepository buildConfigurationSetRepository;

    @Inject
    BuildConfigurationRepository buildConfigurationRepository;

    @Inject
    RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    BuildEnvironmentRepository buildEnvironmentRepository;

    @Inject
    ProjectRepository projectRepository;

    @Test
    public void shouldSaveBCSetWithBCs() {
        //given
        BuildConfigurationFactory buildConfigurationFactory = new BuildConfigurationFactory();

        BuildConfiguration buildConfiguration1 = buildConfigurationFactory.createDetached("One");
        BuildConfiguration buildConfiguration2 = buildConfigurationFactory.createDetached("Two");
        BuildConfiguration buildConfiguration3 = buildConfigurationFactory.createDetached("Three");

        BuildConfigurationSet buildConfigurationSet = BuildConfigurationSet.Builder.newBuilder()
                .name("Build Group")
                .buildConfiguration(buildConfiguration1)
//                .buildConfiguration(buildConfiguration2)
//                .buildConfiguration(buildConfiguration3)
                .build();

        //when
        BuildConfigurationSet buildConfigurationSetSaved = buildConfigurationSetRepository.save(buildConfigurationSet);

        //then
        List<BuildConfiguration> buildConfigurationsLoaded = buildConfigurationRepository.queryWithPredicates(
                withBuildConfigurationSetId(buildConfigurationSetSaved.getId()),
                isNotArchived());

        assertThat(buildConfigurationsLoaded).isNotEmpty();
        assertThat(buildConfigurationsLoaded.size()).isEqualTo(1);
    }

    private class BuildConfigurationFactory {
        RepositoryConfiguration repositoryConfiguration;
        BuildEnvironment buildEnvironment;
        Project project;

        BuildConfigurationFactory() {
            repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder().internalUrl("http://Internal/").build();
            repositoryConfigurationRepository.save(repositoryConfiguration);
            buildEnvironment = BuildEnvironment.Builder.newBuilder()
                    .name("friendly environment")
                    .systemImageId("fe-10")
                    .systemImageType(SystemImageType.DOCKER_IMAGE)
                    .build();
            buildEnvironmentRepository.save(buildEnvironment);
            project = Project.Builder.newBuilder().name("top project").build();
            projectRepository.save(project);

        }

        BuildConfiguration createBuildConfiguration(String name) {

            BuildConfiguration buildConfiguration = BuildConfiguration.Builder.newBuilder()
                    .name(name)
                    .repositoryConfiguration(repositoryConfiguration)
                    .buildEnvironment(buildEnvironment)
                    .project(project)
                    .creationTime(new Date())
                    .lastModificationTime(new Date())
                    .build();

            return buildConfigurationRepository.save(buildConfiguration);
        }

        BuildConfiguration createDetached(String name) {
            BuildConfiguration buildConfiguration = createBuildConfiguration(name);
            return BuildConfiguration.Builder.newBuilder().id(buildConfiguration.getId()).build();
        }
    }
}
