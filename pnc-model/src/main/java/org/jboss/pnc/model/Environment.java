package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;

// TODO: Auto-generated Javadoc
/**
 * The Class Environment.
 */
@Entity
@NamedQuery(name = "Environment.findAll", query = "SELECT e FROM Environment e")
public class Environment implements Serializable {

    private static final long serialVersionUID = 8213767399060607637L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "build_type_id")
    private BuildType buildType;

    @ManyToOne
    @JoinColumn(name = "operational_system_id")
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((buildType == null) ? 0 : buildType.hashCode());
        result = prime * result + ((operationalSystem == null) ? 0 : operationalSystem.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Environment other = (Environment) obj;
        if (buildType != other.buildType)
            return false;
        if (operationalSystem != other.operationalSystem)
            return false;
        return true;
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
