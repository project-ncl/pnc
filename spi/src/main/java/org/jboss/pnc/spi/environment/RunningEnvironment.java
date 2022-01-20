/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.spi.environment;

import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

import java.io.Serializable;
import java.nio.file.Path;

/**
 * Identification of environment started by environment driver
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface RunningEnvironment extends Serializable, DestroyableEnvironment {

    /**
     * 
     * @return ID of an environment
     */
    String getId();

    /**
     * 
     * @return Port to connect to Jenkins UI
     */
    int getBuildAgentPort();

    /**
     * @return Jenkins URL in format IP:PORT
     */
    String getBuildAgentUrl();

    String getHost();

    String getInternalBuildAgentUrl();

    /**
     * @return Repository configuration related to the running environment
     */
    RepositorySession getRepositorySession();

    /**
     * @return Returns a build directory.
     */
    Path getWorkingDirectory();

    DebugData getDebugData();

    static RunningEnvironment createInstance(
            String id,
            int buildAgentPort,
            String host,
            String buildAgentUrl,
            String internalBuildAgentUrl,
            RepositorySession repositorySession,
            Path workingDirectory,
            Runnable destroyer,
            DebugData debugData) {

        return new RunningEnvironment() {
            @Override
            public String getId() {
                return id;
            }

            @Override
            public int getBuildAgentPort() {
                return buildAgentPort;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public String getBuildAgentUrl() {
                return buildAgentUrl;
            }

            @Override
            public String getInternalBuildAgentUrl() {
                return internalBuildAgentUrl;
            }

            @Override
            public RepositorySession getRepositorySession() {
                return repositorySession;
            }

            @Override
            public Path getWorkingDirectory() {
                return workingDirectory;
            }

            @Override
            public void destroyEnvironment() throws EnvironmentDriverException {
                destroyer.run();
            }

            @Override
            public DebugData getDebugData() {
                return debugData;
            }
        };
    }
}
