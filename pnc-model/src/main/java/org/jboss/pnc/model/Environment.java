package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * The Class Environment.
 */
@Entity
public class Environment implements Serializable {

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
}
