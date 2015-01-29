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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.BuildCollection;
import org.jboss.pnc.model.BuildDriverStatus;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.SystemImage;
import org.jboss.pnc.model.User;

/**
 * @author avibelli
 *
 */
public class BuildRecordBuilder {

    private Integer id;

    private String buildScript;

    private Timestamp startTime;

    private Timestamp endTime;

    private BuildConfiguration buildConfiguration;

    private User user;

    private String sourceUrl;

    private String buildLog;

    private BuildDriverStatus status;

    private List<Artifact> builtArtifacts;

    private List<Artifact> dependencies;

    private String buildDriverId;

    private SystemImage systemImage;

    private List<BuildCollection> buildCollections;

    public BuildRecordBuilder() {
        startTime = Timestamp.from(Instant.now());
        buildCollections = new ArrayList<>();
        dependencies = new ArrayList<>();
        builtArtifacts = new ArrayList<>();
    }

    public static BuildRecordBuilder newBuilder() {
        return new BuildRecordBuilder();
    }

    public BuildRecord build() {
        BuildRecord buildRecord = new BuildRecord();
        buildRecord.setId(id);
        buildRecord.setBuildScript(buildScript);
        buildRecord.setStartTime(startTime);
        buildRecord.setEndTime(endTime);
        buildRecord.setBuildConfiguration(buildConfiguration);
        buildRecord.setUser(user);
        buildRecord.setSourceUrl(sourceUrl);
        buildRecord.setBuildLog(buildLog);
        buildRecord.setStatus(status);
        buildRecord.setBuildDriverId(buildDriverId);
        buildRecord.setSystemImage(systemImage);

        // Set the bi-directional mapping
        for (Artifact artifact : builtArtifacts) {
            artifact.setBuildRecord(buildRecord);
        }
        buildRecord.setBuiltArtifacts(builtArtifacts);

        // Set the bi-directional mapping
        for (Artifact artifact : dependencies) {
            artifact.setBuildRecord(buildRecord);
        }
        buildRecord.setDependencies(dependencies);

        buildRecord.setBuildCollections(buildCollections);

        return buildRecord;
    }

    public BuildRecordBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public BuildRecordBuilder buildScript(String buildScript) {
        this.buildScript = buildScript;
        return this;
    }

    public BuildRecordBuilder startTime(Timestamp startTime) {
        this.startTime = startTime;
        return this;
    }

    public BuildRecordBuilder endTime(Timestamp endTime) {
        this.endTime = endTime;
        return this;
    }

    public BuildRecordBuilder buildConfiguration(BuildConfiguration buildConfiguration) {
        this.buildConfiguration = buildConfiguration;
        return this;
    }

    public BuildRecordBuilder user(User user) {
        this.user = user;
        return this;
    }

    public BuildRecordBuilder sourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public BuildRecordBuilder buildLog(String buildLog) {
        this.buildLog = buildLog;
        return this;
    }

    public BuildRecordBuilder status(BuildDriverStatus status) {
        this.status = status;
        return this;
    }

    public BuildRecordBuilder builtArtifact(Artifact builtArtifact) {
        this.builtArtifacts.add(builtArtifact);
        return this;
    }

    public BuildRecordBuilder builtArtifacts(List<Artifact> builtArtifacts) {
        this.builtArtifacts = builtArtifacts;
        return this;
    }

    public BuildRecordBuilder dependency(Artifact builtArtifact) {
        this.dependencies.add(builtArtifact);
        return this;
    }

    public BuildRecordBuilder dependencies(List<Artifact> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public BuildRecordBuilder buildDriverId(String buildDriverId) {
        this.buildDriverId = buildDriverId;
        return this;
    }

    public BuildRecordBuilder systemImage(SystemImage systemImage) {
        this.systemImage = systemImage;
        return this;
    }

    public BuildRecordBuilder buildCollections(List<BuildCollection> buildCollections) {
        this.buildCollections = buildCollections;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public BuildConfiguration getBuildConfiguration() {
        return buildConfiguration;
    }

    public User getUser() {
        return user;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getBuildLog() {
        return buildLog;
    }

    public BuildDriverStatus getStatus() {
        return status;
    }

    public List<Artifact> getBuiltArtifacts() {
        return builtArtifacts;
    }

    public List<Artifact> getDependencies() {
        return dependencies;
    }

    public String getBuildDriverId() {
        return buildDriverId;
    }

    public SystemImage getSystemImage() {
        return systemImage;
    }

    public List<BuildCollection> getBuildCollections() {
        return buildCollections;
    }

}
