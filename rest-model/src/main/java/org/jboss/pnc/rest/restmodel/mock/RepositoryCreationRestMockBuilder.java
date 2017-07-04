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
import org.jboss.pnc.rest.restmodel.BuildConfigurationRest;
import org.jboss.pnc.rest.restmodel.BuildEnvironmentRest;
import org.jboss.pnc.rest.restmodel.ProjectRest;
import org.jboss.pnc.rest.restmodel.RepositoryConfigurationRest;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationRest;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryCreationRestMockBuilder {

    public static RepositoryCreationRest mock(String buildConfigurationName, String script, String scmRepoUrl) {
        BuildConfigurationRest buildConfiguration = new BuildConfigurationRest();
        buildConfiguration.setId(1);
        buildConfiguration.setName(buildConfigurationName);

        BuildEnvironment buildEnvironment = BuildEnvironment.Builder.newBuilder()
                .id(1)
                .build();
        buildConfiguration.setEnvironment(new BuildEnvironmentRest(buildEnvironment));

        ProjectRest projectRest = new ProjectRest();
        projectRest.setId(1);
        buildConfiguration.setProject(projectRest);
        buildConfiguration.setBuildScript(script);

        RepositoryConfigurationRest repositoryConfiguration = new RepositoryConfigurationRest();
        repositoryConfiguration.setId(1);
        repositoryConfiguration.setInternalUrl(scmRepoUrl);

        return new RepositoryCreationRest(repositoryConfiguration, buildConfiguration);

    }
}
