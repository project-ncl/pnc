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
package org.jboss.pnc.rest.restmodel.mock;

import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationUrlAutoRest;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryCreationUrlAutoRestMockBuilder {

    public static RepositoryCreationUrlAutoRest mock(String buildConfigurationName, String script, String scmUrl) {
        return mock(buildConfigurationName, script, scmUrl, true, Optional.empty(), Optional.empty());
    }

    public static RepositoryCreationUrlAutoRest mock(
            String buildConfigurationName,
            String script,
            String scmUrl,
            boolean preBuildSyncEnabled,
            Optional<Integer> buildConfigurationId,
            Optional<Integer> repositoryConfigurationId) {
        BuildConfigurationRest buildConfiguration = new BuildConfigurationRest();
        buildConfigurationId.ifPresent(id -> buildConfiguration.setId(id));

        buildConfiguration.setName(buildConfigurationName);

        BuildEnvironment buildEnvironment = BuildEnvironment.Builder.newBuilder()
                .id(1)
                .build();
        buildConfiguration.setBuildType(BuildType.MVN);
        buildConfiguration.setEnvironment(new BuildEnvironmentRest(buildEnvironment));

        ProjectRest projectRest = new ProjectRest();
        projectRest.setId(1);
        buildConfiguration.setProject(projectRest);
        buildConfiguration.setBuildScript(script);


        return new RepositoryCreationUrlAutoRest(scmUrl, preBuildSyncEnabled, buildConfiguration);

    }
}
