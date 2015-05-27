/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
 * the build script, the environment needed to run, the project configurations that need to be triggered after a
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

    @NotAudited
    @ManyToMany
    @JoinTable(
            name="build_configuration_product_versions_map",
            joinColumns={@JoinColumn(name="build_configuration_id", referencedColumnName="id")},
            inverseJoinColumns={@JoinColumn(name="product_version_id", referencedColumnName="id")})
    private Set<ProductVersion> productVersions;

    @Audited( targetAuditMode = RelationTargetAuditMode.NOT_AUDITED )
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    private Project project;

    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    private Environment environment;

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

    /**
     * Represents the status of the most recent execution of the current configuration.
     */
    @Enumerated(value = EnumType.STRING)
    private BuildStatus buildStatus;

    /**
     * The set of build configs upon which this build depends.  The build configs contained
     * in dependencies should normally be completed before this build config is executed.
     * Similar to Maven dependencies.
     */
    @NotAudited
    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(
            name="build_configuration_dep_map",
            joinColumns={@JoinColumn(name="dependency_id", referencedColumnName="id")},
            inverseJoinColumns={@JoinColumn(name="dependant_id", referencedColumnName="id")})
    private Set<BuildConfiguration> dependencies;

    /**
     * The set of build configs which depend upon this config.  These builds must normally
     * be built after this build is completed.  This is the reverse relationship as Maven
     * dependencies.
     */
    @NotAudited
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildConfiguration> dependants;

    // TODO: What data format does Aprox need?
    // [jdcasey] I'm not sure what this is supposed to do in the repository manager...so hard to say what format is required.
    // @Column(name = "repositories")
    private String repositories;

    /**
     * This contains the saved information of this specific revision of this entity 
     * as set by Hibernate envers.
     * It is only used in certain situations such as during a build execution.
     */
    @Transient
    private BuildConfigurationAudited buildConfigurationAudited;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfiguration() {
        dependencies = new HashSet<BuildConfiguration>();
        dependants = new HashSet<BuildConfiguration>();
        buildRecords = new HashSet<BuildRecord>();
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
     * @return the productVersions associated with this build config
     */
    public Set<ProductVersion> getProductVersions() {
        return productVersions;
    }

    /**
     * @param productVersions the set of productVersions associated with this build config
     */
    public void setProductVersions(Set<ProductVersion> productVersions) {
        this.productVersions = productVersions;
    }

    public boolean addProductVersion(ProductVersion productVersion) {
        return this.productVersions.add(productVersion);
    }

    public boolean removeProductVersion(ProductVersion productVersion) {
        return this.productVersions.remove(productVersion);
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
     * @return the set of build configs upon which this builds depends
     */
    public Set<BuildConfiguration> getDependencies() {
        return dependencies;
    }

    /**
     * @param the set of build configs upon which this build depends
     */
    public void setDependencies(Set<BuildConfiguration> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean addDependency(BuildConfiguration dependency) {
        boolean result = dependencies.add(dependency);
        if (!dependency.getDependants().contains(this)) {
            dependency.addDependant(this);
        }
        return result;
    }

    public boolean removeDependency(BuildConfiguration dependency) {
        boolean result = dependencies.remove(dependency);
        if (dependency.getDependants().contains(this)) {
            dependency.removeDependant(this);
        }
        return result;
    }

    /**
     * @return the set of build configs which depend on this build
     */
    public Set<BuildConfiguration> getDependants() {
        return dependants;
    }

    public void setDependants(Set<BuildConfiguration> dependants) {
        this.dependants = dependants;
    }

    private boolean addDependant(BuildConfiguration dependant) {
        boolean result = dependants.add(dependant);
        if (!dependant.getDependencies().contains(this)) {
            dependant.addDependency(this);
        }
        return result;
    }

    private boolean removeDependant(BuildConfiguration dependant) {
        boolean result = dependants.remove(dependant);
        if (dependant.getDependencies().contains(this)) {
            dependant.removeDependency(this);
        }
        return result;
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

    public BuildStatus getBuildStatus() {
        return buildStatus;
    }

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
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

    public BuildConfigurationAudited getBuildConfigurationAudited() {
        return buildConfigurationAudited;
    }

    public void setBuildConfigurationAudited(BuildConfigurationAudited buildConfigurationAudited) {
        this.buildConfigurationAudited = buildConfigurationAudited;
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

        private String description;

        private Project project;

        private Environment environment;

        private Set<ProductVersion> productVersions;

        private Set<BuildConfiguration> dependencies;

        private Set<BuildConfiguration> dependants;

        private Set<BuildConfigurationSet> buildConfigurationSets;

        private Timestamp creationTime;

        private Timestamp lastModificationTime;

        private BuildStatus buildStatus;

        private String repositories;

        private Builder() {
            dependencies = new HashSet<BuildConfiguration>();
            dependants = new HashSet<BuildConfiguration>();
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
            buildConfiguration.setBuildStatus(buildStatus);
            buildConfiguration.setRepositories(repositories);
            buildConfiguration.setBuildConfigurationSets(buildConfigurationSets);
            buildConfiguration.setProductVersions(productVersions);

            for (BuildConfigurationSet buildConfigurationSet : buildConfigurationSets)
            {
                buildConfigurationSet.addBuildConfiguration(buildConfiguration);
            }

            // Set the bi-directional mapping
            for (BuildConfiguration dependency : dependencies) {
                if (!dependency.getDependants().contains(buildConfiguration)) {
                    dependency.addDependant(buildConfiguration);
                }
            }
            buildConfiguration.setDependencies(dependencies);
            for (BuildConfiguration dependant : dependants) {
                if (!dependant.getDependencies().contains(buildConfiguration)) {
                    dependant.addDependant(buildConfiguration);
                }
            }
            buildConfiguration.setDependants(dependants);

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

        public Builder productVersions(Set<ProductVersion> productVersions) {
            this.productVersions = productVersions;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersions.add(productVersion);
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

        public Builder buildStatus(BuildStatus buildStatus) {
            this.buildStatus = buildStatus;
            return this;
        }

        public Builder repositories(String repositories) {
            this.repositories = repositories;
            return this;
        }

    }

}
