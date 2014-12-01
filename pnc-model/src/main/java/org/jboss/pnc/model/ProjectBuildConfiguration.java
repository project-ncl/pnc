package org.jboss.pnc.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * The Class ProjectBuildConfiguration cointains the informations needed to trigger the build of a project, i.e. the sources and
 * patches URLs, the build script, the environment needed to run, the project configurations that need to be triggered after a
 * successful build. It contains also creation and last modification time for historical purposes
 * 
 * (project + name) should be unique
 *
 * @author avibelli
 */
@Entity
@Table(name = "build_configuration")
@NamedQuery(name = "BuildConfiguration.findAll", query = "SELECT b FROM BuildConfiguration b")
public class ProjectBuildConfiguration implements Serializable {

    private static final long serialVersionUID = -5890729679489304114L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String identifier;

    @Column(name = "build_script")
    private String buildScript;

    @Column(name = "scm_url")
    private String scmUrl;

    @Column(name = "patches_url")
    private String patchesUrl;

    @ManyToOne
    private Project project;

    @ManyToOne
    @JoinColumn(name = "environment_id")
    private Environment environment;

    @OneToMany(mappedBy = "builds_to_trigger")
    private Set<BuildTrigger> buildsToTrigger;

    @OneToMany(mappedBy = "triggered_by_builds")
    private Set<BuildTrigger> triggeredByBuilds;

    @Column(name = "created")
    private Timestamp creationTime;

    @Column(name = "last_updated")
    private Timestamp lastModificationTime;

    /**
     * Instantiates a new project build configuration.
     */
    public ProjectBuildConfiguration() {
        buildsToTrigger = new HashSet<BuildTrigger>();
        triggeredByBuilds = new HashSet<BuildTrigger>();
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return the buildScript
     */
    public String getBuildScript() {
        return buildScript;
    }

    /**
     * @param buildScript the buildScript to set
     */
    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    /**
     * @return the scmUrl
     */
    public String getScmUrl() {
        return scmUrl;
    }

    /**
     * @param scmUrl the scmUrl to set
     */
    public void setScmUrl(String scmUrl) {
        this.scmUrl = scmUrl;
    }

    /**
     * @return the patchesUrl
     */
    public String getPatchesUrl() {
        return patchesUrl;
    }

    /**
     * @param patchesUrl the patchesUrl to set
     */
    public void setPatchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
    }

    /**
     * @return the project
     */
    public Project getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * @return the environment
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * @param environment the environment to set
     */
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @return the buildsToTrigger
     */
    public Set<BuildTrigger> getBuildsToTrigger() {
        return buildsToTrigger;
    }

    /**
     * @param buildsToTrigger the buildsToTrigger to set
     */
    public void setBuildsToTrigger(Set<BuildTrigger> buildsToTrigger) {
        this.buildsToTrigger = buildsToTrigger;
    }

    /**
     * @return the triggeredByBuilds
     */
    public Set<BuildTrigger> getTriggeredByBuilds() {
        return triggeredByBuilds;
    }

    /**
     * @param triggeredByBuilds the triggeredByBuilds to set
     */
    public void setTriggeredByBuilds(Set<BuildTrigger> triggeredByBuilds) {
        this.triggeredByBuilds = triggeredByBuilds;
    }

    /**
     * @return the creationTime
     */
    public Timestamp getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime the creationTime to set
     */
    public void setCreationTime(Timestamp creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the lastModificationTime
     */
    public Timestamp getLastModificationTime() {
        return lastModificationTime;
    }

    /**
     * @param lastModificationTime the lastModificationTime to set
     */
    public void setLastModificationTime(Timestamp lastModificationTime) {
        this.lastModificationTime = lastModificationTime;
    }

    public Set<ProjectBuildConfiguration> getDependencies() {
        if (!buildsToTrigger.isEmpty()) {
            Set<ProjectBuildConfiguration> dependencies = new HashSet<ProjectBuildConfiguration>();
            for (BuildTrigger buildTrigger : buildsToTrigger) {
                dependencies.add(buildTrigger.getBuildConfiguration());
            }
            return dependencies;
        }
        return null;
    }

    public Set<BuildTrigger> addDependency(ProjectBuildConfiguration configuration) {
        buildsToTrigger.add(new BuildTrigger(this, configuration));

        return buildsToTrigger;
    }

    public Set<BuildTrigger> removeDependency(ProjectBuildConfiguration configuration) {

        for (Iterator<BuildTrigger> iterator = buildsToTrigger.iterator(); iterator.hasNext();) {
            BuildTrigger buildTrigger = iterator.next();
            if (configuration.equals(buildTrigger.getTriggeredBuildConfiguration())) {
                iterator.remove();
            }
        }

        return buildsToTrigger;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((project == null) ? 0 : project.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ProjectBuildConfiguration other = (ProjectBuildConfiguration) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (project == null) {
            if (other.project != null)
                return false;
        } else if (!project.equals(other.project))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "ProjectBuildConfiguration [project=" + project + ", identifier=" + identifier + "]";
    }

}
