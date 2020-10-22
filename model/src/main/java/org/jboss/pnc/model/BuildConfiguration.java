/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.PersistenceException;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.enums.BuildType;

/**
 * The Class BuildConfiguration cointains the information needed to execute a build of a project, i.e. the sources, the
 * build script, the build system image needed to run, the project configurations that need to be triggered after a
 * successful build. Note that creationTime and lastModificationTime are handled internally via JPA settings and
 * therefore do not have public setters.
 * <p>
 * (project + name) should be unique
 *
 * @author avibelli
 * @author Jakub Bartecek
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Audited
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uk_build_configuration_name", columnNames = { "name", "active" }),
        indexes = { @Index(name = "idx_build_configuration_product_version", columnList = "productversion_id"),
                @Index(name = "idx_buildconfiguration_buildenvironment", columnList = "buildenvironment_id"),
                @Index(name = "idx_buildconfiguration_project", columnList = "project_id"),
                @Index(
                        name = "idx_buildconfiguration_repositoryconfiguration",
                        columnList = "repositoryconfiguration_id"),
                @Index(name = "idx_buildconfiguration_creation_user", columnList = "creationuser_id"),
                @Index(name = "idx_buildconfiguration_modification_user", columnList = "lastmodificationuser_id") })
public class BuildConfiguration implements GenericEntity<Integer>, Cloneable {

    private static final long serialVersionUID = -5890729679489304114L;

    public static final String DEFAULT_SORTING_FIELD = "name";
    public static final String SEQUENCE_NAME = "build_configuration_id_seq";

    @Id
    private Integer id;

    @NotNull
    @Size(max = 255)
    private String name;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String buildScript;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(
            updatable = true,
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_buildconfiguration_repositoryconfiguration"))
    private RepositoryConfiguration repositoryConfiguration;

    /**
     * Revision to build. This may not be the final revision on which the actual build gets executed, but MUST be the
     * starting point of the build process.
     */
    @Size(max = 255)
    private String scmRevision;

    @NotAudited
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @NotAudited
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_build_configuration_product_version"))
    private ProductVersion productVersion;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildconfiguration_project"))
    private Project project;

    @Enumerated(EnumType.STRING)
    private BuildType buildType;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @NotNull
    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildconfiguration_buildenvironment"))
    private BuildEnvironment buildEnvironment;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToMany(mappedBy = "buildConfigurations")
    private Set<BuildConfigurationSet> buildConfigurationSets;

    @NotNull
    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date creationTime;

    @NotNull
    @Column(columnDefinition = "timestamp with time zone")
    private Date lastModificationTime;

    /**
     * Normally set to true. If BuildConfiguration is no longer to be used (is archived) - this is set to **null**
     *
     * Workaround to have a database constraint for active configuration name
     */
    private Boolean active;

    /**
     * The set of build configs upon which this build depends. The build configs contained in dependencies should
     * normally be completed before this build config is executed. Similar to Maven dependencies.
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @NotAudited
    @ManyToMany(cascade = { CascadeType.REFRESH })
    @JoinTable(
            name = "build_configuration_dep_map",
            joinColumns = { @JoinColumn(
                    name = "dependency_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_build_configuration_dep_map_dependency")) },
            inverseJoinColumns = { @JoinColumn(
                    name = "dependant_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_build_configuration_dep_map_dependant")) },
            indexes = { @Index(name = "idx_build_configuration_dep_map_dependant", columnList = "dependant_id"),
                    @Index(name = "idx_build_configuration_dep_map_dependency", columnList = "dependency_id") })
    private Set<BuildConfiguration> dependencies;

    /**
     * The set of build configs which depend upon this config. These builds must normally be built after this build is
     * completed. This is the reverse relationship as Maven dependencies.
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @NotAudited
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildConfiguration> dependants;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "build_configuration_parameters",
            joinColumns = @JoinColumn(
                    name = "buildconfiguration_id",
                    foreignKey = @ForeignKey(name = "fk_build_configuration_parameters_bc")))
    @MapKeyColumn(length = 50, name = "key", nullable = false)
    @Column(name = "value", nullable = false, length = 8192)
    private Map<String, String> genericParameters = new HashMap<>();

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_build_configuration_creation_user"), updatable = false)
    private User creationUser;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_build_configuration_modification_user"), updatable = true)
    private User lastModificationUser;

    private String defaultAlignmentParams;

    /**
     * Indicates whether the Brew Bridge Pull feature is active or not
     */
    private boolean brewPullActive = false;

    /**
     * Instantiates a new project build configuration.
     */
    public BuildConfiguration() {
        dependencies = new HashSet<>();
        dependants = new HashSet<>();
        buildConfigurationSets = new HashSet<>();

        // The lastModificationTime needs to be non-null for certain use cases even though the
        // actual value is managed by JPA. For example, if saving an entity with a relation
        // to BuildConfiguration, JPA will not allow the related entity to be saved unless
        // the Build Configuration contains both an ID and a non-null lastModificationTime
        lastModificationTime = Date.from(Instant.now());
        active = Boolean.TRUE;
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

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.nullIfBlank(description);
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
    public ProductVersion getProductVersion() {
        return productVersion;
    }

    /**
     * @param productVersion the productVersion associated with this build config
     */
    public void setProductVersion(ProductVersion productVersion) {
        this.productVersion = productVersion;
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
        buildConfigurationSets.add(buildConfigurationSet);
        buildConfigurationSet.getBuildConfigurations().add(this);
    }

    /**
     * @param buildConfigurationSet remove from the list of buildConfigurationSets
     */
    public void removeBuildConfigurationSet(BuildConfigurationSet buildConfigurationSet) {
        this.buildConfigurationSets.remove(buildConfigurationSet);
        buildConfigurationSet.getBuildConfigurations().remove(this);
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
            String depPathString = depPath.stream()
                    .map(BuildConfiguration::getName)
                    .collect(Collectors.joining(" -> "));
            throw new PersistenceException(
                    "Unable to add dependency, would create a circular reference: " + depPathString);
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
     * Gets the full set of indirect dependencies (dependencies of dependencies). In cases where a particular dependency
     * is both a direct dependency and is an indirect dependency, it will be included in the set.
     *
     * @return The set of indirect dependencies
     */
    public Set<BuildConfiguration> getIndirectDependencies() {
        Set<BuildConfiguration> indirectDependencies = new HashSet<>();
        List<BuildConfiguration> configsToCheck = new ArrayList<>();
        configsToCheck.addAll(getDependencies());
        while (!configsToCheck.isEmpty()) {
            BuildConfiguration nextConfig = configsToCheck.get(0);
            for (BuildConfiguration nextDep : nextConfig.getDependencies()) {
                if (!indirectDependencies.contains(nextDep)) {
                    // Do not add an indirect dependency multiple times
                    indirectDependencies.add(nextDep);
                    // Do not check a config multiple times
                    if (!configsToCheck.contains(nextDep)) {
                        configsToCheck.add(nextDep);
                    }
                }
            }
            configsToCheck.remove(nextConfig);
        }
        return indirectDependencies;
    }

    /**
     * Perform a depth first search of the dependencies to find a match of the given build config. Returns a list with a
     * single build config (this), if no match is found.
     *
     * @param buildConfig The build config to search for
     * @return A list of the build configurations in the path between this config and the given config.
     */
    public List<BuildConfiguration> dependencyDepthFirstSearch(BuildConfiguration buildConfig) {
        List<BuildConfiguration> path = new ArrayList<>();
        path.add(this);
        return this.dependencyDepthFirstSearch(buildConfig, path);
    }

    private List<BuildConfiguration> dependencyDepthFirstSearch(
            BuildConfiguration buildConfig,
            List<BuildConfiguration> path) {
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
        Set<BuildConfiguration> allDependencies = new HashSet<>();
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
     * This method is private because a dependant should never be added externally. Instead the dependency relation
     * should be set up using the addDependency method
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
     * This method is private because a dependant should never be removed externally. Instead the dependency relation
     * should be set up using the removeDependency method
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
    public void setCreationTime(Date creationTime) {
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
    public void setLastModificationTime(Date lastModificationTime) {
        if (lastModificationTime != null) {
            this.lastModificationTime = lastModificationTime;
        }
    }

    /**
     * @return true if this build config should no longer be used for builds
     */
    public boolean isArchived() {
        return !Boolean.TRUE.equals(active);
    }

    public void setArchived(boolean archived) {
        this.active = archived ? null : true;
    }

    /**
     * Get the current product milestone (if any) associated with this build config.
     *
     * @return The current product milestone for the product version associated with this build config, or null if there
     *         is none
     */
    public ProductMilestone getCurrentProductMilestone() {
        if (getProductVersion() == null) {
            return null;
        }
        return getProductVersion().getCurrentProductMilestone();
    }

    public RepositoryConfiguration getRepositoryConfiguration() {
        return repositoryConfiguration;
    }

    public void setRepositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
        this.repositoryConfiguration = repositoryConfiguration;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Map<String, String> getGenericParameters() {
        return genericParameters;
    }

    public void setGenericParameters(Map<String, String> genericParameters) {
        this.genericParameters = genericParameters;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

    public User getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(User creationUser) {
        this.creationUser = creationUser;
    }

    public User getLastModificationUser() {
        return lastModificationUser;
    }

    public void setLastModificationUser(User lastModificationUser) {
        this.lastModificationUser = lastModificationUser;
    }

    /**
     * @return the default alignment parameters for this build config type
     */
    public String getDefaultAlignmentParams() {
        return defaultAlignmentParams;
    }

    /**
     * @param defaultAlignmentParams the default alignment parameters
     */
    public void setDefaultAlignmentParams(String defaultAlignmentParams) {
        this.defaultAlignmentParams = StringUtils.nullIfBlank(defaultAlignmentParams);
    }

    public boolean isBrewPullActive() {
        return brewPullActive;
    }

    public void setBrewPullActive(boolean brewPullActive) {
        this.brewPullActive = brewPullActive;
    }

    @Override
    public String toString() {
        return "BuildConfiguration " + getId() + " [project=" + getProject() + ", name=" + getName() + ", active="
                + active + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BuildConfiguration))
            return false;
        return id != null && id.equals(((BuildConfiguration) o).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public static final String CLONE_PREFIX_DATE_FORMAT = "yyyyMMddHHmmss";

    /**
     * Creates a shallow based clone and overrides {@link #creationTime}, {@link #name} and erases {@link #id}.
     *
     * @return This objects clone.
     */
    @Override
    public BuildConfiguration clone() {
        // do not use this.clone as it clones the entity proxy object
        BuildConfiguration clone = new BuildConfiguration();
        clone.id = null;
        Date now = Date.from(Instant.now());
        clone.active = true;
        clone.buildConfigurationSets = new HashSet<>(); // Don't add the clone to the set
        clone.buildEnvironment = buildEnvironment;
        clone.buildScript = buildScript;
        clone.creationTime = now;
        clone.dependants = new HashSet<>(); // Don't add the clone as dependency to parents.
        clone.dependencies = new HashSet<>(dependencies);
        clone.description = description;
        clone.genericParameters = new HashMap<>(genericParameters);
        clone.name = retrieveCloneName(name, now);
        clone.productVersion = null; // Clone is mainly for cloning BC to use in new Product Version
        clone.project = project;
        clone.buildType = buildType;
        clone.repositoryConfiguration = repositoryConfiguration;
        clone.scmRevision = scmRevision;
        clone.creationUser = null;
        clone.lastModificationUser = null;
        clone.defaultAlignmentParams = defaultAlignmentParams;
        return clone;
    }

    /**
     * Change the BC clone name into date_original-BC-name where date will be for every clone updated and for original
     * BC names will be added.
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

    public boolean dependsOn(BuildConfiguration other) {
        return getAllDependencies().contains(other);
    }

    public boolean dependsOnAny(Collection<BuildConfiguration> otherList) {
        return otherList.stream().anyMatch(this::dependsOn);
    }

    public static class Builder {

        private Integer id;

        private String name;

        private String buildScript;

        private RepositoryConfiguration repositoryConfiguration;

        private String scmRevision;

        private String description;

        private Project project;

        private BuildType buildType;

        private BuildEnvironment buildEnvironment;

        private ProductVersion productVersion;

        private Set<BuildConfiguration> dependencies;

        private Set<BuildConfiguration> dependants;

        private Set<BuildConfigurationSet> buildConfigurationSets;

        private Date creationTime;

        private Date lastModificationTime;

        private boolean archived = false;

        private Map<String, String> genericParameters = new HashMap<>();

        private User creationUser;

        private User lastModificationUser;

        private String defaultAlignmentParams;

        private boolean brewPullActive = false;

        private Builder() {
            dependencies = new HashSet<>();
            dependants = new HashSet<>();
            buildConfigurationSets = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public BuildConfiguration build() {
            BuildConfiguration buildConfiguration = new BuildConfiguration();
            buildConfiguration.setId(id);
            buildConfiguration.setName(name);
            buildConfiguration.setBuildScript(buildScript);
            buildConfiguration.setRepositoryConfiguration(repositoryConfiguration);
            buildConfiguration.setScmRevision(scmRevision);
            buildConfiguration.setDescription(description);

            // Set the bi-directional mapping
            if (project != null) {
                project.addBuildConfiguration(buildConfiguration);
            }
            buildConfiguration.setProject(project);
            buildConfiguration.setBuildType(buildType);
            buildConfiguration.setBuildEnvironment(buildEnvironment);
            buildConfiguration.setCreationTime(creationTime);
            buildConfiguration.setLastModificationTime(lastModificationTime);
            buildConfiguration.setArchived(archived);
            buildConfiguration.setGenericParameters(genericParameters);
            buildConfiguration.setBuildConfigurationSets(buildConfigurationSets);
            buildConfiguration.setProductVersion(productVersion);

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
                    dependant.addDependency(buildConfiguration);
                }
            }
            buildConfiguration.setDependants(dependants);
            buildConfiguration.setCreationUser(creationUser);
            buildConfiguration.setLastModificationUser(lastModificationUser);

            buildConfiguration.setDefaultAlignmentParams(defaultAlignmentParams);
            buildConfiguration.setBrewPullActive(brewPullActive);

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

        public Builder repositoryConfiguration(RepositoryConfiguration repositoryConfiguration) {
            this.repositoryConfiguration = repositoryConfiguration;
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

        public Builder buildType(BuildType buildType) {
            this.buildType = buildType;
            return this;
        }

        public Builder buildEnvironment(BuildEnvironment buildEnvironment) {
            this.buildEnvironment = buildEnvironment;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
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

        public Builder archived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public Builder genericParameters(Map<String, String> genericParameters) {
            this.genericParameters = genericParameters;
            return this;
        }

        public Builder creationUser(User creationUser) {
            this.creationUser = creationUser;
            return this;
        }

        public Builder lastModificationUser(User lastModificationUser) {
            this.lastModificationUser = lastModificationUser;
            return this;
        }

        public Builder defaultAlignmentParams(String defaultAlignmentParams) {
            this.defaultAlignmentParams = defaultAlignmentParams;
            return this;
        }

        public Builder brewPullActive(boolean brewPullActive) {
            this.brewPullActive = brewPullActive;
            return this;
        }
    }

}
