package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

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
 * All the artifacts are mapped to the ProjectBuildResult, that are the results deriving from a ProjectBuildConfiguration, so
 * that given a build, the artifacts used can be tracked
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
    @GeneratedValue
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

    // bi-directional many-to-one association to BuildResult
    @ManyToOne
    private ProjectBuildResult projectBuildResult;

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
     * Gets the project build result.
     *
     * @return the project build result
     */
    public ProjectBuildResult getProjectBuildResult() {
        return projectBuildResult;
    }

    /**
     * Sets the project build result.
     *
     * @param projectBuildResult the new project build result
     */
    public void setProjectBuildResult(ProjectBuildResult projectBuildResult) {
        this.projectBuildResult = projectBuildResult;
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

}
