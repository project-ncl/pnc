/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.enums.ArtifactQuality;
import org.jboss.pnc.enums.BuildCategory;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PersistenceException;
import javax.persistence.PreRemove;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 *
 * Class that maps the artifacts created and/or used by the builds of the projects. The "type" indicates the genesis of
 * the artifact, whether it has been imported from external repositories, or built internally.
 *
 * The repoType indicated the type of repository which is used to distributed the artifact. The repoType repo indicates
 * the format for the identifier field.
 *
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_artifact_name",
                columnNames = { "identifier", "sha256", "targetRepository_id" }),
        indexes = { @Index(name = "idx_artifact_targetRepository", columnList = "targetRepository_id"),
                @Index(name = "idx_artifact_identifier", columnList = "identifier"),
                @Index(name = "idx_artifact_filename", columnList = "filename"),
                @Index(name = "idx_artifact_md5", columnList = "md5"),
                @Index(name = "idx_artifact_sha1", columnList = "sha1"),
                @Index(name = "idx_artifact_sha256", columnList = "sha256"),
                @Index(name = "idx_artifact_creation_user", columnList = "creationuser_id"),
                @Index(name = "idx_artifact_modification_user", columnList = "modificationUser_id"),
                @Index(name = "idx_artifact_buildrecord", columnList = "buildrecord_id") })
