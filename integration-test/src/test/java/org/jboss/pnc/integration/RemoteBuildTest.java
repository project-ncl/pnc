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
package org.jboss.pnc.integration;

import org.jboss.pnc.enums.RebuildMode;
import org.jboss.pnc.test.category.DebugTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;
import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.integration.setup.RestClientConfiguration;
import org.jboss.pnc.rest.api.parameters.BuildParameters;
import org.jboss.pnc.restclient.AdvancedBuildConfigurationClient;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Category(DebugTest.class)
public class RemoteBuildTest {

    private static AdvancedBuildConfigurationClient buildConfigurationRestClient;

    @Before
    public void before() {
        buildConfigurationRestClient = new AdvancedBuildConfigurationClient(RestClientConfiguration.asUser());
    }

    @Test
    public void runMultipleBuilds() throws Exception {
        String bcID = prepareBuildConfiguration("test-build-execution");

        BuildParameters buildOptions = new BuildParameters();
        buildOptions.setBuildDependencies(false);
        buildOptions.setRebuildMode(RebuildMode.FORCE);

        for (int i = 0; i < 20; i++) {
            CompletableFuture<Build> executeBuild = buildConfigurationRestClient.executeBuild(bcID, buildOptions);
            Build build = executeBuild.get(1, TimeUnit.MINUTES);
            assertThat(build.getStatus().completedSuccessfully()).isTrue();
        }
    }

    public String prepareBuildConfiguration(String buildConfigurationName) throws RemoteResourceException {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("CUSTOM_PME_PARAMETERS", "-Dmanipulation.disable=true");

        String buildScript = "set +x\n" + "for i in {1..100}; do\n"
                + "    echo $i \"- 0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789\"\n"
                + "done\n" + "echo \"Need to rest for a while ...\"\n" + "sleep 30";

        BuildConfiguration buildConfiguration = getOrCreateBuildConfiguration(
                buildConfigurationName,
                buildScript,
                "master",
                parameters,
                "8",
                "9",
                "3");

        return buildConfiguration.getId();
    }

    private BuildConfiguration getOrCreateBuildConfiguration(
            String buildConfigurationName,
            String buildScript,
            String scmRevision,
            Map<String, String> genericParameters,
            String projectId,
            String repositoryConfigurationId,
            String buildEnvironmentId) throws RemoteResourceException {

        RemoteCollection<BuildConfiguration> existing = buildConfigurationRestClient
                .getAll(Optional.empty(), Optional.of("name==" + buildConfigurationName));
        Iterator<BuildConfiguration> it = existing.iterator();

        if (!it.hasNext()) {
            ProjectRef project = ProjectRef.refBuilder().id(projectId).build();
            SCMRepository repository = SCMRepository.builder().id(repositoryConfigurationId).build();
            Environment environment = Environment.builder().id(buildEnvironmentId).build();
            BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                    .name(buildConfigurationName)
                    .project(project)
                    .scmRepository(repository)
                    .scmRevision(scmRevision)
                    .environment(environment)
                    .buildScript(buildScript)
                    .parameters(genericParameters)
                    .build();
            BuildConfiguration created = buildConfigurationRestClient.createNew(buildConfiguration);
            return created;
        } else {
            return it.next();
        }
    }
}
