/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.environment;

import java.io.Serializable;
import java.nio.file.Path;

import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

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
