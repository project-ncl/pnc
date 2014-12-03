package org.jboss.pnc.spi.builddriver.exception;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-01.
 */
public class BuildDriverException extends Exception {

    private static final long serialVersionUID = -2381584604707278639L;

    public BuildDriverException(String message) {
        super(message);
    }

    public BuildDriverException(String message, Exception cause) {
        super(message, cause);
    }
}
