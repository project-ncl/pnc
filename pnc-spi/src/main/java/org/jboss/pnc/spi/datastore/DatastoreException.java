package org.jboss.pnc.spi.datastore;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-24.
 */
public class DatastoreException extends Exception {
    public DatastoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
