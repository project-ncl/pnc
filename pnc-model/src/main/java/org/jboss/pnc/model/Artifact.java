package org.jboss.pnc.model;

import java.io.Serializable;

import javax.persistence.*;

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

@Entity
@NamedQuery(name = "Artifact.findAll", query = "SELECT a FROM Artifact a")
public class Artifact implements Serializable {

    private static final long serialVersionUID = -2368833657284575734L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String identifier;

    private String checksum;

    private String filename;

    @Column(name = "deploy_url")
    private String deployUrl;

    @Enumerated(EnumType.STRING)
    private ArtifactStatus status;

    // bi-directional many-to-one association to BuildResult
    @ManyToOne
    @JoinColumn(name = "project_build_result_id")
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

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((checksum == null) ? 0 : checksum.hashCode());
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Artifact other = (Artifact) obj;
        if (checksum == null) {
            if (other.checksum != null)
                return false;
        } else if (!checksum.equals(other.checksum))
            return false;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Artifact [identifier=" + identifier + "]";
    }

}
