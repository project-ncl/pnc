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

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Class BuildConfiguration cointains the information needed to execute a build of a project, i.e. the sources,
 * the build script, the build system image needed to run, the project configurations that need to be triggered after
 * a successful build. Note that creationTime and lastModificationTime are handled internally via JPA settings
 * and therefore do not have public setters.
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
    public static final String SEQUENCE_NAME = "build_configuration_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Column(unique = true)
    @NotNull
    private String name;

    @Lob
    private String buildScript;

    /**
     * The upstream/community scm repo URL submitted by the user.
     */
    private String scmRepoURL;

    /**
     * The upstream/community scm revision (commit ID/tag/branch) submitted by the user.
     */
    private String scmRevision;

    /**
     * The URL of the internal mirror of the upstream repository. For builds which require the sources to be mirrored to a
     * secured location before building.
     */
    private String scmMirrorRepoURL;

    /**
     * The SCM revision of the internal mirror of the upstream repository. Contains the revision after any automated source
     * changes have been made by the build system.
     */
    private String scmMirrorRevision;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @NotAudited
    @ManyToMany
    @JoinTable(name = "build_configuration_product_versions_map", joinColumns = {
            @JoinColumn(name = "build_configuration_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "product_version_id", referencedColumnName = "id") }, uniqueConstraints = @UniqueConstraint(name = "UK_build_configuration_id_product_version_id", columnNames = {
                            "build_configuration_id", "product_version_id" }) )
    @ForeignKey(name = "fk_build_configuration_product_versions_map_buildconfiguration", inverseName = "fk_build_configuration_product_versions_map_productversion")
    @Index(name="idx_build_configuration_product_versions_map_buildconfiguration", columnNames={"build_configuration_id", "product_version_id"} )
    private Set<ProductVersion> productVersions;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    @ForeignKey(name = "fk_buildconfiguration_project")
    @Index(name="idx_buildconfiguration_project")
    private Project project;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    @ForeignKey(name = "fk_buildconfiguration_buildenvironment")
    @Index(name="idx_buildconfiguration_buildenvironment")
    private BuildEnvironment buildEnvironment;

    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "latestBuildConfiguration")
    private Set<BuildRecord> buildRecords;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToMany(mappedBy = "buildConfigurations")
    private Set<BuildConfigurationSet> buildConfigurationSets;

    @NotNull
    @Column(columnDefinition = "timestamp with time zone", updatable=false)
    private Date creationTime;

    /**
     * The time at which this entity was last modified and saved to the database.  This is automatically
     * managed by JPA.
     */
    @NotNull
    @Version
    @Column(columnDefinition = "timestamp with time zone")
    private Date lastModificationTime;

    /**
     * Represents the status of the most recent execution of the current configuration.
     */
    @Enumerated(value = EnumType.STRING)
    private BuildStatus buildStatus;

    /**
     * The set of build configs upon which this build depends. The build configs contained in dependencies should normally be
     * completed before this build config is executed. Similar to Maven dependencies.
     */
    @NotAudited
    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(name = "build_configuration_dep_map", joinColumns = {
            @JoinColumn(name = "dependency_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "dependant_id", referencedColumnName = "id") })
    @ForeignKey(name = "fk_build_configuration_dep_map_dependency", inverseName = "fk_build_configuration_dep_map_dependant")
    @Index(name="idx_build_configuration_dep_map_dependency", columnNames={"dependency_id", "dependant_id"} )
    private Set<BuildConfiguration> dependencies;

    /**
     * The set of build configs which depend upon this config. These builds must normally be built after this build is
     * completed. This is the reverse relationship as Maven dependencies.
     */
    @NotAudited
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildConfiguration> dependants;

    // TODO: What data format does Aprox need?
    // [jdcasey] I'm not sure what this is supposed to do in the repository
    // manager...so hard to say what format is required.
    // @Column(name = "repositories")
    private String repositories;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfiguration() {
        dependencies = new HashSet<BuildConfiguration>();
        dependants = new HashSet<BuildConfiguration>();
        buildRecords = new HashSet<BuildRecord>();
        buildConfigurationSets = new HashSet<BuildConfigurationSet>();

        // The lastModificationTime needs to be non-null for certain use cases even though the
        // actual value is managed by JPA.  For example, if saving an entity with a relation
        // to BuildConfiguration, JPA will not allow the related entity to be saved unless
        // the Build Configuration contains both an ID and a non-null lastModificationTime
        lastModificationTime = Date.from(Instant.now());
    }

    /**
     * Set the creation time immediately before the config is persisted
     */
    @PrePersist
    private void initCreationTime() {
        this.creationTime = Date.from(Instant.now());
    }

    @PreRemove
    private void removeConfigurationFromSets() {
        for (BuildConfigurationSet bcs : buildConfigurationSets) {
            bcs.getBuildConfigurations().remove(this);
        }
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public String getScmMirrorRepoURL() {
        return scmMirrorRepoURL;
    }

    public void setScmMirrorRepoURL(String scmMirrorRepoURL) {
        this.scmMirrorRepoURL = scmMirrorRepoURL;
    }

    public String getScmMirrorRevision() {
        return scmMirrorRevision;
    }

    public void setScmMirrorRevision(String scmMirrorRevision) {
        this.scmMirrorRevision = scmMirrorRevision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Project getProject() {
        return project;
    }

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

    public BuildEnvironment getBuildEnvironment() {
        return buildEnvironment;
    }

    public void setBuildEnvironment(BuildEnvironment buildEnvironment) {
        this.buildEnvironment = buildEnvironment;
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
        if (buildConfigurationSets == null) {
            this.buildConfigurationSets.clear();
        } else {
            this.buildConfigurationSets = buildConfigurationSets;
        }
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
     * @return the dependency build configs (only direct dependencies)
     */
    public Set<BuildConfiguration> getDependencies() {
        return dependencies;
    }

    /**
     * @param dependencies the set of build configs upon which this build depends
     */
    public void setDependencies(Set<BuildConfiguration> dependencies) {
        this.dependencies = dependencies;
    }

    public boolean addDependency(BuildConfiguration dependency) {
        // Don't allow a build config to depend on itself
        if (dependency.getId().equals(this.getId())) {
            throw new PersistenceException("A build configuration cannot depend on itself");
        }
        // Verify that we are not creating a circular dependency
        if (dependency.getAllDependencies().contains(this)) {
            List<BuildConfiguration> depPath = dependency.dependencyDepthFirstSearch(this);
            String depPathString = depPath.stream().map(dep -> dep.getName()).collect(Collectors.joining(" -> "));
            throw new PersistenceException("Unable to add dependency, would create a circular reference: " + depPathString);
        }

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
     * Gets the full set of indirect dependencies (dependencies of dependencies). In cases where a particular dependency is both
     * a direct dependency and is an indirect dependency, it will be included in the set.
     *
     * @return The set of indirect dependencies
     */
    public Set<BuildConfiguration> getIndirectDependencies() {
        Set<BuildConfiguration> indirectDependencies = new HashSet<BuildConfiguration>();
        List<BuildConfiguration> configsToCheck = new ArrayList<BuildConfiguration>();
        configsToCheck.addAll(getDependencies());
        while (!configsToCheck.isEmpty()) {
            BuildConfiguration nextConfig = configsToCheck.get(0);
            for (BuildConfiguration nextDep : nextConfig.getDependencies()) {
                if (!indirectDependencies.contains(nextDep)) {
                    indirectDependencies.add(nextDep);
                    configsToCheck.add(nextDep);
                }
            }
            configsToCheck.remove(nextConfig);
        }
        return indirectDependencies;
    }

    /**
     * Perform a depth first search of the dependencies to find a match of the given build config. Returns a list with a single
     * build config (this), if no match is found.
     *
     * @param buildConfig The build config to search for
     * @return A list of the build configurations in the path between this config and the given config.
     */
    public List<BuildConfiguration> dependencyDepthFirstSearch(BuildConfiguration buildConfig) {
        List<BuildConfiguration> path = new ArrayList<>();
        path.add(this);
        return this.dependencyDepthFirstSearch(buildConfig, path);
    }

    private List<BuildConfiguration> dependencyDepthFirstSearch(BuildConfiguration buildConfig, List<BuildConfiguration> path) {
        for (BuildConfiguration dep : getDependencies()) {
            path.add(dep);
            if (dep.equals(buildConfig)) {
                return path;
            } else {
                path = dep.dependencyDepthFirstSearch(buildConfig, path);
                if (path.get(path.size() - 1).equals(buildConfig)) {
                    return path;
                }
            }
            path.remove(path.size() - 1);
        }
        return path;
    }

    /**
     * Get the full set of both the direct and indirect dependencies.
     *
     * @return A set containing both direct and indirect dependencies
     */
    public Set<BuildConfiguration> getAllDependencies() {
        Set<BuildConfiguration> allDependencies = new HashSet<BuildConfiguration>();
        allDependencies.addAll(getDependencies());
        allDependencies.addAll(getIndirectDependencies());
        return allDependencies;
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

    /**
     * This method is private because a dependant should never be added externally. Instead the dependency relation should be
     * set up using the addDependency method
     *
     * @param dependant
     * @return
     */
    private boolean addDependant(BuildConfiguration dependant) {
        boolean result = dependants.add(dependant);
        if (!dependant.getDependencies().contains(this)) {
            dependant.addDependency(this);
        }
        return result;
    }

    /**
     * This method is private because a dependant should never be removed externally. Instead the dependency relation should be
     * set up using the removeDependency method
     *
     * @param dependant
     * @return
     */
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
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime The time at which this config was created
     */
    private void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the lastModificationTime
     */
    public Date getLastModificationTime() {
        return lastModificationTime;
    }

    /**
     * @param lastModificationTime the time at which this config was last modified
     */
    private void setLastModificationTime(Date lastModificationTime) {
        if(lastModificationTime != null) {
            this.lastModificationTime = lastModificationTime;
        }
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

    public Set<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    public BuildConfiguration addBuildRecord(BuildRecord buildRecord) {
        this.buildRecords.add(buildRecord);
        return this;
    }

    @Override
    public String toString() {
        return "BuildConfiguration " + getId() + " [project=" + getProject() + ", name=" + getName() + "]";
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

    public static final String CLONE_PREFIX_DATE_FORMAT = "yyyyMMddHHmmss";

    /**
     * Creates a shallow based clone and overrides {@link #creationTime}, {@link #name} and erases {@link #id}.
     *
     * @return This objects clone.
     */
    @Override
    public BuildConfiguration clone() {
        try {
            BuildConfiguration clone = (BuildConfiguration) super.clone();
            Date now = Date.from(Instant.now());
            clone.name = retrieveCloneName(name, now);
            clone.creationTime = now;
            clone.id = null;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Cloning error" + e);
        }
    }

    /**
     * Change the BC clone name into date_original-BC-name where date will be for every clone updated and for original BC names
     * will be added.
     * 
     * Example: clone1 of pslegr-BC on Wednesday October,21st, 2015: 20151021095415_pslegr-BC
     * 
     * clone2 of 20151021095415_pslegr-BC on Thursday October,22nd, 2015: 20151022nnnnnn_pslegr-BC
     * 
     * clone3 of pslegr-BC on Friday October,23rd, 2015: 20151023nnnnnn_pslegr-BC
     * 
     * @param bcName
     * @param now
     * @return A correct name for the cloned BuildConfiguration
     */
    public static String retrieveCloneName(String bcName, Date now) {

        String bcNameToAppend = "";

        int index = bcName.indexOf("_");

        if (index == -1) {
            // No '_' was found, need to append date prefix to whole bcName
            bcNameToAppend = bcName;

        } else {
            // A '_' char was found, need to analyze if the prefix is a date (to
            // be replaced with new one)
            String prefix = bcName.substring(0, index);

            if (prefix.length() == CLONE_PREFIX_DATE_FORMAT.length()) {
                try {
                    new SimpleDateFormat(CLONE_PREFIX_DATE_FORMAT).parse(prefix);
                    // The prefix was a date, need to append new date to a substring
                    // of original bcName
                    bcNameToAppend = bcName.substring(index + 1);
                } catch (ParseException ex) {
                    // The prefix was not a date, need to append date prefix to
                    // whole bcName
                    bcNameToAppend = bcName;
                }
            } else {
                bcNameToAppend = bcName;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(new SimpleDateFormat(CLONE_PREFIX_DATE_FORMAT).format(now)).append("_").append(bcNameToAppend);
        return sb.toString();

    }

    public static class Builder {

        private Integer id;

        private String name;

        private String buildScript;

        private String scmRepoURL;

        private String scmRevision;

        private String scmMirrorRepoURL;

        private String scmMirrorRevision;

        private String description;

        private Project project;

        private BuildEnvironment buildEnvironment;

        private Set<ProductVersion> productVersions;

        private Set<BuildConfiguration> dependencies;

        private Set<BuildConfiguration> dependants;

        private Set<BuildConfigurationSet> buildConfigurationSets;

        private Date creationTime;

        private Date lastModificationTime;

        private BuildStatus buildStatus;

        private String repositories;

        private Builder() {
            dependencies = new HashSet<BuildConfiguration>();
            dependants = new HashSet<BuildConfiguration>();
            buildConfigurationSets = new HashSet<BuildConfigurationSet>();
            productVersions = new HashSet<ProductVersion>();
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
            buildConfiguration.setScmMirrorRepoURL(scmMirrorRepoURL);
            buildConfiguration.setScmMirrorRevision(scmMirrorRevision);
            buildConfiguration.setDescription(description);

            // Set the bi-directional mapping
            if (project != null) {
                project.addBuildConfiguration(buildConfiguration);
            }
            buildConfiguration.setProject(project);

            buildConfiguration.setBuildEnvironment(buildEnvironment);
            buildConfiguration.setCreationTime(creationTime);
            buildConfiguration.setLastModificationTime(lastModificationTime);
            buildConfiguration.setBuildStatus(buildStatus);
            buildConfiguration.setRepositories(repositories);
            buildConfiguration.setBuildConfigurationSets(buildConfigurationSets);
            buildConfiguration.setProductVersions(productVersions);

            for (BuildConfigurationSet buildConfigurationSet : buildConfigurationSets) {
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

        public Builder scmMirrorRepoURL(String scmMirrorRepoURL) {
            this.scmMirrorRepoURL = scmMirrorRepoURL;
            return this;
        }

        public Builder scmMirrorRevision(String scmMirrorRevision) {
            this.scmMirrorRevision = scmMirrorRevision;
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

        public Builder buildEnvironment(BuildEnvironment buildEnvironment) {
            this.buildEnvironment = buildEnvironment;
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

        public Builder creationTime(Date creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder lastModificationTime(Date lastModificationTime) {
            this.lastModificationTime = lastModificationTime;
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
