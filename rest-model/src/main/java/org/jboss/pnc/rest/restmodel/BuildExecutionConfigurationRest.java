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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.rest.utils.JsonOutputConverterMapper;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@XmlRootElement(name = "buildExecutionConfiguration")
public class BuildExecutionConfigurationRest implements BuildExecutionConfiguration {

    private int id;
    private String buildContentId;
    private UserRest user;
    private String buildScript;
    private String name;
    @Deprecated
    private String scmMirrorRepoURL;
    private String scmRepoURL;
    @Deprecated
    private String scmMirrorRevision;
    private String scmRevision;

    private BuildType buildType;

    public BuildExecutionConfigurationRest() {}

    public BuildExecutionConfigurationRest(String serialized) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BuildExecutionConfigurationRest buildExecutionConfigurationRestFromJson = mapper.readValue(serialized, BuildExecutionConfigurationRest.class);
        BuildExecutionConfiguration buildExecutionConfiguration = buildExecutionConfigurationRestFromJson.toBuildExecutionConfiguration();

        id = buildExecutionConfiguration.getId();
        buildContentId = buildExecutionConfiguration.getBuildContentId();
        buildScript = buildExecutionConfiguration.getBuildScript();
        name = buildExecutionConfiguration.getName();
        scmMirrorRepoURL = buildExecutionConfiguration.getScmMirrorRepoURL();
        scmRepoURL = buildExecutionConfiguration.getScmRepoURL();
        scmMirrorRevision = buildExecutionConfiguration.getScmMirrorRevision();
        scmRevision = buildExecutionConfiguration.getScmRevision();
        buildType = buildExecutionConfiguration.getBuildType();
        user = new UserRest(buildExecutionConfiguration.getUserId());

    }

    public BuildExecutionConfigurationRest(BuildExecutionConfiguration buildExecutionConfiguration) {
        id = buildExecutionConfiguration.getId();
        buildContentId = buildExecutionConfiguration.getBuildContentId();
        buildScript = buildExecutionConfiguration.getBuildScript();
        name = buildExecutionConfiguration.getName();
        scmMirrorRepoURL = buildExecutionConfiguration.getScmMirrorRepoURL();
        scmRepoURL = buildExecutionConfiguration.getScmRepoURL();
        scmMirrorRevision = buildExecutionConfiguration.getScmMirrorRevision();
        scmRevision = buildExecutionConfiguration.getScmRevision();
        buildType = buildExecutionConfiguration.getBuildType();
        user = new UserRest(buildExecutionConfiguration.getUserId());
    }

    public BuildExecutionConfiguration toBuildExecutionConfiguration() {
        return BuildExecutionConfiguration.build(
                id,
                buildContentId,
                user.getId(),
                buildScript,
                name,
                scmRepoURL,
                scmRevision,
                scmMirrorRepoURL,
                scmMirrorRevision,
                buildType
        );
    }

    public BuildConfigurationAuditedRest createBuildConfigurationAuditedRest() {
        BuildConfigurationAuditedRest buildConfigAuditedRest = new BuildConfigurationAuditedRest();
        buildConfigAuditedRest.setId(id);
        buildConfigAuditedRest.setRev(null);
        buildConfigAuditedRest.setName(name);
        buildConfigAuditedRest.setBuildScript(buildScript);
        buildConfigAuditedRest.setScmRepoURL(scmRepoURL);
        buildConfigAuditedRest.setScmRevision(scmRevision);
        buildConfigAuditedRest.setScmMirrorRepoURL(scmMirrorRepoURL);
        buildConfigAuditedRest.setScmMirrorRevision(scmMirrorRevision);
        return buildConfigAuditedRest;
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

    @Deprecated
    public void setScmMirrorRepoURL(String scmMirrorRepoURL) {
        this.scmMirrorRepoURL = scmMirrorRepoURL;
    }

    public void setScmRepoURL(String scmRepoURL) {
        this.scmRepoURL = scmRepoURL;
    }

    @Deprecated
    public void setScmMirrorRevision(String scmMirrorRevision) {
        this.scmMirrorRevision = scmMirrorRevision;
    }

    public void setScmRevision(String scmRevision) {
        this.scmRevision = scmRevision;
    }

    public int getId() {
        return id;
    }

    @JsonIgnore
    @Override
    public Integer getUserId() {
        return user.getId();
    }

    public String getBuildContentId() {
        return buildContentId;
    }

    public String getBuildScript() {
        return buildScript;
    }

    public String getName() {
        return name;
    }

    @Deprecated
    public String getScmMirrorRepoURL() {
        return scmMirrorRepoURL;
    }

    public String getScmRepoURL() {
        return scmRepoURL;
    }

    @Deprecated
    public String getScmMirrorRevision() {
        return scmMirrorRevision;
    }

    public String getScmRevision() {
        return scmRevision;
    }

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

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }
}
