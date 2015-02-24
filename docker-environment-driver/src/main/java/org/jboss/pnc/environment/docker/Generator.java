package org.jboss.pnc.environment.docker;

import javax.annotation.PostConstruct;
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
    
    private static final String DEFAULT_CONTAINER_ID = "PNC-jenkins-container-";

    private String containerIdPrefix ;

    private int latestContainerIdSuffix;

    private int latestJenkinsPort;

    private int latestSshPort;
    
    /**
     * Process initialization of fields based on static constants
     */
    @PostConstruct
    private void init() {
        this.latestJenkinsPort = MIN_JENKINS_PORT - 1; 
        this.latestSshPort = MIN_SSH_PORT - 1;
        this.latestContainerIdSuffix = 0;
        this.containerIdPrefix = DEFAULT_CONTAINER_ID;
    }

    /**
     * @return New unique container id
     */
    public String generateContainerId() {
        if (latestContainerIdSuffix >= 10000)
            latestContainerIdSuffix = 0;
        latestContainerIdSuffix++;

        return containerIdPrefix + latestContainerIdSuffix;
    }

    /**
     * 
     * @return New port for Jenkins
     */
    public int generateJenkinsPort() {
        if (latestJenkinsPort >= MAX_JENKINS_PORT)
            latestJenkinsPort = MIN_JENKINS_PORT - 1;
        latestJenkinsPort++;

        return latestJenkinsPort;
    }

    /**
     * 
     * @return New port for SSH
     */
    public int generateSshPort() {
        if (latestSshPort >= MAX_SSH_PORT)
            latestSshPort = MIN_SSH_PORT - 1;
        latestSshPort++;

        return latestSshPort;
    }

    /**
     * USE THIS METHOD WITH CAUTION!
     * It should be used only for test reason or when you are
     * really sure, what you are doing.
     * This method enables to influent generated values.
     * 
     * It is possible to directly set this values.
     * 
     * @param sshPort Next sshPort value (Max possible value 29999)
     * @param jenkinsPort Next jenkinsPort value (Max value is 39999)
     * @param containerIdPrefix Prefix of generated containerIds
     */
    public void forceNextValues(int sshPort, int jenkinsPort, String containerIdPrefix) {
        this.latestJenkinsPort = jenkinsPort - 1;
        this.latestSshPort = sshPort - 1;
        this.containerIdPrefix = containerIdPrefix;
    }
    

    /**
     * USE THIS METHOD WITH CAUTION!
     * It should be used only for test reason or when you are
     * really sure, what you are doing.
     * This method enables to influent generated values.
     * 
     * The state values are reinitialized to default values.
     */
    public void reInit() {
        init();
    }
    
}
