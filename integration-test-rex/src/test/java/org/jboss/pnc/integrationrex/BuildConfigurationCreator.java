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
package org.jboss.pnc.integrationrex;

import org.jboss.pnc.auth.KeycloakClient;
import org.jboss.pnc.client.BuildConfigurationClient;
import org.jboss.pnc.client.EnvironmentClient;
import org.jboss.pnc.client.GroupConfigurationClient;
import org.jboss.pnc.client.ProjectClient;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.SCMRepositoryClient;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.BuildConfigurationRef;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.GroupConfiguration;
import org.jboss.pnc.dto.Project;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.enums.BuildType;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.jboss.pnc.integrationrex.setup.RestClientConfiguration.withBearerToken;

public class BuildConfigurationCreator {

    private final BuildConfigurationClient buildConfigurationClient;
    private final GroupConfigurationClient groupConfigurationClient;
    private final SCMRepository scmRepository;
    private final Project project;
    private final Environment environment;

    public BuildConfigurationCreator(String authServerUrl, String keycloakRealm) throws RemoteResourceException {
        String token = KeycloakClient
                .getAuthTokensBySecret(authServerUrl, keycloakRealm, "test-user", "test-pass", "pnc", "", false)
                .getToken();

        buildConfigurationClient = new BuildConfigurationClient(withBearerToken(token));
        SCMRepositoryClient scmRepositoryClient = new SCMRepositoryClient(withBearerToken(token));
        ProjectClient projectClient = new ProjectClient(withBearerToken(token));
        EnvironmentClient environmentClient = new EnvironmentClient(withBearerToken(token));
        groupConfigurationClient = new GroupConfigurationClient(withBearerToken(token));

        scmRepository = scmRepositoryClient.getAll("", "").iterator().next();
        project = projectClient.getAll().iterator().next();
        environment = environmentClient.getAll().iterator().next();
    }

    public BuildConfiguration newBuildConfiguration(String name) throws RemoteResourceException {
        return newBuildConfiguration(name, Collections.emptySet());
    }

    public BuildConfiguration newBuildConfiguration(String name, Set<BuildConfigurationRef> dependencies)
            throws RemoteResourceException {
        BuildConfiguration.Builder bcBuilder = BuildConfiguration.builder()
                .buildType(BuildType.MVN)
                .name(name)
                .buildScript("mvn clean deploy -Dname=" + name)
                .environment(environment)
                .scmRepository(scmRepository)
                .project(project);
        if (dependencies != null) {
            Map<String, BuildConfigurationRef> map = dependencies.stream()
                    .collect(Collectors.toMap(BuildConfigurationRef::getName, Function.identity()));
            bcBuilder.dependencies(map);
        }

        return buildConfigurationClient.createNew(bcBuilder.build());
    }

    public GroupConfiguration newGroupConfiguration(String name, Set<BuildConfiguration> buildConfigurations)
            throws RemoteResourceException {
        Map<String, BuildConfigurationRef> map = buildConfigurations.stream()
                .collect(Collectors.toMap(BuildConfiguration::getName, Function.identity()));

        GroupConfiguration groupConfiguration = GroupConfiguration.builder().name(name).buildConfigs(map).build();
        return groupConfigurationClient.createNew(groupConfiguration);
    }
}
