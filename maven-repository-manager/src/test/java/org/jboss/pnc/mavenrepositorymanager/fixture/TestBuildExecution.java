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
package org.jboss.pnc.mavenrepositorymanager.fixture;

import org.jboss.pnc.spi.repositorymanager.BuildExecution;

import java.net.URI;

public class TestBuildExecution implements BuildExecution {

    private int id = 1;

    private String topContentId;

    private String buildSetContentId;

    private String buildContentId;

    private String projectName = "my project";

    private boolean isSetBuild;

    private URI logsWebSocketLink;

    public TestBuildExecution(String topId, String setId, String buildId, boolean isSetBuild) {
        this.topContentId = topId;
        this.buildSetContentId = setId;
        this.buildContentId = buildId;
        this.isSetBuild = isSetBuild;
    }

    public TestBuildExecution() {
        this("product+myproduct+1-0", null, "build+myproject+12345", false);
    }

    public int getId() {
        return id;
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    public void setTopContentId(String topContentId) {
        this.topContentId = topContentId;
    }

    public void setBuildSetContentId(String buildSetContentId) {
        this.buildSetContentId = buildSetContentId;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

}
