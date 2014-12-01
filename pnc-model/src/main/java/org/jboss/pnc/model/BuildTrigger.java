package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The Class BuildTrigger defines the precedences between different ProjectBuildConfiguration, when one
 * ProjectBuildConfiguration need to trigger the build of a dependant ProjectBuildConfiguration
 *
 * @author avibelli
 */
@Entity
@Table(name = "build_trigger")
@NamedQuery(name = "BuildTrigger.findAll", query = "SELECT b FROM BuildTrigger b")
public class BuildTrigger implements Serializable {

    private static final long serialVersionUID = 2473896576726273092L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "build_configuration_id")
    private ProjectBuildConfiguration buildConfiguration;

    @ManyToOne
    @JoinColumn(name = "triggered_build_configuration_id")
    private ProjectBuildConfiguration triggeredBuildConfiguration;

    /**
     * Instantiates a new builds the trigger.
     */
    public BuildTrigger() {
    }

    /**
     * Instantiates a new builds the trigger.
     *
     * @param buildConfiguration the build configuration
     * @param triggeredBuildConfiguration the triggered build configuration
     */
    public BuildTrigger(ProjectBuildConfiguration buildConfiguration, ProjectBuildConfiguration triggeredBuildConfiguration) {

        this.buildConfiguration = buildConfiguration;
        this.triggeredBuildConfiguration = triggeredBuildConfiguration;
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
     * Gets the builds the configuration.
     *
     * @return the builds the configuration
     */
    public ProjectBuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    /**
     * Sets the builds the configuration.
     *
     * @param buildConfiguration the new builds the configuration
     */
    public void setBuildConfiguration(ProjectBuildConfiguration buildConfiguration) {
        this.buildConfiguration = buildConfiguration;
    }

    /**
     * Gets the triggered build configuration.
     *
     * @return the triggered build configuration
     */
    public ProjectBuildConfiguration getTriggeredBuildConfiguration() {
        return triggeredBuildConfiguration;
    }

    /**
     * Sets the triggered build configuration.
     *
     * @param triggeredBuildConfiguration the new triggered build configuration
     */
    public void setTriggeredBuildConfiguration(ProjectBuildConfiguration triggeredBuildConfiguration) {
        this.triggeredBuildConfiguration = triggeredBuildConfiguration;
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
        result = prime * result + ((buildConfiguration == null) ? 0 : buildConfiguration.hashCode());
        result = prime * result + ((triggeredBuildConfiguration == null) ? 0 : triggeredBuildConfiguration.hashCode());
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
        BuildTrigger other = (BuildTrigger) obj;
        if (buildConfiguration == null) {
            if (other.buildConfiguration != null)
                return false;
        } else if (!buildConfiguration.equals(other.buildConfiguration))
            return false;
        if (triggeredBuildConfiguration == null) {
            if (other.triggeredBuildConfiguration != null)
                return false;
        } else if (!triggeredBuildConfiguration.equals(other.triggeredBuildConfiguration))
            return false;
        return true;
    }

}
