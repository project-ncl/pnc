package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.OperationalSystem;

public class EnvironmentBuilder {

    private BuildType buildType = BuildType.JAVA;
    private OperationalSystem operationalSystem = OperationalSystem.LINUX;
    private Integer id;

    private EnvironmentBuilder() {

    }

    public static EnvironmentBuilder defaultEnvironment() {
        return new EnvironmentBuilder();
    }

    public static EnvironmentBuilder emptyEnvironment() {
        return new EnvironmentBuilder().id(null).buildTool(null).operationalSystem(null);
    }

    public Environment build() {
        Environment environment = new Environment();
        environment.setBuildType(buildType);
        environment.setOperationalSystem(operationalSystem);
        return environment;
    }

    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public EnvironmentBuilder buildTool(BuildType buildType) {
        this.buildType = buildType;
        return this;
    }

    public EnvironmentBuilder id(Integer id) {
        this.id = id;
        return  this;
    }

    private EnvironmentBuilder operationalSystem(OperationalSystem operationalSystem) {
        this.operationalSystem = operationalSystem;
        return this;
    }
}
