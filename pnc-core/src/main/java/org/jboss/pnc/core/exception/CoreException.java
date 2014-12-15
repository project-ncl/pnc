package org.jboss.pnc.core.exception;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public class CoreException extends Exception {
    public CoreException(String message) {
        super(message);
    }

    public CoreException(Exception e) {
        super(e);
    }
}
