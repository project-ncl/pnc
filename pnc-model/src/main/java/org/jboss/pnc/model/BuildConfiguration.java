package org.jboss.pnc.model;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * The Class BuildConfiguration cointains the informations needed to trigger the build of a project, i.e. the sources and
 * patches URLs, the build script, the environment needed to run, the project configurations that need to be triggered after a
 * successful build. It contains also creation and last modification time for historical purposes
 * <p>
 * (project + name) should be unique
 *
 * @author avibelli
 */
@Entity
@Audited
public class BuildConfiguration implements GenericEntity<Integer>, Cloneable {

    private static final long serialVersionUID = -5890729679489304114L;

    public static final String DEFAULT_SORTING_FIELD = "name";

    @Id
    @SequenceGenerator(name="build_configuration_id_seq", sequenceName="build_configuration_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="build_configuration_id_seq")
    private Integer id;

    @NotNull
    private String name;

    private String buildScript;

    private String scmRepoURL;

    private String scmRevision;

    private String description;

    private String patchesUrl;

    @NotAudited
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private ProductVersion productVersion;

    @Audited( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    private Project project;

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    private Environment environment;

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private BuildConfiguration parent;

    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "latestBuildConfiguration")
    private Set<BuildRecord> buildRecords;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToMany(mappedBy = "buildConfigurations")
    private Set<BuildConfigurationSet> buildConfigurationSets;

    @NotNull
    private Timestamp creationTime;

    @NotNull
    @Version
    private Timestamp lastModificationTime;

    @OneToMany(mappedBy = "parent", cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH })
    private Set<BuildConfiguration> dependencies;

    // TODO: What data format does Aprox need?
    // [jdcasey] I'm not sure what this is supposed to do in the repository manager...so hard to say what format is required.
    // @Column(name = "repositories")
    private String repositories;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfiguration() {
        dependencies = new HashSet<>();
        buildRecords = new HashSet<>();
        buildConfigurationSets = new HashSet<BuildConfigurationSet>();
        creationTime = Timestamp.from(Instant.now());
    }

    @PreRemove
    private void removeConfigurationFromSets() {
        for (BuildConfigurationSet bcs : buildConfigurationSets) {
            bcs.getBuildConfigurations().remove(this);
        }
    }

    /**
     * @return the id
     */
    @Override
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
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
     * @return the scmRepoURL
     */
    public String getScmRepoURL() {
        return scmRepoURL;
    }

    /**
     * @param scmRepoURL the scmRepoURL to set
     */
    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    /**
     * @return the scmRevision
     */
    public String getScmRevision() {
        return scmRevision;
    }

    /**
     * @param scmRevision the scmRevision to set
     */
    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
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
     * @return the buildConfigurationSets which contain this build configuration
     */
    public Set<BuildConfigurationSet> getBuildConfigurationSets() {
        return buildConfigurationSets;
    }

    /**
     * @param buildConfigurationSets the list of buildConfigurationSets
     */
    public void setBuildConfigurationSets(Set<BuildConfigurationSet> buildConfigurationSets) {
        if (buildConfigurationSets == null ){
            this.buildConfigurationSets = new HashSet<BuildConfigurationSet>();
        }
        this.buildConfigurationSets = buildConfigurationSets;
    }

