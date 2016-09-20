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
package org.jboss.pnc.rest.restmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactRepo;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

@XmlRootElement(name = "Artifact")
public class ArtifactRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    private String identifier;

    @ApiModelProperty(dataType = "string")
    private Artifact.Quality artifactQuality;

    @ApiModelProperty(dataType = "string")
    private ArtifactRepo.Type repoType;

    @Getter
    @Setter
    private String md5;

    @Getter
    @Setter
    private String sha1;

    @Getter
    @Setter
    private String sha256;

    private String filename;

    private String deployUrl;

    private Set<Integer> buildRecordIds;

    private Set<Integer> dependantBuildRecordIds;

    private Date importDate;

    private String originUrl;

    @Getter
    @Setter
    private Long size;

    public ArtifactRest() {
    }

    public ArtifactRest(Artifact artifact) {
        this.id = artifact.getId();
        this.identifier = artifact.getIdentifier();
        this.repoType = artifact.getRepoType();
        this.md5 = artifact.getMd5();
        this.sha1= artifact.getSha1();
        this.sha256= artifact.getSha256();
        this.filename = artifact.getFilename();
        this.deployUrl = artifact.getDeployUrl();
        this.artifactQuality = artifact.getArtifactQuality();
        this.importDate = artifact.getImportDate();
        this.originUrl = artifact.getOriginUrl();
        this.buildRecordIds = nullableStreamOf(artifact.getBuildRecords())
                .map(BuildRecord::getId).collect(Collectors.toSet());
        this.dependantBuildRecordIds = nullableStreamOf(artifact.getDependantBuildRecords())
                .map(BuildRecord::getId).collect(Collectors.toSet());
        this.size = artifact.getSize();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public ArtifactRepo.Type getRepoType() {
        return repoType;
    }

    public void setRepoType(ArtifactRepo.Type repoType) {
        this.repoType = repoType;
    }

    public Artifact.Quality getArtifactQuality() {
        return artifactQuality;
    }

    public void setArtifactQuality(Artifact.Quality artifactQuality) {
        this.artifactQuality = artifactQuality;
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

    public Date getImportDate() {
        return importDate;
    }

    public void setImportDate(Date importDate) {
        this.importDate = importDate;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    @JsonIgnore
    public boolean isImported() {
        return (originUrl != null && !originUrl.isEmpty());
    }

    @Deprecated
    @JsonIgnore
    public String getStatus() {
        if (buildRecordIds != null && buildRecordIds.size() > 0) {
            return "BINARY_BUILT";
        }
        return "BINARY_IMPORTED";
    }

    public Set<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }

    public void setBuildRecordIds(Set<Integer> buildRecordIds) {
        this.buildRecordIds = buildRecordIds;
    }

    @JsonIgnore
    public boolean isBuilt() {
        return (buildRecordIds != null && buildRecordIds.size() > 0);
    }

    public Set<Integer> getDependantBuildRecordIds() {
        return dependantBuildRecordIds;
    }

    public void setDependantBuildRecordIds(Set<Integer> dependantBuildRecordIds) {
        this.dependantBuildRecordIds = dependantBuildRecordIds;
    }

    public Artifact.Builder toDBEntityBuilder() {
        Artifact.Builder builder = Artifact.Builder.newBuilder()
                .id(this.getId())
                .identifier(this.getIdentifier())
                .md5(this.getMd5())
                .sha1(this.getSha1())
                .sha256(this.getSha256())
                .size(this.getSize())
                .repoType(this.getRepoType())
                .artifactQuality(this.getArtifactQuality())
                .deployUrl(this.getDeployUrl())
                .importDate(this.getImportDate())
                .originUrl(this.getOriginUrl())
                .filename(this.getFilename());

        nullableStreamOf(this.getBuildRecordIds()).forEach(buildRecordId -> {
            builder.buildRecord(BuildRecord.Builder.newBuilder().id(buildRecordId).build());
        });
        nullableStreamOf(this.getDependantBuildRecordIds()).forEach(depBuildRecordId -> {
            builder.dependantBuildRecord(BuildRecord.Builder.newBuilder().id(depBuildRecordId).build());
        });

        return builder;
    }

    @Override
    public String toString() {
        return "ArtifactRest{" +
                "id=" + id +
                ", identifier='" + identifier + '\'' +
                ", artifactQuality=" + artifactQuality +
                ", repoType=" + repoType +
                ", md5='" + md5 + '\'' +
                ", sha1='" + sha1 + '\'' +
                ", sha256='" + sha256 + '\'' +
                ", filename='" + filename + '\'' +
                ", deployUrl='" + deployUrl + '\'' +
                ", buildRecordIds=" + buildRecordIds +
                ", dependantBuildRecordIds=" + dependantBuildRecordIds +
                ", importDate=" + importDate +
                ", originUrl='" + originUrl + '\'' +
                '}';
    }
}
