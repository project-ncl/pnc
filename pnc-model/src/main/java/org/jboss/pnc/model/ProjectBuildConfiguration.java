package org.jboss.pnc.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.ForeignKey;

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
@Table(name = "project_build_configuration")
public class ProjectBuildConfiguration implements Serializable {

    private static final long serialVersionUID = -5890729679489304114L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String identifier;

    @Column(nullable = false, name = "build_script")
    private String buildScript;

    @Column(nullable = false, name = "scm_url")
    private String scmUrl;

    @Column(name = "patches_url")
    private String patchesUrl;

    @JoinColumn(name = "product_version_id")
    @ManyToOne(cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_project_build_configuration_product_version")
    private ProductVersion productVersion;

    @ManyToOne(cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_project_build_configuration_project")
    private Project project;

    @ManyToOne(cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_project_build_configuration_environment")
    private Environment environment;

    @ManyToOne(cascade = CascadeType.ALL)
    @ForeignKey(name = "fk_project_build_configuration_parent")
    private ProjectBuildConfiguration parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private Set<ProjectBuildConfiguration> dependency;

    @Column(name = "creation_time")
    private Timestamp creationTime;

    @Column(name = "last_modification_time")
    @Version
    private Timestamp lastModificationTime;

    // TODO: What data format does Aprox need?
    private String repository;

    /**
     * Instantiates a new project build configuration.
     */
    public ProjectBuildConfiguration() {
        dependency = new HashSet<>();
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
     * @return the parent
     */
    public ProjectBuildConfiguration getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(ProjectBuildConfiguration parent) {
        this.parent = parent;
    }

    /**
     * @return the dependency
     */
    public Set<ProjectBuildConfiguration> getDependency() {
        return dependency;
    }

    /**
     * @param dependency the dependency to set
     */
    public void setDependency(Set<ProjectBuildConfiguration> dependency) {
        this.dependency = dependency;
    }

    /**
     * @return the repository
     */
    public String getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(String repository) {
        this.repository = repository;
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

    public ProjectBuildConfiguration addDependency(ProjectBuildConfiguration configuration) {
        configuration.setParent(this);
        dependency.add(configuration);
        return this;
    }

    public ProjectBuildConfiguration removeDependency(ProjectBuildConfiguration configuration) {
        configuration.setParent(null);
        dependency.remove(configuration);
        return this;
    }

    @Override
    public String toString() {
        return "ProjectBuildConfiguration [project=" + project + ", identifier=" + identifier + "]";
    }

}
