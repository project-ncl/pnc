package org.jboss.pnc.model;

/**
 * List of Jenkins job statuses.
 *
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public enum BuildDriverStatus {
    SUCCESS,
    FAILED, 
    UNSTABLE, 
    REBUILDING, 
    BUILDING, 
    ABORTED, 
    CANCELLED,
    UNKNOWN
}
