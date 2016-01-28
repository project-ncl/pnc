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

package org.jboss.pnc.mock.executor;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionConfigurationMock implements BuildExecutionConfiguration {

    private int id;
    private String buildContentId;
    private Integer userId;
    private String buildScript;
    private String name;
    private String scmMirrorRepoURL;
    private String scmRepoURL;
    private String scmMirrorRevision;
    private String scmRevision;
    private BuildType buildType;

    public static BuildExecutionConfiguration mockConfig() {
        BuildExecutionConfigurationMock mock = new BuildExecutionConfigurationMock();
        mock.setId(1);
        mock.setBuildScript("mvn install");
        mock.setScmRepoURL("http://www.github.com");
        mock.setScmRevision("1234567890");
        return mock;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScmMirrorRepoURL() {
        return scmMirrorRepoURL;
    }

    public void setScmMirrorRepoURL(String scmMirrorRepoURL) {
        this.scmMirrorRepoURL = scmMirrorRepoURL;
    }

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public String getScmMirrorRevision() {
        return scmMirrorRevision;
    }

    public void setScmMirrorRevision(String scmMirrorRevision) {
        this.scmMirrorRevision = scmMirrorRevision;
    }

    public String getScmRevision() {
        return scmRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public BuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }

}
