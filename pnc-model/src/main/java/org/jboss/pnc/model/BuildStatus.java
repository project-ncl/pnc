package org.jboss.pnc.model;

/**
 * Statuses of build process.
 * 
 * @author Jakub Bartecek <jbartece@redhat.com>
 *
 */
public enum BuildStatus {
    SUCCESS, FAILED, UNSTABLE, BUILDING, ABORTED, CANCELLED, SYSTEM_ERROR, UNKNOWN
}
