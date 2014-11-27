package org.jboss.pnc.model;

public class Environment {

    BuildType buildType;
    OperationalSystem operationalSystem;

    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setOperationalSystem(OperationalSystem operationalSystem) {
        this.operationalSystem = operationalSystem;
    }
}
