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

import io.swagger.annotations.ApiModelProperty;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.RepositoryType;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;

import static org.jboss.pnc.rest.utils.StreamHelper.nullableStreamOf;

import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@XmlRootElement(name = "Artifact")
public class ArtifactRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private String identifier;

    @ApiModelProperty(dataType = "string")
    private RepositoryType repoType;

    private String checksum;

    private String filename;

    private String deployUrl;

    private Set<Integer> buildRecordIds;

    private Set<Integer> dependantBuildRecordIds;

    private boolean imported;

    private Date downloadDate;

    private String originUrl;

    public ArtifactRest() {
    }

    public ArtifactRest(Artifact artifact) {
        this.id = artifact.getId();
        this.identifier = artifact.getIdentifier();
        this.repoType = artifact.getRepoType();
        this.checksum = artifact.getChecksum();
        this.filename = artifact.getFilename();
        this.deployUrl = artifact.getDeployUrl();
        this.imported = artifact.getImported();
        this.downloadDate = artifact.getDownloadDate();
        this.originUrl = artifact.getOriginUrl();
        this.buildRecordIds = nullableStreamOf(artifact.getBuildRecords())
                .map(build -> build.getId()).collect(Collectors.toSet());
        this.dependantBuildRecordIds = nullableStreamOf(artifact.getDependantBuildRecords())
                .map(depBuild -> depBuild.getId()).collect(Collectors.toSet());
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

    public boolean getImported() {
        return imported;
    }

    public void setImported(boolean imported) {
        this.imported = imported;
    }

    public Date getDonwloadDate() {
        return downloadDate;
    }

    public void setDownloadDate(Date downloadDate) {
        this.downloadDate = downloadDate;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    @Deprecated
    public String getStatus() {
        if (imported) {
            return "BINARY_IMPORTED";
        }
        return "BINARY_BUILT";
    }

    public Set<Integer> getBuildRecordIds() {
        return buildRecordIds;
    }

    public void setBuildRecordIds(Set<Integer> buildRecordIds) {
        this.buildRecordIds = buildRecordIds;
    }

    public Set<Integer> getDependantBuildRecordIds() {
        return dependantBuildRecordIds;
    }

    public void setDependantBuildRecordIds(Set<Integer> dependantBuildRecordIds) {
        this.dependantBuildRecordIds = dependantBuildRecordIds;
    }

}
