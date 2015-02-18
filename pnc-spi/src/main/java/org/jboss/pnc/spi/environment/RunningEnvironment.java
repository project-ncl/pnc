package org.jboss.pnc.spi.environment;

import java.io.Serializable;

/**
 * Identification of environment started by environment driver
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public interface RunningEnvironment extends Serializable {
    
    /**
     * 
     * @return ID of an environment
     */
    public String getId();

    /**
     * 
     * @return Port to connect to Jenkins UI
     */
    public int getJenkinsPort();

}
