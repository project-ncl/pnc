/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.environment;

import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;

/**
 * Environment, which has a single method to destroy it
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface DestroyableEnvironment {

    /**
     * Destroys current running environment
     * 
     * @throws EnvironmentDriverException Thrown if any error occurs during destroying running environment
     */
    void destroyEnvironment() throws EnvironmentDriverException;
}
