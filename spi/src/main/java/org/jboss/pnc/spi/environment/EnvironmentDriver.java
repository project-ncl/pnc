/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.environment;

import java.util.Map;

import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.builddriver.DebugData;
import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositorySession;

/**
 * SPI interface for Environment driver, which provides support to control different target environments.
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface EnvironmentDriver {

    /**
     * Creates and starts new clean environment.
     * 
     * @param systemImageId The unique identifier or checksum of the build system image
     * @param systemImageRepositoryUrl The URL containing the system image
     * @param systemImageType The type of image to be initialized
     * @param repositorySession Configuration of repository to store built artifacts
     *
     * @param tempBuild
     * @return New started environment in initialization phase
     * @throws EnvironmentDriverException Thrown if any error occurs during starting new environment
     */
    StartedEnvironment startEnvironment(
            String systemImageId,
            String systemImageRepositoryUrl,
            SystemImageType systemImageType,
            RepositorySession repositorySession,
            DebugData debugData,
            String accessToken,
            boolean tempBuild,
            Map<String, String> parameters) throws EnvironmentDriverException;

    /**
     * Test if selected driver can build requested environment
     * 
     * @param buildType The type of image to be used to initialize the build environment
     * @return True, if selected driver can instantiate an image of the given type, otherwise false.
     */
    boolean canRunImageType(SystemImageType buildType);

}
