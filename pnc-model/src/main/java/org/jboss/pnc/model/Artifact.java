package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

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
// TODO: We need to capture two types of artifact:
// 1. Build output, which has an associated build result
// 2. Import, which has an origin repository that we probably need to track
//
// Ordinarily, I'd model this as a common base class and two subclasses to capture the variant info.
// I'm not sure how it would need to be modeled for efficient storage via JPA.
@Entity
public class Artifact implements Serializable {

    private static final long serialVersionUID = -2368833657284575734L;

    @Id
    @SequenceGenerator(name="artifact_id_seq", sequenceName="artifact_id_seq", allocationSize=1)    
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="artifact_id_seq")
    private Integer id;

    /**
     * TODO: Is this meant to be a Maven GAV e.g. use ProjectVersionRef [jdcasey] Non-maven repo artifacts might not conform to
     * GAV standard.
     */
    private String identifier;

    // The type of repository that hosts this artifact. This is also a sort of description for what type of artifatct this is
    // (maven, npm, etc.)
    private RepositoryType repoType;

    private String checksum;

    private String filename;

    // What is this used for?
    private String deployUrl;

    @Enumerated(EnumType.STRING)
    private ArtifactStatus status;

    // bi-directional many-to-one association to buildRecord
    @ManyToOne
    private BuildRecord buildRecord;

    /**
     * Instantiates a new artifact.
     */
    public Artifact() {
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

    /**
     * Gets the project build record.
     *
     * @return the project build record
     */
    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

    /**
     * Sets the project build record.
     *
     * @param buildRecord the new project build record
     */
    public void setBuildRecord(BuildRecord buildRecord) {
        this.buildRecord = buildRecord;
    }

    /**
     * @return the repoType
     */
    public RepositoryType getRepoType() {
        return repoType;
    }

    /**
     * @param repoType the repoType to set
     */
    public void setRepoType(RepositoryType repoType) {
        this.repoType = repoType;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Artifact [identifier=" + identifier + "]";
    }

    public static class Builder {

        private Integer id;

        private String identifier;

        private RepositoryType repoType;

        private String checksum;

        private String filename;

        private String deployUrl;

        private ArtifactStatus status;

        private BuildRecord buildRecord;

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
            artifact.setBuildRecord(buildRecord);

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

        public Builder deployUrl(String deployUrl) {
            this.deployUrl = deployUrl;
            return this;
        }

        public Builder status(ArtifactStatus status) {
            this.status = status;
            return this;
        }

        public Builder buildRecord(BuildRecord buildRecord) {
            this.buildRecord = buildRecord;
            return this;
        }

    }
}
