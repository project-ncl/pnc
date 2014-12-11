package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ForeignKey;

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
@Table(name = "artifact")
public class Artifact implements Serializable {

    private static final long serialVersionUID = -2368833657284575734L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * TODO: Is this meant to be a Maven GAV e.g. use ProjectVersionRef [jdcasey] Non-maven repo artifacts might not conform to
     * GAV standard.
     */
    @Column(nullable = false)
    private String identifier;

    // The type of repository that hosts this artifact. This is also a sort of description for what type of artifatct this is
    // (maven, npm, etc.)
    @Column(nullable = false, name = "repository_type")
    @Enumerated(EnumType.STRING)
    private RepositoryType repositoryType;

    private String checksum;

    @Column(nullable = false, length = 100)
    private String filename;

    // What is this used for?
    @Column(name = "deploy_url")
    private String deployUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "artifact_status")
    private ArtifactStatus artifactStatus;

    @JoinColumn(name = "project_build_result_id")
    @ManyToOne
    @ForeignKey(name = "fk_artifact_project_build_result")
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
     * @return the artifactStatus
     */
    public ArtifactStatus getArtifactStatus() {
        return artifactStatus;
    }

    /**
     * @param artifactStatus the artifactStatus to set
     */
    public void setArtifactStatus(ArtifactStatus artifactStatus) {
        this.artifactStatus = artifactStatus;
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
     * @return the repositoryType
     */
    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    /**
     * @param repositoryType the repositoryType to set
     */
    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Artifact [identifier=" + identifier + "]";
    }

}
