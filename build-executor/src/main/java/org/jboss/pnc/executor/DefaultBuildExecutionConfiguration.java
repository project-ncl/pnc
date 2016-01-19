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

package org.jboss.pnc.executor;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultBuildExecutionConfiguration implements BuildExecutionConfiguration {

    private final int id;
    private final String buildContentId;
    private final Integer userId;
    private final String buildScript;
    private final String name;
    private final String scmMirrorRepoURL;
    private final String scmRepoURL;
    private final String scmMirrorRevision;
    private final String scmRevision;
    private final BuildType buildType;

    public DefaultBuildExecutionConfiguration(
            int id,
            String buildContentId,
            Integer userId,
            String buildScript,
            String name,
            String scmMirrorRepoURL,
            String scmRepoURL,
            String scmMirrorRevision,
            String scmRevision, BuildType buildType) {
        this.id = id;

        this.buildContentId = buildContentId;
        this.userId = userId;
        this.buildScript = buildScript;
        this.name = name;
        this.scmMirrorRepoURL = scmMirrorRepoURL;
        this.scmRepoURL = scmRepoURL;
        this.scmMirrorRevision = scmMirrorRevision;
        this.scmRevision = scmRevision;
        this.buildType = buildType;
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
    public Integer getUserId() {
        return userId;
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
}
