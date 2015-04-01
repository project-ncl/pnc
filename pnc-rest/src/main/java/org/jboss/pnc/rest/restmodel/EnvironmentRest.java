package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.common.Identifiable;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.model.Environment;
import org.jboss.pnc.model.OperationalSystem;

/**
 * // TODO: Document this
 *
 * @author slaskawiec
 * @since 4.0
 */
public class EnvironmentRest implements Identifiable<Integer> {

    private Integer id;

    private BuildType buildType;

    private OperationalSystem operationalSystem;

    public EnvironmentRest() {
    }

    public EnvironmentRest(Environment environment) {
        this.id = environment.getId();
        this.buildType = environment.getBuildType();
        this.operationalSystem = environment.getOperationalSystem();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    public void setOperationalSystem(OperationalSystem operationalSystem) {
        this.operationalSystem = operationalSystem;
    }

    public Environment toEnvironment() {
        Environment environment = new Environment();
        environment.setId(id);
        environment.setBuildType(buildType);
        environment.setOperationalSystem(operationalSystem);
        return environment;
    }
}