    /**
     * @param buildConfigurationSet add to the list of buildConfigurationSets
     */
    public void addBuildConfigurationSet(BuildConfigurationSet buildConfigurationSet) {
        if (!this.buildConfigurationSets.contains(buildConfigurationSet)) {
            this.buildConfigurationSets.add(buildConfigurationSet);
        }
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

    public Set<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    public BuildConfiguration addBuildRecord(BuildRecord buildRecord) {
        this.buildRecords.add(buildRecord);
        return this;
    }

    @Override
    public String toString() {
        return "BuildConfiguration [project=" + project + ", name=" + name + "]";
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

    /**
     * Creates a shallow based clone and overrides {@link #creationTime}, {@link #name} and erases {@link #id}.
     *
     * @return This objects clone.
     */
    @Override
    public BuildConfiguration clone() {
        try {
            BuildConfiguration clone = (BuildConfiguration) super.clone();
            clone.name = "_" + name;
            clone.creationTime = Timestamp.from(Instant.now());
            clone.id = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Cloning error" + e);
        }
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String buildScript;

        private String scmRepoURL;

        private String scmRevision;

        private String patchesUrl;

        private String description;

        private Project project;

        private Environment environment;

        private BuildConfiguration parent;

        private Set<BuildConfiguration> dependencies;

        private Set<BuildConfigurationSet> buildConfigurationSets;

        private Timestamp creationTime;

        private Timestamp lastModificationTime;

        private String repositories;

        private Builder() {
            dependencies = new HashSet<>();
            buildConfigurationSets = new HashSet<BuildConfigurationSet>();
            creationTime = Timestamp.from(Instant.now());
            lastModificationTime = Timestamp.from(Instant.now());
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildConfiguration build() {
            BuildConfiguration buildConfiguration = new BuildConfiguration();
            buildConfiguration.setId(id);
            buildConfiguration.setName(name);
            buildConfiguration.setBuildScript(buildScript);
            buildConfiguration.setScmRepoURL(scmRepoURL);
            buildConfiguration.setScmRevision(scmRevision);
            buildConfiguration.setDescription(description);

            // Set the bi-directional mapping
            if (project != null) {
                project.addBuildConfiguration(buildConfiguration);
            }
            buildConfiguration.setProject(project);

            buildConfiguration.setEnvironment(environment);
            buildConfiguration.setCreationTime(creationTime);
            buildConfiguration.setLastModificationTime(lastModificationTime);
            buildConfiguration.setRepositories(repositories);
            buildConfiguration.setBuildConfigurationSets(buildConfigurationSets);
            for (BuildConfigurationSet buildConfigurationSet : buildConfigurationSets)
            {
                buildConfigurationSet.addBuildConfiguration(buildConfiguration);
            }

            // Set the bi-directional mapping
            for (BuildConfiguration dependency : dependencies) {
                dependency.setParent(buildConfiguration);
            }
            buildConfiguration.setDependencies(dependencies);

            return buildConfiguration;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder buildScript(String buildScript) {
            this.buildScript = buildScript;
            return this;
        }

        public Builder scmRepoURL(String scmRepoURL) {
            this.scmRepoURL = scmRepoURL;
            return this;
        }

        public Builder scmRevision(String scmRevision) {
            this.scmRevision = scmRevision;
            return this;
        }

        public Builder patchesUrl(String patchesUrl) {
            this.patchesUrl = patchesUrl;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Builder environment(Environment environment) {
            this.environment = environment;
            return this;
        }

        public Builder dependency(BuildConfiguration dependency) {
            this.dependencies.add(dependency);
            return this;
        }

        public Builder dependencies(Set<BuildConfiguration> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public Builder buildConfigurationSet(BuildConfigurationSet buildConfigurationSet) {
            this.buildConfigurationSets.add(buildConfigurationSet);
            return this;
        }

        public Builder buildConfigurationSets(Set<BuildConfigurationSet> buildConfigurationSets) {
            this.buildConfigurationSets = buildConfigurationSets;
            return this;
        }

        public Builder creationTime(Timestamp creationTime) {
            if (creationTime != null) {
                this.creationTime = creationTime;
            }
            return this;
        }

        /**
         * @param lastModificationTime Sets last update time and ignores Null values (since they may affect the entity consistency).
         */
        public Builder lastModificationTime(Timestamp lastModificationTime) {
            if (lastModificationTime != null) {
                this.lastModificationTime = lastModificationTime;
            }
            return this;
        }

        public Builder repositories(String repositories) {
            this.repositories = repositories;
            return this;
        }

    }

}
