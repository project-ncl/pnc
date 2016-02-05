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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * Class that maps the artifacts used by the builds of the projects.
 * 
 * Different types of artifacts should provide different logic for the identifier field.
 * 
 * The status indicates the genesis of the artifact, whether it has been imported from external repositories, or built
 * internally.
 * 
 * All the artifacts are mapped to the BuildRecord, that are the results deriving from a BuildConfiguration, so that given a
 * build, the artifacts used can be tracked
 * 
 * 
 * (identifier + checksum) should be unique
 */
@Entity
public class Artifact implements GenericEntity<Integer> {

    private static final long serialVersionUID = -2368833657284575734L;
    public static final String SEQUENCE_NAME = "artifact_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Deprecated
    private String identifier;

    // The type of repository that hosts this artifact. This is also a sort of description for what type of artifatct this is
    // (maven, npm, etc.)
    @Deprecated
    private RepositoryType repoType;

    private String checksum;

    private String filename;

    @Deprecated
    private String deployUrl;

    @Enumerated(EnumType.STRING)
    private ArtifactStatus status;

    /**
     * The builds for which this artifact is a dependency
     */
    @NotNull
    @ManyToMany
    @ForeignKey(name = "fk_artifact_buildrecord")
    @Index(name="idx_artifact_buildrecord")
    private Set<BuildRecord> dependantBuildRecords;

    /**
     * Instantiates a new artifact.
     */
    public Artifact() {
        dependantBuildRecords = new HashSet<BuildRecord>();
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
    @Deprecated
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the identifier.
     *
     * @param identifier the new identifier
     */
    @Deprecated
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
    @Deprecated
    public String getDeployUrl() {
        return deployUrl;
    }

    /**
     * Sets the deploy url.
     *
     * @param deployUrl the new deploy url
     */
    @Deprecated
    public void setDeployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
    }

    /**
     * Gets the status.
     * 
     * The status (the genesis of the artifact, whether it has been imported or built internally).
     *
     * @return the status
     */
    public ArtifactStatus getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param status the new status
     */
    public void setStatus(ArtifactStatus status) {
        this.status = status;
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
    @Deprecated
    public RepositoryType getRepoType() {
        return repoType;
    }

    /**
     * @param repoType the repoType to set
     */
    @Deprecated
    public void setRepoType(RepositoryType repoType) {
        this.repoType = repoType;
    }

    @Override
    public String toString() {
        return "Artifact [id: " + id + ", filename=" + filename + "]";
    }

    public static class Builder {

        private Integer id;

        private String identifier;

        private RepositoryType repoType;

        private String checksum;

        private String filename;

        private String deployUrl;

        private ArtifactStatus status;

        private Set<BuildRecord> dependantBuildRecords;

        public static Builder newBuilder() {
            return new Builder();
        }

        public Artifact build() {
            Artifact artifact = new Artifact();
            artifact.setId(id);
            artifact.setIdentifier(identifier);
            artifact.setRepoType(repoType);
            artifact.setChecksum(checksum);
            artifact.setFilename(filename);
            artifact.setDeployUrl(deployUrl);
            artifact.setStatus(status);
            if (dependantBuildRecords != null) {
                artifact.setDependantBuildRecords(dependantBuildRecords);
            }
            return artifact;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        @Deprecated
        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        @Deprecated
        public Builder repoType(RepositoryType repoType) {
            this.repoType = repoType;
            return this;
        }

        public Builder checksum(String checksum) {
            this.checksum = checksum;
            return this;
        }

        public Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        @Deprecated
        public Builder deployUrl(String deployUrl) {
            this.deployUrl = deployUrl;
            return this;
        }

        public Builder status(ArtifactStatus status) {
            this.status = status;
            return this;
        }

        public Builder dependantBuildRecords(Set<BuildRecord> dependantBuildRecords) {
            this.dependantBuildRecords = dependantBuildRecords;
            return this;
        }

    }
}
