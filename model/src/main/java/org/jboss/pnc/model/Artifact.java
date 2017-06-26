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

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "identifier", "sha256" }) )
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
    @Size(max=255)
    @Column(updatable=false)
    private String identifier;

    @NotNull
    @Size(max=32)
    @Column(updatable=false)
    private String md5;

    @NotNull
    @Size(max=40)
    @Column(updatable=false)
    private String sha1;

    @NotNull
    @Size(max=64)
    @Column(updatable=false)
    private String sha256;

    @Getter
    @Setter
    @Column(updatable = false)
    private Long size;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Artifact.Quality artifactQuality;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc).  This field determines
     * the format of the identifier string.
     */
    @NotNull
    @Column(updatable=false)
    private ArtifactRepo.Type repoType;

    @Size(max=255)
    @Column(updatable=false)
    private String filename;

    /**
     * Repository URL where the artifact file is available.
     */
    @Size(max=500)
    @Column(updatable=false, length=500)
    private String deployPath;

    /**
     * The record of the build which produced this artifact.
     * Usually there should be only one build record that produced this artifact.
     * However some other build may produce the same artifact (same checksum)
     * in such case we link the BuildRecord to the same artifact.
     */
    @ManyToMany(mappedBy = "builtArtifacts")
    private Set<BuildRecord> buildRecords;

    /**
     * The list of builds which depend on this artifact.
     * For example, if the build downloaded this artifact as a Maven dependency.
     */
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildRecord> dependantBuildRecords;

    /**
     * The location from which this artifact was originally downloaded for import
     */
    @Size(max=500)
    @Column(unique=true, updatable=false, length=500)
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
     * Try to use the {@link Artifact.Builder} instead.
     *
     * Basic no-arg constructor.  Initializes the buildRecords and dependantBuildRecords to
     * empty set.
     */
    Artifact() {
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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
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
    public String getDeployPath() {
        return deployPath;
    }

    /**
     * Sets the deploy path.
     *
     * @param deployPath the new deploy url
     */
    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
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
        if (identifier == null || md5 == null || sha1 == null || sha256 == null) {
            return this == obj;
        }
        Artifact compare = (Artifact)obj;
        return identifier.equals(compare.getIdentifier())
                && md5.equals(compare.getMd5())
                && sha1.equals(compare.getSha1())
                && sha256.equals(compare.getSha256());
    }

    @Override
    public int hashCode() {
        return (identifier + md5 + sha1 + sha256).hashCode();
    }

    public static class Builder {

        private Integer id;

        private String identifier;

        private String md5;

        private String sha1;

        private String sha256;

        private Long size;

        private Quality artifactQuality;

        private ArtifactRepo.Type repoType;

        private String filename;

        private String deployPath;

        private Set<BuildRecord> dependantBuildRecords;

        private Set<BuildRecord> buildRecords;

        private Set<ProductMilestone> distributedInProductMilestones;

        private String originUrl;

        private Date importDate;

        private Builder() {
            buildRecords = new HashSet<>();
            dependantBuildRecords = new HashSet<>();
            distributedInProductMilestones = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Artifact build() {
            Artifact artifact = new Artifact();
            artifact.setId(id);
            artifact.setIdentifier(identifier);
            artifact.setMd5(md5);
            artifact.setSha1(sha1);
            artifact.setSha256(sha256);
            artifact.setSize(size);
            if (artifactQuality == null) {
                artifactQuality = Quality.NEW;
            }
            artifact.setArtifactQuality(artifactQuality);
            artifact.setRepoType(repoType);
            artifact.setFilename(filename);
            artifact.setDeployPath(deployPath);
            if (dependantBuildRecords != null) {
                artifact.setDependantBuildRecords(dependantBuildRecords);
            }
            artifact.setBuildRecords(buildRecords);
            artifact.setDistributedInProductMilestones(distributedInProductMilestones);
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

        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder sha1(String sha1) {
            this.sha1 = sha1;
            return this;
        }

        public Builder sha256(String sha256) {
            this.sha256 = sha256;
            return this;
        }

        public Builder size(Long size) {
            this.size = size;
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

        public Builder deployPath(String deployPath) {
            this.deployPath = deployPath;
            return this;
        }

        public Builder buildRecord(BuildRecord buildRecord) {
            this.buildRecords.add(buildRecord);
            return this;
        }

        public Builder buildRecords(Set<BuildRecord> buildRecords) {
            this.buildRecords = buildRecords;
            return this;
        }

        public Builder dependantBuildRecord(BuildRecord dependantBuildRecord) {
            this.dependantBuildRecords.add(dependantBuildRecord);
            return this;
        }

        public Builder dependantBuildRecords(Set<BuildRecord> dependantBuildRecords) {
            this.dependantBuildRecords = dependantBuildRecords;
            return this;
        }

        public Builder distributedInProductMilestones(Set<ProductMilestone> distributedInProductMilestones) {
            this.distributedInProductMilestones = distributedInProductMilestones;
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
