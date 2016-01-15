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

package org.jboss.pnc.spi.executor;

import org.jboss.pnc.model.BuildType;
import org.jboss.pnc.spi.repositorymanager.BuildExecution;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface BuildExecutionConfiguration extends BuildExecution {

    int getId();

    Integer getUserId();

    String getBuildScript();

    String getName();  //TODO used to be buildConfigurationName

    @Deprecated  //TODO determine this before calling the execution
    String getScmMirrorRepoURL();

    String getScmRepoURL();

    @Deprecated //TODO determine this before calling the execution
    String getScmMirrorRevision();

    String getScmRevision();

    BuildType getBuildType();

    static BuildExecutionConfiguration build(
            int id,
            String buildContentId,
            Integer userId,
            String buildScript,
            String name,
            String scmRepoURL,
            String scmRevision,
            String scmMirrorRepoURL,
            String scmMirrorRevision,
            BuildType buildType) {
        return new BuildExecutionConfiguration() {

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
        };
    }

}
