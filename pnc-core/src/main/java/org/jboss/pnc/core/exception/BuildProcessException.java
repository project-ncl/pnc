package org.jboss.pnc.core.exception;

import org.jboss.pnc.spi.environment.RunningEnvironment;

/**
 * Exception in build process, which contains data
 * to clean up after unsuccessful build task 
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class BuildProcessException extends CoreExceptionWrapper {
    
    private RunningEnvironment runningEnvironment;
    
    public BuildProcessException(Throwable cause) {
        super(cause);
    }

    /**
     * @param cause Exception cause
     * @param runningEnvironment Reference to a started environment  
     */
    public BuildProcessException(Throwable cause, RunningEnvironment runningEnvironment) {
        super(cause);
        this.runningEnvironment = runningEnvironment;
    }

    public RunningEnvironment getRunningEnvironment() {
        return runningEnvironment;
    }

}
