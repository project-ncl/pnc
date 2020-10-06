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

import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.mock.repository.SequenceHandlerRepositoryMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.spi.datastore.repositories.BuildEnvironmentRepository;
import org.jboss.pnc.spi.datastore.repositories.ProjectRepository;
import org.jboss.pnc.spi.datastore.repositories.RepositoryConfigurationRepository;
import org.jboss.pnc.spi.datastore.repositories.SequenceHandlerRepository;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import java.util.Date;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Dependent
public class Producers {

    @Inject
    private RepositoryConfigurationRepository repositoryConfigurationRepository;

    @Inject
    private BuildEnvironmentRepository buildEnvironmentRepository;

    @Inject
    private ProjectRepository projectRepository;

    SequenceHandlerRepository sequenceHandlerRepository = new SequenceHandlerRepositoryMock();

    RepositoryConfiguration repositoryConfiguration() {
        RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
                .internalUrl(randomAlphabetic(20))
                .build();
        repositoryConfigurationRepository.save(repositoryConfiguration);

        return repositoryConfiguration;
    }

    BuildEnvironment buildEnv() {
        final String name = randomAlphabetic(10);
        BuildEnvironment environment = BuildEnvironment.Builder.newBuilder()
                .name(name)
                .systemImageRepositoryUrl("https://example.com/" + name)
                .systemImageType(SystemImageType.DOCKER_IMAGE)
                .systemImageId(randomNumeric(10))
                .build();
        buildEnvironmentRepository.save(environment);
        return environment;
    }

    BuildConfiguration createValidBuildConfiguration(String name) {
        return BuildConfiguration.Builder.newBuilder()
                .id(sequenceHandlerRepository.getNextID(BuildConfiguration.SEQUENCE_NAME).intValue())
                .creationTime(new Date())
                .buildEnvironment(buildEnv())
                .project(project())
                .name(name)
                .buildType(BuildType.MVN)
                .repositoryConfiguration(repositoryConfiguration())
                .build();
    }

    Project project() {
        Project project = Project.Builder.newBuilder().name(randomAlphabetic(20)).build();
        projectRepository.save(project);
        return project;
    }
}
