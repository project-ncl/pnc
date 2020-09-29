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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(name = "uk_buildconfigurationset_name", columnNames = "name"),
        indexes = @Index(name = "idx_buildconfigurationset_productversion", columnList = "productversion_id"))
public class BuildConfigurationSet implements GenericEntity<Integer> {

    private static final long serialVersionUID = 2596901834161647987L;

    public static final String DEFAULT_SORTING_FIELD = "id";
    public static final String SEQUENCE_NAME = "build_configuration_set_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Column(unique = true)
    @NotNull
    @Size(max = 255)
    private String name;

    @ManyToOne(cascade = { CascadeType.REFRESH, CascadeType.DETACH })
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildconfigurationset_productversion"))
    private ProductVersion productVersion;

    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany
    @JoinTable(
            name = "build_configuration_set_map",
            joinColumns = { @JoinColumn(
                    name = "build_configuration_set_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_build_configuration_set_map_buildconfigurationset")) },
            inverseJoinColumns = { @JoinColumn(
                    name = "build_configuration_id",
                    referencedColumnName = "id",
                    foreignKey = @ForeignKey(name = "fk_build_configuration_set_map_buildconfiguration")) },
            indexes = { @Index(name = "idx_build_configuration_set_map_bc", columnList = "build_configuration_id"),
                    @Index(name = "idx_build_configuration_set_map_bcs", columnList = "build_configuration_set_id") })
    private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

    @OneToMany(mappedBy = "buildConfigurationSet")
    private Set<BuildConfigSetRecord> buildConfigSetRecords = new HashSet<>();

    /**
     * Normally set to true. If BuildConfigurationSet is no longer to be used (is archived) - this is set to **null**
     */
    private Boolean active;

    public BuildConfigurationSet() {
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
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the id
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
     * @return the buildConfigurations set
     */
    public Set<BuildConfiguration> getBuildConfigurations() {
        return buildConfigurations;
    }

    /**
     * @param buildConfigurations the buildConfigurations to set
     */
    public void setBuildConfigurations(Set<BuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
    }

    /**
     * @param buildConfiguration the buildConfiguration to add to the set
     */
    public void addBuildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfigurations.add(buildConfiguration);
    }

    /**
     * @param buildConfiguration the buildConfiguration to remove from the set
     */
    public void removeBuildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfigurations.remove(buildConfiguration);
    }

    public Set<BuildConfigSetRecord> getBuildConfigSetRecords() {
        return buildConfigSetRecords;
    }

    public void setBuildConfigSetRecords(Set<BuildConfigSetRecord> buildConfigSetRecords) {
        this.buildConfigSetRecords = buildConfigSetRecords;
    }

    public boolean addBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        return this.buildConfigSetRecords.add(buildConfigSetRecord);
    }

    public boolean removeBuildConfigSetRecord(BuildConfigSetRecord buildConfigSetRecord) {
        return this.buildConfigSetRecords.remove(buildConfigSetRecord);
    }

    /**
     * Get the current product milestone (if any) associated with this build config set.
     *
     * @return The current product milestone for the product version associated with this build config set, or null if
     *         there is none
     */
    public ProductMilestone getCurrentProductMilestone() {
        if (getProductVersion() == null) {
            return null;
        }
        return getProductVersion().getCurrentProductMilestone();
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BuildConfigurationSet that = (BuildConfigurationSet) o;

        return id != null ? id.equals(that.getId()) : that.getId() == null;

    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "BuildConfigurationSet{" + "id=" + id + ", name='" + name + '\'' + ", productVersion=" + productVersion
                + ", buildConfigurations=" + buildConfigurations + ", buildConfigSetRecords=" + buildConfigSetRecords
                + ", active=" + active + '}';
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public static class Builder {

        private Integer id;

        private String name;

        private ProductVersion productVersion;

        private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

        private Set<BuildConfigSetRecord> buildConfigSetRecords = new HashSet<>();

        private boolean archived = false;

        private Builder() {

        }

        public BuildConfigurationSet build() {
            BuildConfigurationSet buildConfigurationSet = new BuildConfigurationSet();
            buildConfigurationSet.setId(id);
            buildConfigurationSet.setName(name);
            buildConfigurationSet.setProductVersion(productVersion);
            buildConfigurationSet.setBuildConfigurations(buildConfigurations);
            buildConfigurationSet.setArchived(archived);
            for (BuildConfiguration buildConfiguration : buildConfigurations) {
                buildConfiguration.addBuildConfigurationSet(buildConfigurationSet);
            }
            buildConfigurationSet.setBuildConfigSetRecords(buildConfigSetRecords);
            for (BuildConfigSetRecord buildConfigSetRecord : buildConfigSetRecords) {
                buildConfigSetRecord.setBuildConfigurationSet(buildConfigurationSet);
            }

            return buildConfigurationSet;
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder productVersion(ProductVersion productVersion) {
            this.productVersion = productVersion;
            return this;
        }

        public Builder buildConfigurations(Set<BuildConfiguration> buildConfigurations) {
            this.buildConfigurations = buildConfigurations;
            return this;
        }

        public Builder buildConfiguration(BuildConfiguration buildConfiguration) {
            this.buildConfigurations.add(buildConfiguration);
            return this;
        }

        public Builder archived(boolean archived) {
            this.archived = archived;
            return this;
        }

        public Builder buildConfigSetRecords(Set<BuildConfigSetRecord> buildConfigSetRecords) {
            this.buildConfigSetRecords = buildConfigSetRecords;
            return this;
        }
    }

}
