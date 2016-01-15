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

package org.jboss.pnc.executor.mock.builders;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.Project;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectConfigurationBuilder { //TODO reuse TestProjectConfigurationBuilder from pnc-mock

    public static final String FAIL = "mvn clean install -Dmock.config=Fail";
    public static final String PASS = "mvn clean install -Dmock.config=Pass";

    BuildEnvironment javaBuildEnvironment = BuildEnvironment.Builder.newBuilder().build();

    public BuildConfiguration build(int id, String name) {
        return build(id, name, null);
    }

    public BuildConfiguration build(int id, String name, BuildConfigurationSet buildConfigurationSet) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(id);
        buildConfiguration.setBuildScript(PASS);
        buildConfiguration.setName(id + "");
        buildConfiguration.setBuildEnvironment(javaBuildEnvironment);
        buildConfiguration.setProject(project);
        project.addBuildConfiguration(buildConfiguration);
        if (buildConfigurationSet != null) {
            buildConfigurationSet.addBuildConfiguration(buildConfiguration);
        }

        return buildConfiguration;
    }

    public BuildConfiguration buildFailingConfiguration(int id, String name, BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration =  build(id, name, buildConfigurationSet);
        buildConfiguration.setBuildScript(FAIL);
        return buildConfiguration;
    }

}
