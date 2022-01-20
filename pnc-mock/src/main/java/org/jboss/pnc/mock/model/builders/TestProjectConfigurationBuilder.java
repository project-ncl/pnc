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
package org.jboss.pnc.mock.model.builders;

import org.jboss.pnc.mock.datastore.DatastoreMock;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.RepositoryConfiguration;

import javax.inject.Inject;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectConfigurationBuilder {

    public static final String FAIL = "mvn clean install -Dmock.config=Fail";
    public static final String FAIL_WITH_DELAY = "mvn clean install -Dmock.config=FailWithDelay";
    public static final String PASS = "mvn clean install -Dmock.config=Pass";
    public static final String CANCEL = "mvn clean install -Dmock.config=Cancel";

    BuildEnvironment javaBuildEnvironment = BuildEnvironment.Builder.newBuilder().build();

    // TODO remove datastore dependency
    @Inject
    DatastoreMock datastore;

    @Deprecated // CDI workaround
    public TestProjectConfigurationBuilder() {
    }

    public TestProjectConfigurationBuilder(DatastoreMock datastore) {
        this.datastore = datastore;
    }

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
        BuildConfiguration buildConfiguration2 = buildFailingConfiguration(
                2,
                "with-dependencies-2",
                buildConfigurationSet);

        buildConfiguration1.addDependency(buildConfiguration2);
        return buildConfiguration1;
    }

    public BuildConfiguration buildConfigurationWithTransitiveDependenciesThatFail(
            BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration1 = build(1, "with-dependencies-1", buildConfigurationSet);
        BuildConfiguration buildConfiguration2 = build(2, "with-dependencies-2", buildConfigurationSet);
        BuildConfiguration buildConfiguration3 = buildFailingConfiguration(
                3,
                "with-dependencies-3",
                buildConfigurationSet);

        buildConfiguration1.addDependency(buildConfiguration2);
        buildConfiguration2.addDependency(buildConfiguration3);
        return buildConfiguration1;
    }

    public BuildConfiguration buildConfigurationForCancelling(BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration1 = build(1, "with-dependency-1-on-2", buildConfigurationSet);
        BuildConfiguration buildConfiguration2 = build(2, "with-dependency-2-on-3", buildConfigurationSet);
        BuildConfiguration buildConfiguration3 = build(3, "not-dependent", buildConfigurationSet);
        // CANCEL script means, that the build waits for 1 sec and then completes itself, gives time to cancel
        buildConfiguration2.setBuildScript(CANCEL);

        buildConfiguration1.addDependency(buildConfiguration2);
        buildConfiguration2.addDependency(buildConfiguration3);
        return buildConfiguration1;
    }

    public BuildConfiguration build(int id, String name) {
        return build(id, name, null);
    }

    public BuildConfiguration build(int id, String name, BuildConfigurationSet buildConfigurationSet) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);

        RepositoryConfiguration repositoryConfiguration = RepositoryConfiguration.Builder.newBuilder()
                .id(id)
                .internalUrl("github.com/" + name)
                .build();

        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(id);
        buildConfiguration.setBuildScript(PASS);
        buildConfiguration.setName(id + "");
        buildConfiguration.setRepositoryConfiguration(repositoryConfiguration);
        buildConfiguration.setBuildType(BuildType.MVN);
        buildConfiguration.setBuildEnvironment(javaBuildEnvironment);
        buildConfiguration.setProject(project);
        buildConfiguration.setProject(project);
        buildConfiguration.setArchived(false);
        project.addBuildConfiguration(buildConfiguration);

        if (buildConfigurationSet != null) {
            buildConfigurationSet.addBuildConfiguration(buildConfiguration);
        }
        datastore.save(buildConfiguration);

        return buildConfiguration;
    }

    public BuildConfiguration buildFailingConfiguration(
            int id,
            String name,
            BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration = build(id, name, buildConfigurationSet);
        buildConfiguration.setBuildScript(FAIL);
        return buildConfiguration;
    }

    public BuildConfiguration buildFailingWithDelayConfiguration(
            int id,
            String name,
            BuildConfigurationSet buildConfigurationSet) {
        BuildConfiguration buildConfiguration = build(id, name, buildConfigurationSet);
        buildConfiguration.setBuildScript(FAIL_WITH_DELAY);
        return buildConfiguration;
    }

    public BuildConfiguration buildConfigurationToCancel(int id, String name) {
        BuildConfiguration buildConfiguration = build(id, name);
        buildConfiguration.setBuildScript(CANCEL);
        return buildConfiguration;
    }

    public BuildConfigurationSet buildConfigurationSet(Integer configurationSetId) {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-build-configuration");
        buildConfigurationSet.setId(configurationSetId);
        buildConfigurationWithDependencies(buildConfigurationSet);

        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetForCancel(Integer configurationSetId) {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-build-cancel-configuration");
        buildConfigurationSet.setId(configurationSetId);
        buildConfigurationForCancelling(buildConfigurationSet);
        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetWithFailedDependencies(Integer configurationSetId) {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-build-configuration-failed-deps");
        buildConfigurationSet.setId(configurationSetId);
        buildConfigurationWithDependenciesThatFail(buildConfigurationSet);

        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetWithFailedDependenciesAndDelay(Integer configurationSetId) {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-build-configuration-failed-deps");
        buildConfigurationSet.setId(configurationSetId);
        buildConfigurationWithTransitiveDependenciesThatFail(buildConfigurationSet);

        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetWithCycleDependency() {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationSet.setName("test-cycle");
        buildConfigurationWithCycleDependency(buildConfigurationSet);

        return buildConfigurationSet;
    }
}
