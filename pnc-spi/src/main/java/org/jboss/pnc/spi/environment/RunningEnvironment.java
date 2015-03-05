package org.jboss.pnc.spi.environment;

import java.io.InputStream;
import java.io.Serializable;

import org.jboss.pnc.spi.environment.exception.EnvironmentDriverException;
import org.jboss.pnc.spi.repositorymanager.model.RepositoryConfiguration;

/**
 * Identification of environment started by environment driver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public interface RunningEnvironment extends Serializable {

    /**
     * Destroys current running environment
     * 
     * @throws EnvironmentDriverException Thrown if any error occurs during destroying running environment
     */
    void destroyEnvironment() throws EnvironmentDriverException;

    /**
     * Transfers data to the running environment. The data are saved to the file on path specified
     * as parameter.
     * 
     * @param pathOnHost Path in the target environment, where the data are passed
     * @param stream Data, which will be transfered to the target container
     * @throws EnvironmentDriverException Thrown if it the data transfer couldn't be finished.
     */
    void transferDataToEnvironment(String pathOnHost, InputStream stream) throws EnvironmentDriverException;

    /**
     * Transfers data to the running environment. The data are saved to the file on path specified
     * as parameter.
     * 
     * @param pathOnHost Path in the target environment, where the data are passed
     * @param data Data, which will be transfered to the target container
     * @throws EnvironmentDriverException Thrown if it the data transfer couldn't be finished.
     */
    void transferDataToEnvironment(String pathOnHost, String data) throws EnvironmentDriverException;

    /**
     * 
     * @return ID of an environment
     */
    String getId();

    /**
     * 
     * @return Port to connect to Jenkins UI
     */
    int getJenkinsPort();

    RepositoryConfiguration getRepositoryConfiguration();

}
