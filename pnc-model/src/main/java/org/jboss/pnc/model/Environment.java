package org.jboss.pnc.model;

import javax.persistence.*;

import java.io.Serializable;

/**
 * The Class Environment.
 */
@Entity
public class Environment implements Serializable {

    public static final String DEFAULT_SORTING_FIELD = "id";

    private static final long serialVersionUID = 8213767399060607637L;

    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    private BuildType buildType;

    @Enumerated(EnumType.STRING)
    private OperationalSystem operationalSystem;

    /**
     * Instantiates a new environment.
     */
    public Environment() {
    }

    /**
     * Instantiates a new environment.
     *
     * @param buildType the build type
     * @param operationalSystem the operational system
     */
    public Environment(BuildType buildType, OperationalSystem operationalSystem) {
        this.buildType = buildType;
        this.operationalSystem = operationalSystem;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param id the new id
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the operational system.
     *
     * @return the operational system
     */
    public OperationalSystem getOperationalSystem() {
        return operationalSystem;
    }

    /**
     * Sets the builds the type.
     *
     * @param buildType the new builds the type
     */
    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    /**
     * Gets the builds the type.
     *
     * @return the builds the type
     */
    public BuildType getBuildType() {
        return buildType;
    }

    /**
     * Sets the operational system.
     *
     * @param operationalSystem the new operational system
     */
    public void setOperationalSystem(OperationalSystem operationalSystem) {
        this.operationalSystem = operationalSystem;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Environment [operationalSystem=" + operationalSystem + ", buildType=" + buildType + "]";
    }

    public static class Builder {

        private BuildType buildType = BuildType.JAVA;
        private OperationalSystem operationalSystem = OperationalSystem.LINUX;
        private Integer id;

        private Builder() {

        }

        public static Builder defaultEnvironment() {
            return new Builder();
        }

        public static Builder emptyEnvironment() {
            return new Builder().id(null).buildType(null).operationalSystem(null);
        }

        public Environment build() {
            Environment environment = new Environment();
            environment.setId(id);
            environment.setBuildType(buildType);
            environment.setOperationalSystem(operationalSystem);
            return environment;
        }

        public OperationalSystem getOperationalSystem() {
            return operationalSystem;
        }

        public Builder buildType(BuildType buildType) {
            this.buildType = buildType;
            return this;
        }

        public Builder id(Integer id) {
            this.id = id;
            return  this;
        }

        private Builder operationalSystem(OperationalSystem operationalSystem) {
            this.operationalSystem = operationalSystem;
            return this;
        }
    }
}