public class Artifact implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String SEQUENCE_NAME = "artifact_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Contains a string which uniquely identifies the artifact in a repository. For example, for a maven artifact this
     * is the GATVC (groupId:artifactId:type:version[:qualifier] The format of the identifier string is determined by
     * the repoType
     */
    @NotNull
    @Size(max = 1024)
    private String identifier;

    /**
     * Package URL with format scheme:type/namespace/name@version?qualifiers#subpath. A purl is a URL string used to
     * identify and locate a software package in a mostly universal and uniform way across programing languages, package
     * managers, packaging conventions, tools, APIs and databases. Such a package URL is useful to reliably reference
     * the same software package using a simple and expressive syntax and conventions based on familiar URLs. See
     * https://github.com/package-url/purl-spec
     */
    @Size(max = 1024)
    private String purl;

    @NotNull
    @Size(max = 32)
    private String md5;

    @NotNull
    @Size(max = 40)
    private String sha1;

    @NotNull
    @Size(max = 64)
    private String sha256;

    private Long size;

    @Audited
    @NotNull
    @Enumerated(EnumType.STRING)
    private ArtifactQuality artifactQuality;

    @Audited
    @NotNull
    @Enumerated(EnumType.STRING)
    private BuildCategory buildCategory;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc). This field determines the format of the
     * identifier string.
     */
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_artifact_targetRepository"))
    @NotNull
    @ManyToOne(cascade = CascadeType.REFRESH)
    private TargetRepository targetRepository;

    @Size(max = 255)
    private String filename;

    /**
     * Path to repository where the artifact file is available.
     */
    @Size(max = 500)
    @Column(length = 500)
    private String deployPath;

    /**
     * The record of the build which produced this artifact.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_artifact_buildrecord"))
    private BuildRecord buildRecord;

    /**
     * The list of builds which depend on this artifact. For example, if the build downloaded this artifact as a Maven
     * dependency.
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(mappedBy = "dependencies")
    private Set<BuildRecord> dependantBuildRecords;

    /**
     * The location from which this artifact was originally downloaded for import
     */
    @Size(max = 500)
    @Column(unique = false, length = 500)
    private String originUrl;

    /**
     * The date when this artifact was originally imported
     */
    private Date importDate;

    /**
     * The product milestone releases which distribute this artifact
     */
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @ManyToMany(mappedBy = "deliveredArtifacts")
    private Set<ProductMilestone> deliveredInProductMilestones;

    /**
     * User who created the artifact (either triggering the build or e.g. creating via Deliverable Analyzer)
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_artifact_creation_user"), updatable = false)
    private User creationUser;

    /**
     * User who last changed any audited field related to the Quality labels
     */
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_artifact_modification_user"), updatable = true)
    private User modificationUser;

    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date creationTime;

    @Audited
    @Column(columnDefinition = "timestamp with time zone")
    private Date modificationTime;

    /**
     * Reason for the setting of the Quality level
     */
    @Audited
    @Size(max = 200)
    @Column(length = 200)
    private String qualityLevelReason;

    @Transient
    public IdentifierSha256 getIdentifierSha256() {
        return new IdentifierSha256(identifier, sha256);
    }

    /**
     * Try to use the {@link Artifact.Builder} instead.
     *
     * Basic no-arg constructor. Initializes the buildRecords and dependantBuildRecords to empty set.
     */
    Artifact() {
        dependantBuildRecords = new HashSet<>();
        deliveredInProductMilestones = new HashSet<>();
        creationTime = Date.from(Instant.now());
        modificationTime = Date.from(Instant.now());
    }

    @PreRemove
    public void preRemove() {
        if (artifactQuality != ArtifactQuality.TEMPORARY && artifactQuality != ArtifactQuality.DELETED) {
            throw new PersistenceException(
                    "The non-temporary artifacts cannot be deleted! Only deletion of temporary artifacts is supported ");
        }
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    @Override
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
     * The identifier should contain different logic depending on the artifact type: i.e Maven should contain the GAV,
     * NPM and CocoaPOD should be identified differently
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

    public String getPurl() {
        return purl;
    }

    public void setPurl(String purl) {
        this.purl = purl;
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

    public ArtifactQuality getArtifactQuality() {
        return artifactQuality;
    }

    public void setArtifactQuality(ArtifactQuality artifactQuality) {
        this.artifactQuality = artifactQuality;
    }

    public BuildCategory getBuildCategory() {
        return buildCategory;
    }

    public void setBuildCategory(BuildCategory buildCategory) {
        this.buildCategory = buildCategory;
    }

    /**
     * Check if this artifact has an associated build record
     *
     * @return true if there is a build record for this artifact, false otherwise
     */
    public boolean isBuilt() {
        return buildRecord != null;
    }

    /**
     * Check if this artifact was imported from a remote URL
     *
     * @return true if there is an originUrl
     */
    public boolean isImported() {
        return (originUrl != null && !originUrl.isEmpty());
    }

    public boolean isTrusted() {
        return (isBuilt() || TargetRepository.isTrusted(originUrl, targetRepository));
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
     * Gets the build record which produced this artifact.
     *
     * @return the build record
     */
    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

    /**
     * Sets the build record which produced this artifact.
     *
     * @param buildRecord the build record
     */
    public void setBuildRecord(BuildRecord buildRecord) {
        if (this.buildRecord != null) {
            this.buildRecord.getBuiltArtifacts().remove(this);
        }
        if (buildRecord != null) {
            buildRecord.getBuiltArtifacts().add(this);
            // The user who produced the BuildRecord is saved as the creationUser and modificationUser of the Artifact
            this.creationUser = buildRecord.getUser();
            this.modificationUser = buildRecord.getUser();
        }
        this.buildRecord = buildRecord;
    }

    public Set<BuildRecord> getDependantBuildRecords() {
        return dependantBuildRecords;
    }

    public void setDependantBuildRecords(Set<BuildRecord> buildRecords) {
        this.dependantBuildRecords = buildRecords;
    }

    public void addDependantBuildRecord(BuildRecord buildRecord) {
        dependantBuildRecords.add(buildRecord);
        buildRecord.getDependencies().add(this);
    }

    public void removeDependantBuildRecord(BuildRecord buildRecord) {
        dependantBuildRecords.remove(buildRecord);
        buildRecord.getDependencies().remove(this);
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

    public Set<ProductMilestone> getDeliveredInProductMilestones() {
        return deliveredInProductMilestones;
    }

    public void setDeliveredInProductMilestones(Set<ProductMilestone> deliveredInProductMilestones) {
        this.deliveredInProductMilestones = deliveredInProductMilestones;
    }

    public boolean addDeliveredInProductMilestone(ProductMilestone productMilestone) {
        productMilestone.getDeliveredArtifacts().add(this);
        return deliveredInProductMilestones.add(productMilestone);
    }

    public boolean removeDeliveredInProductMilestone(ProductMilestone productMilestone) {
        productMilestone.getDeliveredArtifacts().remove(this);
        return deliveredInProductMilestones.remove(productMilestone);
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public TargetRepository getTargetRepository() {
        return targetRepository;
    }

    public void setTargetRepository(TargetRepository targetRepository) {
        this.targetRepository = targetRepository;
    }

    /**
     * @return the creationUser
     */
    public User getCreationUser() {
        return creationUser;
    }

    /**
     * @param creationUser The user who created this artifact
     */
    public void setCreationUser(User creationUser) {
        this.creationUser = creationUser;
    }

    /**
     * @return the modificationUser
     */
    public User getModificationUser() {
        return modificationUser;
    }

    /**
     * @param modificationUser The user who last modified the Quality label of this artifact
     */
    public void setModificationUser(User modificationUser) {
        this.modificationUser = modificationUser;
    }

    /**
     * @return the creationTime
     */
    public Date getCreationTime() {
        return creationTime;
    }

    /**
     * @param creationTime The time at which this artifact was created
     */
    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * @return the modificationTime
     */
    public Date getModificationTime() {
        return modificationTime;
    }

    /**
     * @param modificationTime The time at which the Quality label of this artifact was last modified
     */
    public void setModificationTime(Date modificationTime) {
        if (modificationTime != null) {
            this.modificationTime = modificationTime;
        }
    }

    /**
     * @return the qualityLevelReason
     */
    public String getQualityLevelReason() {
        return qualityLevelReason;
    }

    /**
     * @param qualityLevelReason The reason for the Quality level setting (change) of this artifact
     */
    public void setQualityLevelReason(String qualityLevelReason) {
        this.qualityLevelReason = StringUtils.nullIfBlank(qualityLevelReason);
    }

    @Override
    public String toString() {
        String tr = (targetRepository == null) ? "targetRepository=null" : targetRepository.toString();
        return "Artifact [id: " + id + ", identifier=" + identifier + ", artifactQuality=" + artifactQuality
                + ", buildCategory=" + buildCategory + ", " + tr + "]";
    }

    public String getDescriptiveString() {
        Integer trId = (targetRepository == null) ? null : targetRepository.getId();
        return String.format(
                "Identifier=%s, Sha256=%s, Target repository=%s, Deploy path=%s, Quality=%s",
                identifier,
                sha256,
                trId,
                deployPath,
                artifactQuality);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Artifact))
            return false;
        return id != null && id.equals(((Artifact) o).getId());
    }

    @Override
    public int hashCode() {
        // Because the id is generated when the entity is stored to DB, we need to have constant hash code to achieve
        // equals+hashCode consistency across all JPA object states
        return 31;
    }

    public static Builder builder() {
        return Builder.newBuilder();
    }

    public static class Builder {

        private Integer id;

        private String identifier;

        private String purl;

        private String md5;

        private String sha1;

        private String sha256;

        private Long size;

        private ArtifactQuality artifactQuality;

        private BuildCategory buildCategory;

        private TargetRepository targetRepository;

        private String filename;

        private String deployPath;

        private Set<BuildRecord> dependantBuildRecords;

        private BuildRecord buildRecord;

        private Set<ProductMilestone> deliveredInProductMilestones;

        private String originUrl;

        private Date importDate;

        private User creationUser;

        private User modificationUser;

        private Date creationTime;

        private Date modificationTime;

        private String qualityLevelReason;

        private Builder() {
            dependantBuildRecords = new HashSet<>();
            deliveredInProductMilestones = new HashSet<>();
        }

        public static Builder newBuilder() {
            return new Builder();
        }

        public Artifact build() {
            Artifact artifact = new Artifact();
            artifact.setId(id);
            artifact.setIdentifier(identifier);
            artifact.setPurl(purl);
            artifact.setMd5(md5);
            artifact.setSha1(sha1);
            artifact.setSha256(sha256);
            artifact.setSize(size);

            if (artifactQuality == null) {
                artifactQuality = ArtifactQuality.NEW;
            }
            artifact.setArtifactQuality(artifactQuality);

            if (buildCategory == null) {
                buildCategory = BuildCategory.STANDARD;
            }
            artifact.setBuildCategory(buildCategory);

            artifact.setTargetRepository(targetRepository);
            artifact.setFilename(filename);
            artifact.setDeployPath(deployPath);
            if (dependantBuildRecords != null) {
                artifact.setDependantBuildRecords(dependantBuildRecords);
            }
            artifact.setBuildRecord(buildRecord);
            artifact.setDeliveredInProductMilestones(deliveredInProductMilestones);
            artifact.setOriginUrl(originUrl);
            artifact.setImportDate(importDate);
            artifact.setCreationUser(creationUser);
            artifact.setModificationUser(modificationUser);
            artifact.setCreationTime(creationTime);
            artifact.setModificationTime(modificationTime);
            artifact.setQualityLevelReason(qualityLevelReason);

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

        public Builder purl(String purl) {
            this.purl = purl;
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

        public Builder artifactQuality(ArtifactQuality artifactQuality) {
            this.artifactQuality = artifactQuality;
            return this;
        }

        public Builder buildCategory(BuildCategory buildCategory) {
            this.buildCategory = buildCategory;
            return this;
        }

        public Builder targetRepository(TargetRepository targetRepository) {
            this.targetRepository = targetRepository;
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
            this.buildRecord = buildRecord;
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

        public Builder deliveredInProductMilestones(Set<ProductMilestone> deliveredInProductMilestones) {
            this.deliveredInProductMilestones = deliveredInProductMilestones;
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

        public Builder creationUser(User creationUser) {
            this.creationUser = creationUser;
            return this;
        }

        public Builder modificationUser(User modificationUser) {
            this.modificationUser = modificationUser;
            return this;
        }

        public Builder creationTime(Date creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder modificationTime(Date modificationTime) {
            this.modificationTime = modificationTime;
            return this;
        }

        public Builder qualityLevelReason(String qualityLevelReason) {
            this.qualityLevelReason = qualityLevelReason;
            return this;
        }

    }

    public static class IdentifierSha256 {
        private String identifier;
        private String sha256;

        public IdentifierSha256(String identifier, String sha256) {
            this.identifier = identifier;
            this.sha256 = sha256;
        }

        public String getSha256() {
            return sha256;
        }

        public String getIdentifier() {
            return identifier;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IdentifierSha256)) {
                return false;
            }

            IdentifierSha256 that = (IdentifierSha256) o;

            if (!identifier.equals(that.identifier)) {
                return false;
            }
            return sha256.equals(that.sha256);
        }

        @Override
        public int hashCode() {
            int result = identifier.hashCode();
            result = 31 * result + sha256.hashCode();
            return result;
        }
    }
}
