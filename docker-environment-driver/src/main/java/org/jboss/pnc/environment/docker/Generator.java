package org.jboss.pnc.environment.docker;

import javax.ejb.Singleton;

/**
 * Generates container IDs, jenkins ports and SSH ports for Docker containers.
 * The sequence repeats after every 10^4 generated unique values
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
@Singleton
public class Generator {

    private static final int MAX_JENKINS_PORT = 29999;

    private static final int MIN_JENKINS_PORT = 20000;

    private static final int MAX_SSH_PORT = 39999;

    private static final int MIN_SSH_PORT = 30000;

    private static final String PREFIX = "PNC-jenkins-container-";

    private int suffix = 0;
    
    private int latestJenkinsPort = MIN_JENKINS_PORT - 1;
    
    private int latestSshPort = MIN_SSH_PORT - 1;


    /**
     * @return New unique container id
     */
    public String generateContainerId() {
        if (suffix > 10000)
            suffix = 0;
        suffix++;

        return PREFIX + suffix;
    }
    
    /**
     * 
     * @return New port for Jenkins
     */
    public int generateJenkinsPort(){
        if(latestJenkinsPort >= MAX_JENKINS_PORT)
            latestJenkinsPort = MIN_JENKINS_PORT - 1;
        latestJenkinsPort++;
        
        return latestJenkinsPort;
    }
    
    /**
     * 
     * @return New port for SSH
     */
    public int generateSshPort(){
        if(latestSshPort >= MAX_SSH_PORT)
            latestSshPort = MIN_SSH_PORT - 1;
        latestSshPort++;
        
        return latestSshPort;
    }
}
