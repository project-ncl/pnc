package org.jboss.pnc.spi.environment;

import java.io.Serializable;

/**
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class RunningEnvironment implements Serializable{
    
    private final String id;
    
    private final int jenkinsPort;
    
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
