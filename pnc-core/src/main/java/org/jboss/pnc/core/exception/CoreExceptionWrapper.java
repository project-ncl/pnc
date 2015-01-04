package org.jboss.pnc.core.exception;

/**
 * Wraps exception in RuntimeException
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-23.
 */
public class CoreExceptionWrapper extends RuntimeException {
    public CoreExceptionWrapper(Throwable cause) {
        super(cause);
    }
}
