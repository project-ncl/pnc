package org.jboss.pnc.model;

import javax.persistence.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
public class ProjectBuildConfiguration implements Serializable {

    private static final long serialVersionUID = -5890729679489304114L;

    @Id
    @GeneratedValue
    private Integer id;

    private String identifier;

    private String buildScript;

    private String scmUrl;

    private String patchesUrl;

    @ManyToOne
    private ProductVersion productVersion;

    @ManyToOne
    private Project project;

    @ManyToOne
    private Environment environment;

    @OneToMany(mappedBy = "triggeredBuildConfiguration")
    private Set<BuildTrigger> buildsToTrigger;

    @OneToMany(mappedBy = "buildConfiguration")
    private Set<BuildTrigger> triggeredByBuilds;

    private Timestamp creationTime;

    @Version
    private Timestamp lastModificationTime;

    // TODO: What data format does Aprox need?
    // @Column(name = "repositories")
    private String repositories;

    /**
     * Instantiates a new project build configuration.
     */
    public ProjectBuildConfiguration() {
        buildsToTrigger = new HashSet<>();
        triggeredByBuilds = new HashSet<>();
        creationTime = Timestamp.from(Instant.now());
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
     * @return the productVersion
     */
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion the productVersion to set
     */
    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
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
            Set<ProjectBuildConfiguration> dependencies = new HashSet<>();
            for (BuildTrigger buildTrigger : buildsToTrigger) {
                dependencies.add(buildTrigger.getTriggeredBuildConfiguration());
            }
            return dependencies;
        }
        return Collections.emptySet();
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
    public String toString() {
        return "ProjectBuildConfiguration [project=" + project + ", identifier=" + identifier + "]";
    }

}
