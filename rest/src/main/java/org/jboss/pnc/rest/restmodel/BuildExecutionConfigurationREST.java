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

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationREST implements BuildExecutionConfiguration {

    private int id;
    private String buildContentId;
    private UserRest user;
    private String buildScript;
    private String name;
    private String scmMirrorRepoURL;
    private String scmRepoURL;
    private String scmMirrorRevision;
    private String scmRevision;
    private BuildType buildType;

    public BuildExecutionConfigurationREST() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBuildContentId(String buildContentId) {
        this.buildContentId = buildContentId;
    }

    public void setBuildScript(String buildScript) {
        this.buildScript = buildScript;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setScmMirrorRepoURL(String scmMirrorRepoURL) {
        this.scmMirrorRepoURL = scmMirrorRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    public void setScmMirrorRevision(String scmMirrorRevision) {
        this.scmMirrorRevision = scmMirrorRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public Integer getUserId() {
        return user.getId();
    }

    @Override
    public String getBuildContentId() {
        return buildContentId;
    }

    @Override
    public String getBuildScript() {
        return buildScript;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getScmMirrorRepoURL() {
        return scmMirrorRepoURL;
    }

    @Override
    public String getScmRepoURL() {
        return scmRepoURL;
    }

    @Override
    public String getScmMirrorRevision() {
        return scmMirrorRevision;
    }

    @Override
    public String getScmRevision() {
        return scmRevision;
    }

    @Override
    public BuildType getBuildType() {
        return buildType;
    }

    public UserRest getUser() {
        return user;
    }

    public void setUser(UserRest user) {
        this.user = user;
    }

    public void setBuildType(BuildType buildType) {
        this.buildType = buildType;
    }
}
