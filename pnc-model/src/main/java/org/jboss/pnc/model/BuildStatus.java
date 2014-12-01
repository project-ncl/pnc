package org.jboss.pnc.model;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * This class indicates the progress of a build: finished with success, in progress, cancelled or finished with errors
 */
public enum BuildStatus {
    SUCCESS,
    IN_PROGRESS,
    CANCELLED,
    FAILED
}
