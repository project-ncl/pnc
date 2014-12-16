package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.RepositoryType;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Artifact")
public class ArtifactRest {

    private Integer id;

    private String identifier;

    private RepositoryType repoType;

    private String checksum;

    private String filename;

    // What is this used for?
    private String deployUrl;

    private ArtifactStatus status;

    public ArtifactRest() {
    }

    public ArtifactRest(Artifact artifact) {
        this.id = artifact.getId();
        this.identifier = artifact.getIdentifier();
        this.repoType = artifact.getRepoType();
        this.checksum = artifact.getChecksum();
        this.filename = artifact.getFilename();
        this.deployUrl = artifact.getDeployUrl();
        this.status = artifact.getStatus();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public RepositoryType getRepoType() {
        return repoType;
    }

    public void setRepoType(RepositoryType repoType) {
        this.repoType = repoType;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDeployUrl() {
        return deployUrl;
    }

    public void setDeployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
    }

    public ArtifactStatus getStatus() {
        return status;
    }

    public void setStatus(ArtifactStatus status) {
        this.status = status;
    }
}
