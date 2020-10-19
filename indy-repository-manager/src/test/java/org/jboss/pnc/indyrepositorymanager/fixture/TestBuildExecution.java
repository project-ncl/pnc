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
package org.jboss.pnc.indyrepositorymanager.fixture;

import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.spi.repositorymanager.ArtifactRepository;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;

import java.util.List;

public class TestBuildExecution implements BuildExecution {

    private int id = 1;

    private String buildContentId;

    private List<ArtifactRepository> artifactRepositories;

    private BuildType buildType;

    public TestBuildExecution(String buildId) {
        this(buildId, BuildType.MVN);
    }

    public TestBuildExecution(String buildId, BuildType buildType) {
        this.buildContentId = buildId;
        this.buildType = buildType;
    }

    public TestBuildExecution() {
        this("build+myproject+12345");
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    @Override
    public List<ArtifactRepository> getArtifactRepositories() {
        return artifactRepositories;
    }

    public void setArtifactRepositories(List<ArtifactRepository> artifactRepositories) {
        this.artifactRepositories = artifactRepositories;
    }

    @Override
    public boolean isTempBuild() {
        return false;
    }

    @Override
    public String getTempBuildTimestamp() {
        return null;
    }

    @Override
    public BuildType getBuildType() {
        return buildType;
    }

}
