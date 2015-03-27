package org.jboss.pnc.core.test.configurationBuilders;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.BuildConfiguration;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectConfigurationBuilder {

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
        buildConfiguration2.addDependency(buildConfiguration3);
        buildConfiguration2.addDependency(buildConfiguration4);
        buildConfiguration4.addDependency(buildConfiguration5);

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
        buildConfiguration.setName(id + "");
        buildConfiguration.setEnvironment(javaEnvironment);
        buildConfiguration.setProject(project);
        project.addBuildConfiguration(buildConfiguration);
        if (buildConfigurationSet != null) {
            buildConfigurationSet.addBuildConfiguration(buildConfiguration);
        }
        return buildConfiguration;
    }

    public BuildConfigurationSet buildConfigurationSet() {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationWithDependencies(buildConfigurationSet);

        return buildConfigurationSet;
    }

    public BuildConfigurationSet buildConfigurationSetWithCycleDependency() {
        BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
        buildConfigurationWithCycleDependency(buildConfigurationSet);

        return buildConfigurationSet;
    }
}
