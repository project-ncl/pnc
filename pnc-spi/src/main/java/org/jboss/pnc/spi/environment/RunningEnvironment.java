package org.jboss.pnc.spi.environment;

import java.io.Serializable;

/**
 * Identification of environment started by environment driver  
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class RunningEnvironment implements Serializable{
    
    /**
     * ID of environment
     */
    private final String id;
    
    /**
     * Port to communicate with Jenkins
     */
    private final int jenkinsPort;
    
    /**
     * Port to SSH to running environment
     */
    private final int sshPort;

    public RunningEnvironment(String id, int jenkinsPort, int sshPort) {
        super();
        this.id = id;
        this.jenkinsPort = jenkinsPort;
        this.sshPort = sshPort;
    }


    public String getId() {
        return id;
    }

    
    public int getJenkinsPort() {
        return jenkinsPort;
    }

    
    public int getSshPort() {
        return sshPort;
    }
}
