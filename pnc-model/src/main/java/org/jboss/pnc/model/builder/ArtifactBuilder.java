/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model.builder;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.ArtifactStatus;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.RepositoryType;

/**
 * @author avibelli
 *
 */
public class ArtifactBuilder {

    private Integer id;

    private String identifier;

    private RepositoryType repoType;

    private String checksum;

    private String filename;

    private String deployUrl;

    private ArtifactStatus status;

    private BuildRecord buildRecord;

    private ArtifactBuilder() {
    }

    public static ArtifactBuilder newBuilder() {
        return new ArtifactBuilder();
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

    public ArtifactBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ArtifactBuilder identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public ArtifactBuilder repoType(RepositoryType repoType) {
        this.repoType = repoType;
        return this;
    }

    public ArtifactBuilder checksum(String checksum) {
        this.checksum = checksum;
        return this;
    }

    public ArtifactBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }

    public ArtifactBuilder deployUrl(String deployUrl) {
        this.deployUrl = deployUrl;
        return this;
    }

    public ArtifactBuilder status(ArtifactStatus status) {
        this.status = status;
        return this;
    }

    public ArtifactBuilder buildRecord(BuildRecord buildRecord) {
        this.buildRecord = buildRecord;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public RepositoryType getRepoType() {
        return repoType;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getFilename() {
        return filename;
    }

    public String getDeployUrl() {
        return deployUrl;
    }

    public ArtifactStatus getStatus() {
        return status;
    }

    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

}
