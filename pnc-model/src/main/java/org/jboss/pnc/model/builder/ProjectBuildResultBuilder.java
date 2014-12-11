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
import org.jboss.pnc.model.BuildStatus;
import org.jboss.pnc.model.ProjectBuildConfiguration;
import org.jboss.pnc.model.ProjectBuildResult;
import org.jboss.pnc.model.SystemImage;
import org.jboss.pnc.model.User;

/**
 * @author avibelli
 *
 */
public class ProjectBuildResultBuilder {

    private Integer id;

    private String buildScript;

    private Timestamp startTime;

    private Timestamp endTime;

    private ProjectBuildConfiguration projectBuildConfiguration;

    private User user;

    private String sourceUrl;

    private String patchesUrl;

    private String buildLog;

    private BuildStatus status;

    private List<Artifact> builtArtifacts;

    private List<Artifact> dependencies;

    private String buildDriverId;

    private SystemImage systemImage;

    private List<BuildCollection> buildCollections;

    public ProjectBuildResultBuilder() {
        startTime = Timestamp.from(Instant.now());
        buildCollections = new ArrayList<>();
        dependencies = new ArrayList<>();
        builtArtifacts = new ArrayList<>();
    }

    public static ProjectBuildResultBuilder newBuilder() {
        return new ProjectBuildResultBuilder();
    }

    public ProjectBuildResult build() {
        ProjectBuildResult projectBuildResult = new ProjectBuildResult();
        projectBuildResult.setId(id);
        projectBuildResult.setBuildScript(buildScript);
        projectBuildResult.setStartTime(startTime);
        projectBuildResult.setEndTime(endTime);
        projectBuildResult.setProjectBuildConfiguration(projectBuildConfiguration);
        projectBuildResult.setUser(user);
        projectBuildResult.setSourceUrl(sourceUrl);
        projectBuildResult.setPatchesUrl(patchesUrl);
        projectBuildResult.setBuildLog(buildLog);
        projectBuildResult.setBuildStatus(status);
        projectBuildResult.setBuildDriverId(buildDriverId);
        projectBuildResult.setSystemImage(systemImage);

        // Set the bi-directional mapping
        for (Artifact artifact : builtArtifacts) {
            artifact.setProjectBuildResult(projectBuildResult);
        }
        projectBuildResult.setBuiltArtifact(builtArtifacts);

        // Set the bi-directional mapping
        for (Artifact artifact : dependencies) {
            artifact.setProjectBuildResult(projectBuildResult);
        }
        projectBuildResult.setDependency(dependencies);

        projectBuildResult.setBuildCollection(buildCollections);

        return projectBuildResult;
    }

    public ProjectBuildResultBuilder id(Integer id) {
        this.id = id;
        return this;
    }

    public ProjectBuildResultBuilder buildScript(String buildScript) {
        this.buildScript = buildScript;
        return this;
    }

    public ProjectBuildResultBuilder startTime(Timestamp startTime) {
        this.startTime = startTime;
        return this;
    }

    public ProjectBuildResultBuilder endTime(Timestamp endTime) {
        this.endTime = endTime;
        return this;
    }

    public ProjectBuildResultBuilder projectBuildConfiguration(ProjectBuildConfiguration projectBuildConfiguration) {
        this.projectBuildConfiguration = projectBuildConfiguration;
        return this;
    }

    public ProjectBuildResultBuilder user(User user) {
        this.user = user;
        return this;
    }

    public ProjectBuildResultBuilder sourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        return this;
    }

    public ProjectBuildResultBuilder patchesUrl(String patchesUrl) {
        this.patchesUrl = patchesUrl;
        return this;
    }

    public ProjectBuildResultBuilder buildLog(String buildLog) {
        this.buildLog = buildLog;
        return this;
    }

    public ProjectBuildResultBuilder status(BuildStatus status) {
        this.status = status;
        return this;
    }

    public ProjectBuildResultBuilder builtArtifact(Artifact builtArtifact) {
        this.builtArtifacts.add(builtArtifact);
        return this;
    }

    public ProjectBuildResultBuilder builtArtifacts(List<Artifact> builtArtifacts) {
        this.builtArtifacts = builtArtifacts;
        return this;
    }

    public ProjectBuildResultBuilder dependency(Artifact builtArtifact) {
        this.dependencies.add(builtArtifact);
        return this;
    }

    public ProjectBuildResultBuilder dependencies(List<Artifact> dependencies) {
        this.dependencies = dependencies;
        return this;
    }

    public ProjectBuildResultBuilder buildDriverId(String buildDriverId) {
        this.buildDriverId = buildDriverId;
        return this;
    }

    public ProjectBuildResultBuilder systemImage(SystemImage systemImage) {
        this.systemImage = systemImage;
        return this;
    }

    public ProjectBuildResultBuilder buildCollections(List<BuildCollection> buildCollections) {
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

    public ProjectBuildConfiguration getProjectBuildConfiguration() {
        return projectBuildConfiguration;
    }

    public User getUser() {
        return user;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getPatchesUrl() {
        return patchesUrl;
    }

    public String getBuildLog() {
        return buildLog;
    }

    public BuildStatus getStatus() {
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
