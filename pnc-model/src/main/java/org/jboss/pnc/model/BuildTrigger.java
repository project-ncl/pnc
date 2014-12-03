package org.jboss.pnc.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The Class BuildTrigger defines the precedences between different ProjectBuildConfiguration, when one
 * ProjectBuildConfiguration need to trigger the build of a dependant ProjectBuildConfiguration
 *
 * @author avibelli
 */
@Entity
public class BuildTrigger implements Serializable {

    private static final long serialVersionUID = 2473896576726273092L;

    @Id
    @GeneratedValue
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    private ProjectBuildConfiguration buildConfiguration;

    @ManyToOne(cascade = CascadeType.ALL)
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

}
