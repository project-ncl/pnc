package org.jboss.pnc.auth;

public class SecurityContextNotAvailable extends Exception{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    public final static String MSG = "Could not get SecurityContext";

    public SecurityContextNotAvailable() {
        super();
        // TODO Auto-generated constructor stub
    }

    public SecurityContextNotAvailable(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

    public SecurityContextNotAvailable(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public SecurityContextNotAvailable(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public SecurityContextNotAvailable(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }
    
    

}
