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
package org.jboss.pnc.core.test.configurationBuilders;

import org.jboss.pnc.model.*;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectConfigurationBuilder {

    public static final String FAIL = "Fail";
    public static final String PASS = "Pass";

    Environment javaEnvironment = Environment.Builder.defaultEnvironment().build();

    public BuildConfiguration buildConfigurationWhichDependsOnItself() {
        BuildConfiguration buildConfiguration = build(1, "depends-on-itself");
        buildConfiguration.addDependency(buildConfiguration);
        return buildConfiguration;
    }

    public BuildConfiguration buildConfigurationWithCycleDependency(BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration1 = build(1, "cycle-dependency-1", buildConfigurationSet);
        BuildConfiguration buildConfiguration2 = build(2, "cycle-dependency-2", buildConfigurationSet);
        BuildConfiguration buildConfiguration3 = build(3, "cycle-dependency-3", buildConfigurationSet);

        buildConfiguration1.addDependency(buildConfiguration2);
        buildConfiguration2.addDependency(buildConfiguration3);
        buildConfiguration3.addDependency(buildConfiguration1);

        return buildConfiguration1;
    }

    public BuildConfiguration buildConfigurationWithDependencies(BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration1 = build(1, "with-dependencies-1", buildConfigurationSet);
        BuildConfiguration buildConfiguration2 = build(2, "with-dependencies-2", buildConfigurationSet);
        BuildConfiguration buildConfiguration3 = build(3, "with-dependencies-3", buildConfigurationSet);
        BuildConfiguration buildConfiguration4 = build(4, "with-dependencies-4", buildConfigurationSet);
        BuildConfiguration buildConfiguration5 = build(5, "with-dependencies-5", buildConfigurationSet);

        buildConfiguration1.addDependency(buildConfiguration2);
        buildConfiguration1.addDependency(buildConfiguration3);
        buildConfiguration2.addDependency(buildConfiguration3);
        buildConfiguration2.addDependency(buildConfiguration4);
        buildConfiguration3.addDependency(buildConfiguration5);
        buildConfiguration4.addDependency(buildConfiguration5);

        return buildConfiguration1;
    }

    public BuildConfiguration buildConfigurationWithDependenciesThatFail(BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration1 = build(1, "with-dependencies-1", buildConfigurationSet);
        BuildConfiguration buildConfiguration2 = buildFailingConfiguration(2, "with-dependencies-2", buildConfigurationSet);

        buildConfiguration1.addDependency(buildConfiguration2);
        return buildConfiguration1;
    }

    public BuildConfiguration build(int id, String name) {
        return build(id, name, null);
    }

    public BuildConfiguration build(int id, String name, BuildConfigurationSet buildConfigurationSet) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(id);
        buildConfiguration.setDescription(PASS);
        buildConfiguration.setName(id + "");
        buildConfiguration.setEnvironment(javaEnvironment);
        buildConfiguration.setProject(project);
        project.addBuildConfiguration(buildConfiguration);
        if (buildConfigurationSet != null) {
            buildConfigurationSet.addBuildConfiguration(buildConfiguration);
        }
        return buildConfiguration;
    }

    public BuildConfiguration buildFailingConfiguration(int id, String name, BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration =  build(id, name, buildConfigurationSet);
        buildConfiguration.setDescription(FAIL);
        return buildConfiguration;
    }

    public BuildConfigurationSet buildConfigurationSet(Integer configurationSetId) {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-build-configuration");
        buildConfigurationSet.setId(configurationSetId);
        buildConfigurationWithDependencies(buildConfigurationSet);

        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetWithFailedDependencies(Integer configurationSetId){
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-build-configuration-failed-deps");
        buildConfigurationSet.setId(configurationSetId);
        buildConfigurationWithDependenciesThatFail(buildConfigurationSet);

        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetWithCycleDependency() {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-cycle");
        buildConfigurationWithCycleDependency(buildConfigurationSet);

        return buildConfigurationSet;
    }
}
