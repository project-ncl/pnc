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

import java.util.Date;

import org.jboss.pnc.enums.ArtifactQuality;

/**
 * The audited record of an artifact, to keep track of the changes to the Quality label. Each change to the quality
 * related fields in the artifact table is recorded in the audit table. This class serves to access the data of a
 * specific version of a build configuration. Keep in mind that it is not managed by JPA and needs to be filled
 * manually.
 *
 */
public class ArtifactAudited implements GenericEntity<Integer> {

    private static final long serialVersionUID = -1127405380682198904L;

    /**
     * The id of the artifact this record is associated with
     */
    private Integer id;

    /**
     * The table revision which identifies version of the artifact
     */
    private Integer rev;

    private IdRev idRev;

    private ArtifactQuality artifactQuality;

    private User creationUser;

    private User modificationUser;

    private Date creationTime;

    private Date modificationTime;

    private String reason;

    private Artifact artifact;

    public ArtifactAudited() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public IdRev getIdRev() {
        return idRev;
    }

    public void setIdRev(IdRev idRev) {
        this.idRev = idRev;
    }

    public ArtifactQuality getArtifactQuality() {
        return artifactQuality;
    }

    public void setArtifactQuality(ArtifactQuality artifactQuality) {
        this.artifactQuality = artifactQuality;
    }

    public User getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(User creationUser) {
        this.creationUser = creationUser;
    }

    public User getModificationUser() {
        return modificationUser;
    }

    public void setModificationUser(User modificationUser) {
        this.modificationUser = modificationUser;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public String toString() {
        return "ArtifactAudited [artifactQuality=" + artifactQuality + ", id=" + id + ", rev=" + rev + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ArtifactAudited that = (ArtifactAudited) o;

        return (idRev != null ? idRev.equals(that.idRev) : false);
    }

    @Override
    public int hashCode() {
        return idRev != null ? idRev.hashCode() : 0;
    }

    public static class Builder {
        private Artifact artifact;
        private Integer rev;

        public static Builder newBuilder() {
            return new Builder();
        }

        public ArtifactAudited build() {
            ArtifactAudited artifactAudited = new ArtifactAudited();
            artifactAudited.setId(artifact.getId());
            artifactAudited.setRev(rev);
            artifactAudited.setIdRev(new IdRev(artifact.getId(), rev));
            artifactAudited.setArtifactQuality(artifact.getArtifactQuality());
            artifactAudited.setCreationUser(artifact.getCreationUser());
            artifactAudited.setCreationTime(artifact.getCreationTime());
            artifactAudited.setModificationTime(artifact.getModificationTime());
            artifactAudited.setModificationUser(artifact.getModificationUser());
            artifactAudited.setReason(artifact.getReason());
            artifactAudited.artifact = artifact;
            return artifactAudited;
        }

        public Builder artifact(Artifact artifact) {
            this.artifact = artifact;
            return this;
        }

        public Builder rev(Integer rev) {
            this.rev = rev;
            return this;
        }

    }

    public static ArtifactAudited fromArtifact(Artifact artifact, Integer revision) {
        return ArtifactAudited.Builder.newBuilder().artifact(artifact).rev(revision).build();
    }
}
