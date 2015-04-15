package org.jboss.pnc.spi.environment;

import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;

/**
 * Environment, which has a single method to destroy it
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
public interface DestroyableEnvironmnet {

    /**
     * Destroys current running environment
     * 
     * @throws EnvironmentDriverException Thrown if any error occurs during destroying running environment
     */
    void destroyEnvironment() throws EnvironmentDriverException;
}
