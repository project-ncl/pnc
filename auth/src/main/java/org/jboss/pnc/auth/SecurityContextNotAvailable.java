package org.jboss.pnc.auth;

public class SecurityContextNotAvailable extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public final static String MSG = "Could not get SecurityContext";

    public SecurityContextNotAvailable() {
        super();
    }

    public SecurityContextNotAvailable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SecurityContextNotAvailable(String message, Throwable cause) {
        super(message, cause);
    }

    public SecurityContextNotAvailable(String message) {
        super(message);
    }

    public SecurityContextNotAvailable(Throwable cause) {
        super(cause);
    }
    
    

}
