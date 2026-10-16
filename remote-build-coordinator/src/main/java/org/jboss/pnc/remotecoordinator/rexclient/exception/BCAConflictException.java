/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.exception;

/**
 * Rex returned Constraint validation on BuildConfigurationAudited ID:REV combination. A Task with the same BCA already
 * exists.
 */
public class BCAConflictException extends ConflictResponseException {
    private final String bcaIdRev;

    public BCAConflictException(String bcaIdRev) {
        this.bcaIdRev = bcaIdRev;
    }

    public BCAConflictException(String message, String bcaIdRev) {
        super(message);
        this.bcaIdRev = bcaIdRev;
    }

    public BCAConflictException(String message, Throwable cause, String bcaIdRev) {
        super(message, cause);
        this.bcaIdRev = bcaIdRev;
    }

    public BCAConflictException(Throwable cause, String bcaIdRev) {
        super(cause);
        this.bcaIdRev = bcaIdRev;
    }
}
