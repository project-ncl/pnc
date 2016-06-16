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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * Class that maps the artifacts created and/or used by the builds of the projects.
 * The "type" indicates the genesis of the artifact, whether it has been imported from 
 * external repositories, or built internally.
 * 
 * The repoType indicated the type of repository which is used to distributed the artifact.
 * The repoType repo indicates the format for the identifier field.
 * 
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "identifier", "checksum" }) )
public class Artifact implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String SEQUENCE_NAME = "artifact_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Contains a string which uniquely identifies the artifact in a repository.
     * For example, for a maven artifact this is the GATVC (groupId:artifactId:type:version[:qualifier]
     * The format of the identifier string is determined by the repoType
     */
    @NotNull
    @Column(updatable=false)
    private String identifier;

    @NotNull
    @Column(updatable=false)
    private String checksum;

    @NotNull
    @Enumerated(value = EnumType.STRING)
    private Artifact.Quality artifactQuality;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc).  This field determines
     * the format of the identifier string.
     */
    @NotNull
    @Column(updatable=false)
    private ArtifactRepo.Type repoType;

    @Column(updatable=false)
    private String filename;

    /**
     * Repository URL where the artifact file is available.
     */
    @Column(updatable=false)
    private String deployUrl;

    /**
     * The record of the build which produced this artifact
     */
    @ManyToMany(mappedBy = "builtArtifacts")
    private Set<BuildRecord> buildRecords;

    /**
     * The builds which depend on this artifact
     */
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildRecord> dependantBuildRecords;

    /**
     * The location from which this artifact was originally downloaded for import
     */
    @Column(unique=true, updatable=false)
    private String originUrl;

    /**
     * The date when this artifact was originally imported
     */
    @Column(updatable=false)
    private Date importDate;

    /**
     * The product milestone releases which distribute this artifact
     */
    @ManyToMany(mappedBy = "distributedArtifacts")
    private Set<ProductMilestone> distributedInProductMilestones;

    public enum Quality {

        /**
         * The artifact has not yet been verified or tested
         */
        NEW,

        /**
         * The artifact has been verified by an automated process, but has not yet been tested against
         * a complete product or other large set of components.
         */
        VERIFIED,

        /**
         * The artifact has passed integration testing.
         */
        TESTED,

        /**
         * The artifact should no longer be used due to lack of support and/or a better alternative
         * being available.
         */
        DEPRECATED,

        /**
         * The artifact contains a severe defect, possibly a functional or security issue.
         */
        BLACKLISTED,

    }

    /**
     * Basic no-arg constructor.  Initializes the buildRecords and dependantBuildRecords to 
     * empty set.
     */
    public Artifact() {
        buildRecords = new HashSet<>();
        dependantBuildRecords = new HashSet<>();
        distributedInProductMilestones = new HashSet<>();
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
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets the identifier.
     * 
     * The identifier should contain different logic depending on the artifact type: i.e Maven should contain the GAV, NPM and
     * CocoaPOD should be identified differently
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the new identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the checksum.
     *
     * @return the checksum
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * Sets the checksum.
     *
     * @param checksum the new checksum
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Artifact.Quality getArtifactQuality() {
        return artifactQuality;
    }

    public void setArtifactQuality(Artifact.Quality artifactQuality) {
        this.artifactQuality = artifactQuality;
    }

    /**
     * Check if this artifact has an associated build record
     * @return true if there is a build record for this artifact, false otherwise
     */
    public boolean isBuilt() {
        return (buildRecords != null && buildRecords.size() > 0);
    }

    /** Check if this artifact was imported from a remote URL
     * @return true if there is an originUrl
     */
    public boolean isImported() {
        return (originUrl != null && !originUrl.isEmpty());
    }

    public boolean isTrusted() {
        return (isBuilt() || ArtifactRepo.isTrusted(originUrl));
    }

    /**
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the filename.
     *
     * @param filename the new filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Gets the deploy url.
     *
     * @return the deploy url
     */
    public String getDeployUrl() {
        return deployUrl;
    }

    /**
     * Sets the deploy url.
     *
     * @param deployUrl the new deploy url
     */
    public void setDeployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
    }

    /**
     * Gets the set of build records which produced this artifact.
     *
     * @return the set of build records
     */
    public Set<BuildRecord> getBuildRecords() {
        return buildRecords;
    }

    /**
     * Sets the project build record.
     *
     * @param buildRecords the set of build records
     */
    public void setBuildRecords(Set<BuildRecord> buildRecords) {
        this.buildRecords = buildRecords;
    }

    /**
     * Add a build record which produced this artifact
     *
     * @param buildRecord the new project build record
     * @return 
     */
    public boolean addBuildRecord(BuildRecord buildRecord) {
        return this.buildRecords.add(buildRecord);
    }

    public Set<BuildRecord> getDependantBuildRecords() {
        return dependantBuildRecords;
    }

    public void setDependantBuildRecords(Set<BuildRecord> buildRecords) {
        this.dependantBuildRecords = buildRecords;
    }

    public void addDependantBuildRecord(BuildRecord buildRecord) {
        if (!dependantBuildRecords.contains(buildRecord)) {
            this.dependantBuildRecords.add(buildRecord);
        }
    }

    /**
     * @return the repoType
     */
    public ArtifactRepo.Type getRepoType() {
        return repoType;
    }

    /**
     * @param repoType the repoType to set
     */
    public void setRepoType(ArtifactRepo.Type repoType) {
        this.repoType = repoType;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public Set<ProductMilestone> getDistributedInProductMilestones() {
        return distributedInProductMilestones;
    }

    public void setDistributedInProductMilestones(Set<ProductMilestone> distributedInProductMilestones) {
        this.distributedInProductMilestones = distributedInProductMilestones;
    }

    public boolean addDistributedInProductMilestone(ProductMilestone productMilestone) {
        return this.distributedInProductMilestones.add(productMilestone);
    }

    @Override
    public String toString() {
        return "Artifact [id: " + id + ", identifier=" + identifier + ", quality=" + artifactQuality + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Artifact)) {
            return false;
        }
        if (identifier == null || checksum == null) {
            return this == obj;
        }
        Artifact compare = (Artifact)obj;
        return (identifier.equals(compare.getIdentifier()) && checksum.equals(compare.getChecksum()));
    }

    @Override
    public int hashCode() {
        return (identifier + checksum).hashCode();
    }

    public static class Builder {

        private Integer id;

        private String identifier;

        private String checksum;

        private Quality artifactQuality;

        private ArtifactRepo.Type repoType;

        private String filename;

        private String deployUrl;

        private Set<BuildRecord> dependantBuildRecords;

        private Set<BuildRecord> buildRecords;

        private String originUrl;

        private Date importDate;

        private Builder() {
            buildRecords = new HashSet<>();
            dependantBuildRecords = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Artifact build() {
            Artifact artifact = new Artifact();
            artifact.setId(id);
            artifact.setIdentifier(identifier);
            artifact.setChecksum(checksum);
            if (artifactQuality == null) {
                artifactQuality = Quality.NEW;
            }
            artifact.setArtifactQuality(artifactQuality);
            artifact.setRepoType(repoType);
            artifact.setFilename(filename);
            artifact.setDeployUrl(deployUrl);
            if (dependantBuildRecords != null) {
                artifact.setDependantBuildRecords(dependantBuildRecords);
            }
            artifact.setBuildRecords(buildRecords);
            artifact.setOriginUrl(originUrl);
            artifact.setImportDate(importDate);

            return artifact;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder artifactQuality(Artifact.Quality artifactQuality) {
            this.artifactQuality = artifactQuality;
            return this;
        }

        public Builder repoType(ArtifactRepo.Type repoType) {
            this.repoType = repoType;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        public Builder deployUrl(String deployUrl) {
            this.deployUrl = deployUrl;
            return this;
        }

        public Builder dependantBuildRecords(Set<BuildRecord> dependantBuildRecords) {
            this.dependantBuildRecords = dependantBuildRecords;
            return this;
        }

        public Builder buildRecord(Set<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

        public Builder originUrl(String originUrl) {
            this.originUrl = originUrl;
            return this;
        }

        public Builder importDate(Date importDate) {
            this.importDate = importDate;
            return this;
        }

    }
}
