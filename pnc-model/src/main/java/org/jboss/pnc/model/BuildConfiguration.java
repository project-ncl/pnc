package org.jboss.pnc.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class BuildConfiguration cointains the informations needed to trigger the build of a project, i.e. the sources and
 * patches URLs, the build script, the environment needed to run, the project configurations that need to be triggered after a
 * successful build. It contains also creation and last modification time for historical purposes
 * 
 * (project + name) should be unique
 *
 * @author avibelli
 */
@Entity
public class BuildConfiguration implements Serializable {

    private static final long serialVersionUID = -5890729679489304114L;

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String buildScript;

    private String scmUrl;

    private String description;

	private String patchesUrl;

    @ManyToOne(cascade = CascadeType.ALL)
    private ProductVersion productVersion;

    @ManyToOne(cascade = CascadeType.ALL)
    private Project project;

    @ManyToOne(cascade = CascadeType.ALL)
    private Environment environment;

    @ManyToOne(cascade = CascadeType.ALL)
    private BuildConfiguration parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<BuildConfiguration> dependencies;

    private Timestamp creationTime;

    @Version
    private Timestamp lastModificationTime;

    // TODO: What data format does Aprox need?
    // @Column(name = "repositories")
    private String repositories;
    private String scmBranch;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfiguration() {
        dependencies = new HashSet<>();
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
    public String getName() {
        return name;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setName(String name) {
        this.name = name;
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

    public String getScmBranch() {
        return scmBranch;
    }

    public void setScmBranch(String scmBranch) {
        this.scmBranch = scmBranch;
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

    public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
     * @return the parent
     */
    public BuildConfiguration getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(BuildConfiguration parent) {
        this.parent = parent;
    }

    /**
     * @return the dependencies
     */
    public Set<BuildConfiguration> getDependencies() {
        return dependencies;
    }

    /**
     * @param dependencies the dependencies to set
     */
    public void setDependencies(Set<BuildConfiguration> dependencies) {
        this.dependencies = dependencies;
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

    /**
     * @return the repositories
     */
    public String getRepositories() {
        return repositories;
    }

    /**
     * @param repositories the repositories to set
     */
    public void setRepositories(String repositories) {
        this.repositories = repositories;
    }

    public BuildConfiguration addDependency(BuildConfiguration configuration) {
        configuration.setParent(this);
        dependencies.add(configuration);
        return this;
    }

    public BuildConfiguration removeDependency(BuildConfiguration configuration) {
        configuration.setParent(null);
        dependencies.remove(configuration);
        return this;
    }

    @Override
    public String toString() {
        return "BuildConfiguration [project=" + project + ", identifier=" + name + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BuildConfiguration that = (BuildConfiguration) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

}
