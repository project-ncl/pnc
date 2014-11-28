package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.OperationalSystem;

public class EnvironmentBuilder {

    BuildType buildTool = BuildType.JAVA;
    OperationalSystem operationalSystem = OperationalSystem.LINUX;

    private EnvironmentBuilder() {

    }

    public static EnvironmentBuilder defaultEnvironment() {
        return new EnvironmentBuilder();
    }

    public Environment build() {
        Environment environment = new Environment();
        environment.setBuildType(buildTool);
        environment.setOperationalSystem(operationalSystem);
        return environment;
    }

    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    public BuildType getBuildTool() {
        return buildTool;
    }

    public EnvironmentBuilder withDocker() {
        this.buildTool = BuildType.DOCKER;
        return this;
    }

    public EnvironmentBuilder withNative() {
        this.buildTool = BuildType.NATIVE;
        return this;
    }
}
