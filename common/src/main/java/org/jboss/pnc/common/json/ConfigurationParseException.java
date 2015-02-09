package org.jboss.pnc.common.json;

public class ConfigurationParseException extends Exception{
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ConfigurationParseException() {
        super();
    }
    
    public ConfigurationParseException(String msg) {
        super(msg);
    }
    
    public ConfigurationParseException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
