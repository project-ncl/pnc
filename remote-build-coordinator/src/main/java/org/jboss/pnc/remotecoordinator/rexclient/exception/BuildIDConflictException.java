/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.remotecoordinator.rexclient.exception;

/**
 * Rex returned Constraint validation on Build ID. A Task with the same Build ID already exists.
 */
public class BuildIDConflictException extends ConflictResponseException {
    private final String buildId;

    public BuildIDConflictException(String buildId) {
        this.buildId = buildId;
    }

    public BuildIDConflictException(String message, String buildId) {
        super(message);
        this.buildId = buildId;
    }

    public BuildIDConflictException(String message, Throwable cause, String buildId) {
        super(message, cause);
        this.buildId = buildId;
    }

    public BuildIDConflictException(Throwable cause, String buildId) {
        super(cause);
        this.buildId = buildId;
    }
}
