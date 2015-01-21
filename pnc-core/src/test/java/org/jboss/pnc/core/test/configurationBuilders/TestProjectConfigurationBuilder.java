package org.jboss.pnc.core.test.configurationBuilders;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.Project;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.builder.EnvironmentBuilder;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class TestProjectConfigurationBuilder {

    Environment javaEnvironment = EnvironmentBuilder.defaultEnvironment().build();

    public BuildConfiguration buildConfigurationWhichDependsOnItself() {
        BuildConfiguration buildConfiguration = build(1, "depends-on-itself");
        buildConfiguration.addDependency(buildConfiguration);
        return buildConfiguration;
    }

    public BuildConfiguration buildConfigurationWithCycleDependency() {
        BuildConfiguration buildConfiguration1 = build(1, "cycle-dependency-1");
        BuildConfiguration buildConfiguration2 = build(2, "cycle-dependency-2");
        BuildConfiguration buildConfiguration3 = build(3, "cycle-dependency-3");

        buildConfiguration1.addDependency(buildConfiguration2);
        buildConfiguration2.addDependency(buildConfiguration3);
        buildConfiguration3.addDependency(buildConfiguration1);

        return buildConfiguration1;
    }

    public BuildConfiguration buildConfigurationWithDependencies() {
        BuildConfiguration buildConfiguration1 = build(1, "with-dependencies-1");
        BuildConfiguration buildConfiguration2 = build(2, "with-dependencies-2");
        BuildConfiguration buildConfiguration3 = build(3, "with-dependencies-3");
        BuildConfiguration buildConfiguration4 = build(4, "with-dependencies-4");
        BuildConfiguration buildConfiguration5 = build(5, "with-dependencies-5");

        buildConfiguration1.addDependency(buildConfiguration2);
        buildConfiguration2.addDependency(buildConfiguration3);
        buildConfiguration2.addDependency(buildConfiguration4);
        buildConfiguration4.addDependency(buildConfiguration5);

        return buildConfiguration1;
    }

    public BuildConfiguration build(int id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        BuildConfiguration buildConfiguration = new BuildConfiguration();
        buildConfiguration.setId(id);
        buildConfiguration.setName(id + "");
        buildConfiguration.setEnvironment(javaEnvironment);
        buildConfiguration.setProject(project);
        project.addBuildConfiguration(buildConfiguration);
        return buildConfiguration;
    }

}
