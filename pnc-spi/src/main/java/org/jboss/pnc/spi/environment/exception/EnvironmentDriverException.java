package org.jboss.pnc.spi.environment.exception;

/**
 * Exception, which indicates environment driver error
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public class EnvironmentDriverException extends Exception {

    /**
     * 
     * @param msg Error message
     */
    public EnvironmentDriverException(String msg) {
        super(msg);
    }

    /**
     * 
     * @param msg Error message
     * @param cause Exception, which caused this exception
     */
    public EnvironmentDriverException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
