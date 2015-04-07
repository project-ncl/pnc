package org.jboss.pnc.core.exception;

import org.jboss.pnc.spi.environment.DestroyableEnvironmnet;

/**
 * Exception in build process, which contains data
 * to clean up after unsuccessful build task 
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class BuildProcessException extends CoreExceptionWrapper {
    
    private DestroyableEnvironmnet destroyableEnvironment;
    
    public BuildProcessException(Throwable cause) {
        super(cause);
    }

    /**
     * @param cause Exception cause
     * @param runningEnvironment Reference to a started environment  
     */
    public BuildProcessException(Throwable cause, DestroyableEnvironmnet destroyableEnvironment) {
        super(cause);
        this.destroyableEnvironment = destroyableEnvironment;
    }

    public DestroyableEnvironmnet getDestroyableEnvironmnet() {
        return destroyableEnvironment;
    }

}
